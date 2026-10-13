package com.example.backofficemvp.Controller

import com.example.backofficemvp.Dto.ApplicationResponse
import com.example.backofficemvp.Security.CurrentUser
import com.example.backofficemvp.Service.ApprovalService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
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

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('APPROVER_1', 'APPROVER_2')")
    fun pending(): List<ApplicationResponse> {
        return approvalService.pending(currentUser.require())
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('APPROVER_1', 'APPROVER_2')")
    fun approve(@PathVariable id: String): ApplicationResponse {
        return approvalService.approve(currentUser.require(), id)
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('APPROVER_1', 'APPROVER_2')")
    fun reject(
        @PathVariable id: String,
        @Valid @RequestBody body: RejectRequest,
    ): ApplicationResponse {
        return approvalService.reject(currentUser.require(), id, body.comment)
    }
}
