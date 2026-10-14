package com.example.userapplication.service

import com.example.userapplication.dto.ApplicationRequest
import com.example.userapplication.dto.ApplicationResponse
import com.example.userapplication.kafka.ApplicationEventPublisher
import com.example.userapplication.model.Application
import com.example.userapplication.model.ApplicationStatus
import com.example.userapplication.model.ApprovalHistory
import com.example.userapplication.model.Role
import com.example.userapplication.model.User
import com.example.userapplication.repository.ApplicationRepository
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class ApplicationService(
    private val applicationRepository: ApplicationRepository,
    private val verificationGroupService: VerificationGroupService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    fun create(user: User, request: ApplicationRequest): ApplicationResponse {
        requireCustomer(user)
        if (applicationRepository.findByCustomerId(user.id) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Customer already has an application")
        }
        val now = Instant.now()
        val draft = request.toApplication(
            customerId = user.id,
            status = ApplicationStatus.DRAFT,
            approvalHistory = ApprovalHistory(),
            createdAt = now,
            updatedAt = now,
        )
        ApplicationRules.validate(draft)
        return respond(applicationRepository.save(draft))
    }

    fun get(user: User, id: String): ApplicationResponse {
        requireCustomer(user)
        return respond(findOwned(user, id))
    }

    fun update(user: User, id: String, request: ApplicationRequest): ApplicationResponse {
        requireCustomer(user)
        val existing = findOwned(user, id)
        if (existing.status != ApplicationStatus.DRAFT && existing.status != ApplicationStatus.REJECTED) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Application can only be edited while DRAFT or REJECTED",
            )
        }
        val updated = request.toApplication(
            id = existing.id,
            customerId = existing.customerId,
            status = existing.status,
            approvalHistory = existing.approvalHistory,
            createdAt = existing.createdAt,
            updatedAt = Instant.now(),
        )
        ApplicationRules.validate(updated)
        return respond(applicationRepository.save(updated))
    }

    fun submit(user: User, id: String): ApplicationResponse {
        requireCustomer(user)
        val existing = findOwned(user, id)
        if (existing.status != ApplicationStatus.DRAFT && existing.status != ApplicationStatus.REJECTED) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Application can only be submitted while DRAFT or REJECTED",
            )
        }
        ApplicationRules.validate(existing)
        val previousGroups = verificationGroupService.find(existing.id) ?: existing.verificationGroups
        val submitted = applicationRepository.save(
            existing.copy(
                status = ApplicationStatus.PENDING_APPROVAL,
                approvalHistory = ApprovalHistory(),
                currentGroupIndex = 0,
                updatedAt = Instant.now(),
            )
        )
        verificationGroupService.save(existing.id, emptyList())
        try {
            applicationEventPublisher.submitted(submitted)
        } catch (ex: Exception) {
            applicationRepository.save(
                submitted.copy(
                    status = existing.status,
                    approvalHistory = existing.approvalHistory,
                    currentGroupIndex = existing.currentGroupIndex,
                    updatedAt = existing.updatedAt,
                )
            )
            verificationGroupService.save(existing.id, previousGroups)
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Approval queue is unavailable", ex)
        }
        return respond(submitted)
    }

    private fun respond(application: Application): ApplicationResponse {
        val stored = verificationGroupService.find(application.id) ?: application.verificationGroups
        return application.copy(verificationGroups = stored).toResponse()
    }

    private fun findOwned(user: User, id: String): Application {
        val application = find(id)
        if (application.customerId != user.id) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")
        }
        return application
    }

    private fun find(id: String): Application {
        if (!ObjectId.isValid(id)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid application id")
        }
        return applicationRepository.findById(ObjectId(id)).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")
        }
    }

    private fun requireCustomer(user: User) {
        if (user.role != Role.CUSTOMER) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers can change an application")
        }
    }
}
