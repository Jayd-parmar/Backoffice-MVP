package com.example.backofficeapproval.kafka

import com.example.backofficeapproval.model.BackofficeReview
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class ApplicationEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {

    fun decided(review: BackofficeReview) {
        val key = review.applicationId.toHexString()
        send(
            ApplicationTopics.DECIDED,
            key,
            ApplicationDecidedEvent(
                applicationId = key,
                status = review.status,
                approvalHistory = review.approvalHistory.toEvent(),
                verificationGroups = review.verificationGroups.toEvent(),
                currentGroupIndex = review.currentGroupIndex,
                updatedAt = review.updatedAt,
            ),
        )
    }

    private fun send(topic: String, key: String, event: Any) {
        kafkaTemplate.send(topic, key, event).get(10, TimeUnit.SECONDS)
    }
}
