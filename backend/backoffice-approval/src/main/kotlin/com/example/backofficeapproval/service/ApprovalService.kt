package com.example.backofficeapproval.service

import com.example.backofficeapproval.dto.ApplicationResponse
import com.example.backofficeapproval.kafka.ApplicationEventPublisher
import com.example.backofficeapproval.model.ApplicationStatus
import com.example.backofficeapproval.model.ApprovalAction
import com.example.backofficeapproval.model.ApprovalDecision
import com.example.backofficeapproval.model.ApprovalHistory
import com.example.backofficeapproval.model.BackofficeReview
import com.example.backofficeapproval.model.DocumentCode
import com.example.backofficeapproval.model.DocumentVerification
import com.example.backofficeapproval.model.Role
import com.example.backofficeapproval.model.User
import com.example.backofficeapproval.model.VerificationGroup
import com.example.backofficeapproval.repository.BackofficeRepository
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
    private val backofficeRepository: BackofficeRepository,
    private val verificationGroupService: VerificationGroupService,
    private val mongoTemplate: MongoTemplate,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    fun pending(user: User): List<ApplicationResponse> {
        return backofficeRepository.findByStatusAndCurrentGroupIndexOrderByUpdatedAtAsc(
            ApplicationStatus.PENDING_APPROVAL,
            levelIndex(user),
        ).map { withGroups(it).toResponse(maskPanNumber = true) }
    }

    fun open(user: User, id: String): ApplicationResponse {
        val review = findReview(id)
        requireCurrentLevel(review, user)
        return review.toResponse(maskPanNumber = false)
    }

    fun verify(user: User, id: String, code: DocumentCode): ApplicationResponse {
        val review = findReview(id)
        requireCurrentLevel(review, user)
        val group = currentGroup(review)
        val document = group.documents.find { it.code == code }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "This document is not in the current review")
        if (document.verified) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "This document is already verified")
        }
        val now = Instant.now()
        val documents = group.documents.map { document ->
            if (document.code == code) {
                document.copy(verified = true, verifiedBy = user.id, verifiedAt = now)
            } else {
                document
            }
        }
        return applyDocuments(review, user, documents, now).toResponse(maskPanNumber = false)
    }

    fun approve(user: User, id: String): ApplicationResponse {
        val review = findReview(id)
        requireCurrentLevel(review, user)
        val now = Instant.now()
        val documents = currentGroup(review).documents.map { document ->
            if (document.verified) {
                document
            } else {
                document.copy(verified = true, verifiedBy = user.id, verifiedAt = now)
            }
        }
        return applyDocuments(review, user, documents, now).toResponse(maskPanNumber = false)
    }

    fun reject(user: User, id: String, comment: String): ApplicationResponse {
        val reason = comment.trim()
        if (reason.isEmpty()) {
            throw IllegalArgumentException("comment: Comment is required")
        }
        val review = findReview(id)
        requireCurrentLevel(review, user)
        val now = Instant.now()
        val updated = review.copy(
            status = ApplicationStatus.REJECTED,
            approvalHistory = historyWith(review.approvalHistory, user, ApprovalAction.REJECTED, reason, now),
            updatedAt = now,
        )
        return saveIfUnchanged(review, updated, publish = true).toResponse(maskPanNumber = false)
    }

    private fun applyDocuments(
        review: BackofficeReview,
        user: User,
        documents: List<DocumentVerification>,
        now: Instant,
    ): BackofficeReview {
        val groups = review.verificationGroups.toMutableList()
        val group = currentGroup(review).copy(documents = documents)
        groups[review.currentGroupIndex] = group
        val complete = documents.all { it.verified }
        val finished = complete && review.currentGroupIndex + 1 >= review.verificationGroups.size
        val updated = review.copy(
            verificationGroups = groups,
            currentGroupIndex = if (complete && !finished) review.currentGroupIndex + 1 else review.currentGroupIndex,
            status = if (finished) ApplicationStatus.APPROVED else ApplicationStatus.PENDING_APPROVAL,
            approvalHistory = if (complete) {
                historyWith(review.approvalHistory, user, ApprovalAction.APPROVED, null, now)
            } else {
                review.approvalHistory
            },
            updatedAt = now,
        )
        return saveIfUnchanged(review, updated, publish = complete)
    }

    private fun saveIfUnchanged(
        current: BackofficeReview,
        updated: BackofficeReview,
        publish: Boolean,
    ): BackofficeReview {
        val saved = mongoTemplate.findAndModify(
            Query.query(
                Criteria.where("_id").`is`(current.id)
                    .and("status").`is`(ApplicationStatus.PENDING_APPROVAL)
                    .and("currentGroupIndex").`is`(current.currentGroupIndex)
                    .and("updatedAt").`is`(current.updatedAt),
            ),
            Update()
                .set("status", updated.status)
                .set("currentGroupIndex", updated.currentGroupIndex)
                .set("approvalHistory", historyDocument(updated.approvalHistory))
                .set("updatedAt", updated.updatedAt),
            FindAndModifyOptions.options().returnNew(true),
            BackofficeReview::class.java,
        ) ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Application was already reviewed")

        val result = saved.copy(verificationGroups = updated.verificationGroups)
        try {
            verificationGroupService.save(result.applicationId, result.verificationGroups)
            if (publish) applicationEventPublisher.decided(result)
            return result
        } catch (ex: Exception) {
            backofficeRepository.save(current)
            verificationGroupService.save(current.applicationId, current.verificationGroups)
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Approval queue is unavailable", ex)
        }
    }

    private fun withGroups(review: BackofficeReview): BackofficeReview {
        val groups = verificationGroupService.find(review.applicationId) ?: review.verificationGroups
        return review.copy(verificationGroups = groups)
    }

    private fun currentGroup(review: BackofficeReview): VerificationGroup {
        return review.verificationGroups.getOrNull(review.currentGroupIndex)
            ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Application has no review group")
    }

    private fun requireCurrentLevel(review: BackofficeReview, user: User) {
        val allowed = review.status == ApplicationStatus.PENDING_APPROVAL &&
            review.currentGroupIndex == levelIndex(user)
        if (!allowed) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Application is not assigned to your approval level")
        }
    }

    private fun levelIndex(user: User): Int {
        return when (user.role) {
            Role.APPROVER_1 -> 0
            Role.APPROVER_2 -> 1
            Role.APPROVER_3 -> 2
        }
    }

    private fun historyWith(
        history: ApprovalHistory,
        user: User,
        action: ApprovalAction,
        comment: String?,
        actionAt: Instant,
    ): ApprovalHistory {
        val decision = ApprovalDecision(
            userId = user.id,
            action = action,
            comment = comment,
            actionAt = actionAt,
        )
        return when (user.role) {
            Role.APPROVER_1 -> history.copy(approver1 = decision)
            Role.APPROVER_2 -> history.copy(approver2 = decision)
            Role.APPROVER_3 -> history.copy(approver3 = decision)
        }
    }

    private fun historyDocument(history: ApprovalHistory): Document {
        return Document()
            .append("approver1", decisionDocument(history.approver1))
            .append("approver2", decisionDocument(history.approver2))
            .append("approver3", decisionDocument(history.approver3))
    }

    private fun decisionDocument(decision: ApprovalDecision): Document {
        return Document()
            .append("userId", decision.userId)
            .append("action", decision.action?.name)
            .append("comment", decision.comment)
            .append("actionAt", decision.actionAt?.let(Date::from))
    }

    private fun findReview(id: String): BackofficeReview {
        if (!ObjectId.isValid(id)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid application id")
        }
        val review = backofficeRepository.findByApplicationId(ObjectId(id))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")
        return withGroups(review)
    }
}
