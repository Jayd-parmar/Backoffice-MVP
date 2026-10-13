package com.example.backofficemvp.Kafka

import com.example.backofficemvp.Model.ApplicationStatus
import com.example.backofficemvp.Model.ApprovalAction
import com.example.backofficemvp.Model.ApprovalDecision
import com.example.backofficemvp.Model.ApprovalHistory
import org.bson.types.ObjectId
import java.time.Instant

object ApplicationTopics {
    const val CREATED = "application.created"
    const val DECIDED = "application.decided"
}

data class ApplicationCreatedEvent(
    val applicationId: String,
    val customerId: String,
    val status: ApplicationStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ApplicationDecidedEvent(
    val applicationId: String,
    val status: ApplicationStatus,
    val approvalHistory: ApprovalHistoryEvent,
    val updatedAt: Instant,
)

data class ApprovalHistoryEvent(
    val approver1: ApprovalDecisionEvent = ApprovalDecisionEvent(),
    val approver2: ApprovalDecisionEvent = ApprovalDecisionEvent(),
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
    )
}

fun ApprovalHistoryEvent.toModel(): ApprovalHistory {
    return ApprovalHistory(
        approver1 = approver1.toModel(),
        approver2 = approver2.toModel(),
    )
}

private fun ApprovalDecision.toEvent(): ApprovalDecisionEvent {
    return ApprovalDecisionEvent(
        userId = userId?.toHexString(),
        action = action,
        comment = comment,
        actionAt = actionAt,
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
