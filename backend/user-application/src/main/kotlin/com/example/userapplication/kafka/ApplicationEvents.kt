package com.example.userapplication.kafka

import com.example.userapplication.model.AddressDetails
import com.example.userapplication.model.ApplicationStatus
import com.example.userapplication.model.ApprovalAction
import com.example.userapplication.model.ApprovalDecision
import com.example.userapplication.model.ApprovalHistory
import com.example.userapplication.model.VerificationGroup
import com.example.userapplication.model.ForexCardDetails
import com.example.userapplication.model.PersonalDetails
import com.example.userapplication.model.TravelDetails
import org.bson.types.ObjectId
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
    val verificationGroups: List<VerificationGroup> = emptyList(),
    val currentGroupIndex: Int = 0,
    val updatedAt: Instant,
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

fun ApprovalHistoryEvent.toModel(): ApprovalHistory {
    return ApprovalHistory(
        approver1 = approver1.toModel(),
        approver2 = approver2.toModel(),
        approver3 = approver3.toModel(),
    )
}

private fun ApprovalDecisionEvent.toModel(): ApprovalDecision {
    return ApprovalDecision(
        userId = userId?.let(::ObjectId),
        action = action,
        comment = comment,
        actionAt = actionAt,
    )
}
