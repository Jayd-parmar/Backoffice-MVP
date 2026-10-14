package com.example.backofficeapproval.service

import com.example.backofficeapproval.dto.ApplicationResponse
import com.example.backofficeapproval.dto.DocumentVerificationResponse
import com.example.backofficeapproval.dto.VerificationGroupResponse
import com.example.backofficeapproval.model.DocumentCode
import com.example.backofficeapproval.model.VerificationGroup
import com.example.backofficeapproval.dto.ApprovalDecisionResponse
import com.example.backofficeapproval.dto.ApprovalResponse
import com.example.backofficeapproval.model.ApprovalDecision
import com.example.backofficeapproval.model.BackofficeReview

fun BackofficeReview.toResponse(maskPanNumber: Boolean): ApplicationResponse {
    val personal = if (maskPanNumber) {
        personalDetails.copy(panNumber = ApplicationRules.maskPan(personalDetails.panNumber))
    } else {
        personalDetails
    }
    return ApplicationResponse(
        id = applicationId.toHexString(),
        customerId = customerId.toHexString(),
        status = status,
        personalDetails = personal,
        forexCardDetails = forexCardDetails,
        addressDetails = addressDetails,
        travelDetails = travelDetails,
        approvalHistory = ApprovalResponse(
            approver1 = approvalHistory.approver1.toResponse(),
            approver2 = approvalHistory.approver2.toResponse(),
            approver3 = approvalHistory.approver3.toResponse(),
        ),
        verificationGroups = verificationGroups.map { it.toResponse() },
        currentGroupIndex = currentGroupIndex,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

private fun ApprovalDecision.toResponse(): ApprovalDecisionResponse {
    return ApprovalDecisionResponse(
        userId = userId?.toHexString(),
        action = action,
        comment = comment,
        actionAt = actionAt,
    )
}

private fun VerificationGroup.toResponse(): VerificationGroupResponse {
    return VerificationGroupResponse(
        code = code,
        label = groupLabel(code),
        verifiedByLevel = verifiedByLevel,
        documents = documents.map { document ->
            DocumentVerificationResponse(
                code = document.code,
                label = documentLabel(document.code),
                verified = document.verified,
            )
        },
    )
}

private fun documentLabel(code: DocumentCode): String {
    return when (code) {
        DocumentCode.AADHAAR -> "Aadhaar"
        DocumentCode.PAN -> "PAN"
        DocumentCode.PASSPORT -> "Passport"
        DocumentCode.VOTER_ID -> "Voter ID"
        DocumentCode.DRIVING_LICENSE -> "Driving licence"
    }
}

private fun groupLabel(code: String): String {
    return when (code) {
        "AADHAAR_PAN" -> "Aadhaar and PAN"
        "VOTER_DRIVING" -> "Voter ID and driving licence"
        "PASSPORT" -> "Passport"
        "DRIVING" -> "Driving licence"
        else -> code
    }
}
