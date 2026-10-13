package com.example.backofficemvp.Service

import com.example.backofficemvp.Dto.ApplicationRequest
import com.example.backofficemvp.Dto.ApplicationResponse
import com.example.backofficemvp.Model.Application
import com.example.backofficemvp.Model.ApplicationStatus
import com.example.backofficemvp.Model.ApprovalHistory
import com.example.backofficemvp.Model.Role
import com.example.backofficemvp.Model.User
import com.example.backofficemvp.Repository.ApplicationRepository
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class ApplicationService(
    private val applicationRepository: ApplicationRepository,
    private val approvalService: ApprovalService,
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
        return approvalService.saveNewDraft(draft).toResponse(maskPanNumber = false)
    }

    fun get(user: User, id: String): ApplicationResponse {
        if (user.role == Role.APPROVER_1 || user.role == Role.APPROVER_2) {
            return approvalService.open(user, id)
        }
        val application = find(id)
        if (user.role != Role.CUSTOMER || application.customerId != user.id) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")
        }
        return application.toResponse(maskPanNumber = false)
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
        return applicationRepository.save(updated).toResponse(maskPanNumber = false)
    }

    fun submit(user: User, id: String): ApplicationResponse {
        requireCustomer(user)
        val existing = findOwned(user, id)
        return approvalService.submit(existing).toResponse(maskPanNumber = false)
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
