package com.example.backofficeapproval.dto

import com.example.backofficeapproval.model.AddressDetails
import com.example.backofficeapproval.model.DocumentCode
import com.example.backofficeapproval.model.ApplicationStatus
import com.example.backofficeapproval.model.ApprovalAction
import com.example.backofficeapproval.model.ForexCardDetails
import com.example.backofficeapproval.model.PersonalDetails
import com.example.backofficeapproval.model.TravelDetails
import java.time.Instant

data class ApprovalDecisionResponse(
    val userId: String?,
    val action: ApprovalAction?,
    val comment: String?,
    val actionAt: Instant?,
)

data class ApprovalResponse(
    val approver1: ApprovalDecisionResponse,
    val approver2: ApprovalDecisionResponse,
    val approver3: ApprovalDecisionResponse,
)

data class DocumentVerificationResponse(
    val code: DocumentCode,
    val label: String,
    val verified: Boolean,
)

data class VerificationGroupResponse(
    val code: String,
    val label: String,
    val documents: List<DocumentVerificationResponse>,
)

data class ApplicationResponse(
    val id: String,
    val customerId: String,
    val status: ApplicationStatus,
    val personalDetails: PersonalDetails,
    val forexCardDetails: ForexCardDetails,
    val addressDetails: AddressDetails,
    val travelDetails: TravelDetails,
    val approvalHistory: ApprovalResponse,
    val verificationGroups: List<VerificationGroupResponse> = emptyList(),
    val currentGroupIndex: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
)
