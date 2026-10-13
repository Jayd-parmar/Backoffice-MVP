package com.example.backofficemvp.Service

import com.example.backofficemvp.Dto.ApplicationResponse
import com.example.backofficemvp.Kafka.ApplicationEventPublisher
import com.example.backofficemvp.Model.Application
import com.example.backofficemvp.Model.ApplicationStatus
import com.example.backofficemvp.Model.ApprovalAction
import com.example.backofficemvp.Model.ApprovalDecision
import com.example.backofficemvp.Model.ApprovalHistory
import com.example.backofficemvp.Model.BackofficeReview
import com.example.backofficemvp.Model.Role
import com.example.backofficemvp.Model.User
import com.example.backofficemvp.Repository.ApplicationRepository
import com.example.backofficemvp.Repository.BackofficeRepository
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.Date

@Service
class ApprovalService(
    private val applicationRepository: ApplicationRepository,
    private val backofficeRepository: BackofficeRepository,
    private val mongoTemplate: MongoTemplate,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    fun saveNewDraft(application: Application): Application {
        return applicationRepository.save(
            application.copy(
                status = ApplicationStatus.DRAFT,
                approvalHistory = ApprovalHistory(),
            )
        )
    }

    fun submit(application: Application): Application {
        if (application.status != ApplicationStatus.DRAFT && application.status != ApplicationStatus.REJECTED) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Application can only be submitted while DRAFT or REJECTED",
            )
        }
        ApplicationRules.validate(application)
        val saved = applicationRepository.save(
            application.copy(
                status = ApplicationStatus.PENDING_APPROVER_1,
                approvalHistory = ApprovalHistory(),
                updatedAt = Instant.now(),
            )
        )
        try {
            applicationEventPublisher.created(saved)
        } catch (ex: Exception) {
            applicationRepository.save(
                saved.copy(
                    status = application.status,
                    approvalHistory = application.approvalHistory,
                    updatedAt = application.updatedAt,
                )
            )
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Approval queue is unavailable", ex)
        }
        return saved
    }

    fun pending(user: User): List<ApplicationResponse> {
        val status = queueStatus(user)
        val reviews = backofficeRepository.findByStatusOrderByUpdatedAtAsc(status)
        if (reviews.isEmpty()) {
            return emptyList()
        }
        val applications = applicationRepository.findAllById(reviews.map { it.applicationId })
            .associateBy { it.id }
        return reviews.map { review ->
            val application = applications[review.applicationId]
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")
            toApprovalResponse(review, application, maskPanNumber = true)
        }
    }

    fun open(user: User, id: String): ApplicationResponse {
        val review = findReview(id)
        val allowed = when (user.role) {
            Role.APPROVER_1 -> review.status == ApplicationStatus.PENDING_APPROVER_1
            Role.APPROVER_2 -> review.status == ApplicationStatus.PENDING_APPROVER_2
            Role.CUSTOMER -> false
        }
        if (!allowed) {
            val message = when (user.role) {
                Role.APPROVER_1 -> "Application is not assigned to approver 1"
                Role.APPROVER_2 -> "Application is not assigned to approver 2"
                Role.CUSTOMER -> "Customers cannot open the approval queue"
            }
            throw ResponseStatusException(HttpStatus.FORBIDDEN, message)
        }
        return toApprovalResponse(review, findApplication(review.applicationId), maskPanNumber = false)
    }

    fun approve(user: User, id: String): ApplicationResponse {
        val review = findReview(id)
        val now = Instant.now()
        val updated = when (user.role) {
            Role.APPROVER_1 -> {
                requireQueue(review.status, ApplicationStatus.PENDING_APPROVER_1, "Application is not awaiting approver 1")
                review.copy(
                    status = ApplicationStatus.PENDING_APPROVER_2,
                    approvalHistory = review.approvalHistory.copy(
                        approver1 = decision(user, ApprovalAction.APPROVED, comment = null, actionAt = now),
                    ),
                    updatedAt = now,
                )
            }
            Role.APPROVER_2 -> {
                requireQueue(review.status, ApplicationStatus.PENDING_APPROVER_2, "Application is not awaiting approver 2")
                review.copy(
                    status = ApplicationStatus.APPROVED,
                    approvalHistory = review.approvalHistory.copy(
                        approver2 = decision(user, ApprovalAction.APPROVED, comment = null, actionAt = now),
                    ),
                    updatedAt = now,
                )
            }
            Role.CUSTOMER -> throw ResponseStatusException(HttpStatus.FORBIDDEN, "Customers cannot approve applications")
        }
        val saved = saveIfStillPending(review, updated)
        return toApprovalResponse(saved, findApplication(saved.applicationId), maskPanNumber = false)
    }

    fun reject(user: User, id: String, comment: String): ApplicationResponse {
        val reason = comment.trim()
        if (reason.isEmpty()) {
            throw IllegalArgumentException("comment: Comment is required")
        }
        val review = findReview(id)
        val now = Instant.now()
        val updated = when (user.role) {
            Role.APPROVER_1 -> {
                requireQueue(review.status, ApplicationStatus.PENDING_APPROVER_1, "Application is not awaiting approver 1")
                review.copy(
                    status = ApplicationStatus.REJECTED,
                    approvalHistory = review.approvalHistory.copy(
                        approver1 = decision(user, ApprovalAction.REJECTED, comment = reason, actionAt = now),
                    ),
                    updatedAt = now,
                )
            }
            Role.APPROVER_2 -> {
                requireQueue(review.status, ApplicationStatus.PENDING_APPROVER_2, "Application is not awaiting approver 2")
                review.copy(
                    status = ApplicationStatus.REJECTED,
                    approvalHistory = review.approvalHistory.copy(
                        approver2 = decision(user, ApprovalAction.REJECTED, comment = reason, actionAt = now),
                    ),
                    updatedAt = now,
                )
            }
            Role.CUSTOMER -> throw ResponseStatusException(HttpStatus.FORBIDDEN, "Customers cannot reject applications")
        }
        val saved = saveIfStillPending(review, updated)
        return toApprovalResponse(saved, findApplication(saved.applicationId), maskPanNumber = false)
    }

    private fun saveIfStillPending(current: BackofficeReview, updated: BackofficeReview): BackofficeReview {
        val saved = mongoTemplate.findAndModify(
            Query.query(
                Criteria.where("_id").`is`(current.id).and("status").`is`(current.status),
            ),
            Update()
                .set("status", updated.status)
                .set("approvalHistory", historyDocument(updated.approvalHistory))
                .set("updatedAt", updated.updatedAt),
            FindAndModifyOptions.options().returnNew(true),
            BackofficeReview::class.java,
        ) ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Application was already reviewed")

        try {
            applicationEventPublisher.decided(saved)
        } catch (ex: Exception) {
            mongoTemplate.findAndModify(
                Query.query(
                    Criteria.where("_id").`is`(saved.id).and("status").`is`(saved.status),
                ),
                Update()
                    .set("status", current.status)
                    .set("approvalHistory", historyDocument(current.approvalHistory))
                    .set("updatedAt", current.updatedAt),
                FindAndModifyOptions.options().returnNew(true),
                BackofficeReview::class.java,
            )
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Approval queue is unavailable", ex)
        }
        return saved
    }

    private fun queueStatus(user: User): ApplicationStatus {
        return when (user.role) {
            Role.APPROVER_1 -> ApplicationStatus.PENDING_APPROVER_1
            Role.APPROVER_2 -> ApplicationStatus.PENDING_APPROVER_2
            Role.CUSTOMER -> throw ResponseStatusException(HttpStatus.FORBIDDEN, "Customers cannot view the approval queue")
        }
    }

    private fun requireQueue(status: ApplicationStatus, expected: ApplicationStatus, message: String) {
        if (status != expected) {
            throw ResponseStatusException(HttpStatus.CONFLICT, message)
        }
    }

    private fun historyDocument(history: ApprovalHistory): Document {
        return Document()
            .append("approver1", decisionDocument(history.approver1))
            .append("approver2", decisionDocument(history.approver2))
    }

    private fun decisionDocument(decision: ApprovalDecision): Document {
        return Document()
            .append("userId", decision.userId)
            .append("action", decision.action?.name)
            .append("comment", decision.comment)
            .append("actionAt", decision.actionAt?.let(Date::from))
    }

    private fun decision(
        user: User,
        action: ApprovalAction,
        comment: String?,
        actionAt: Instant,
    ): ApprovalDecision {
        return ApprovalDecision(
            userId = user.id,
            action = action,
            comment = comment,
            actionAt = actionAt,
        )
    }

    private fun findReview(id: String): BackofficeReview {
        if (!ObjectId.isValid(id)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid application id")
        }
        return backofficeRepository.findByApplicationId(ObjectId(id))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")
    }

    private fun findApplication(id: ObjectId): Application {
        return applicationRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")
        }
    }
}
