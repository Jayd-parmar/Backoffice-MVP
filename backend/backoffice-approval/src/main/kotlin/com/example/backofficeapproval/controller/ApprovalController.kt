package com.example.backofficeapproval.controller

import com.example.backofficeapproval.dto.ApplicationResponse
import com.example.backofficeapproval.security.CurrentUser
import com.example.backofficeapproval.service.ApprovalService
import com.example.backofficeapproval.model.DocumentCode
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/approvals")
class ApprovalController(
    private val approvalService: ApprovalService,
    private val currentUser: CurrentUser,
) {

    data class RejectRequest(
        @field:NotBlank(message = "Comment is required")
        @field:Size(max = 500, message = "Comment must be at most 500 characters")
        val comment: String,
    )

    data class VerifyRequest(
        @field:NotNull(message = "Document is required")
        val code: DocumentCode,
    )

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('APPROVER_1', 'APPROVER_2', 'APPROVER_3')")
    fun pending(): List<ApplicationResponse> {
        return approvalService.pending(currentUser.require())
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('APPROVER_1', 'APPROVER_2', 'APPROVER_3')")
    fun open(@PathVariable id: String): ApplicationResponse {
        return approvalService.open(currentUser.require(), id)
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('APPROVER_1', 'APPROVER_2', 'APPROVER_3')")
    fun verify(
        @PathVariable id: String,
        @Valid @RequestBody body: VerifyRequest,
    ): ApplicationResponse {
        return approvalService.verify(currentUser.require(), id, body.code)
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('APPROVER_1', 'APPROVER_2', 'APPROVER_3')")
    fun approve(@PathVariable id: String): ApplicationResponse {
        return approvalService.approve(currentUser.require(), id)
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('APPROVER_1', 'APPROVER_2', 'APPROVER_3')")
    fun reject(
        @PathVariable id: String,
        @Valid @RequestBody body: RejectRequest,
    ): ApplicationResponse {
        return approvalService.reject(currentUser.require(), id, body.comment)
    }
}
