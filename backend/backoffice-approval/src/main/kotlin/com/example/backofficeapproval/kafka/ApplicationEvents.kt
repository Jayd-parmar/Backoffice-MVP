package com.example.backofficeapproval.kafka

import com.example.backofficeapproval.model.AddressDetails
import com.example.backofficeapproval.model.DocumentCode
import com.example.backofficeapproval.model.VerificationGroup
import com.example.backofficeapproval.model.ApplicationStatus
import com.example.backofficeapproval.model.ApprovalAction
import com.example.backofficeapproval.model.ApprovalDecision
import com.example.backofficeapproval.model.ApprovalHistory
import com.example.backofficeapproval.model.ForexCardDetails
import com.example.backofficeapproval.model.PersonalDetails
import com.example.backofficeapproval.model.TravelDetails
import java.time.Instant

object ApplicationTopics {
    const val SUBMITTED = "application.submitted"
    const val DECIDED = "application.decided"
}

data class ApplicationSubmittedEvent(
    val applicationId: String,
    val customerId: String,
    val status: ApplicationStatus,
    val personalDetails: PersonalDetails,
    val forexCardDetails: ForexCardDetails,
    val addressDetails: AddressDetails,
    val travelDetails: TravelDetails,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ApplicationDecidedEvent(
    val applicationId: String,
    val status: ApplicationStatus,
    val approvalHistory: ApprovalHistoryEvent,
    val verificationGroups: List<VerificationGroupEvent> = emptyList(),
    val currentGroupIndex: Int = 0,
    val updatedAt: Instant,
)

data class VerificationGroupEvent(
    val code: String,
    val verifiedByLevel: String? = null,
    val documents: List<DocumentVerificationEvent> = emptyList(),
)

data class DocumentVerificationEvent(
    val code: DocumentCode,
    val verified: Boolean = false,
    val verifiedBy: String? = null,
    val verifiedAt: Instant? = null,
)

data class ApprovalHistoryEvent(
    val approver1: ApprovalDecisionEvent = ApprovalDecisionEvent(),
    val approver2: ApprovalDecisionEvent = ApprovalDecisionEvent(),
    val approver3: ApprovalDecisionEvent = ApprovalDecisionEvent(),
)

data class ApprovalDecisionEvent(
    val userId: String? = null,
    val action: ApprovalAction? = null,
    val comment: String? = null,
    val actionAt: Instant? = null,
)

fun ApprovalHistory.toEvent(): ApprovalHistoryEvent {
    return ApprovalHistoryEvent(
        approver1 = approver1.toEvent(),
        approver2 = approver2.toEvent(),
        approver3 = approver3.toEvent(),
    )
}

fun List<VerificationGroup>.toEvent(): List<VerificationGroupEvent> {
    return map { group ->
        VerificationGroupEvent(
            code = group.code,
            verifiedByLevel = group.verifiedByLevel.name,
            documents = group.documents.map { document ->
                DocumentVerificationEvent(
                    code = document.code,
                    verified = document.verified,
                    verifiedBy = document.verifiedBy?.toHexString(),
                    verifiedAt = document.verifiedAt,
                )
            },
        )
    }
}

private fun ApprovalDecision.toEvent(): ApprovalDecisionEvent {
    return ApprovalDecisionEvent(
        userId = userId?.toHexString(),
        action = action,
        comment = comment,
        actionAt = actionAt,
    )
}
