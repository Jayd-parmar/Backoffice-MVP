package com.example.backofficemvp.Kafka

import com.example.backofficemvp.Model.Application
import com.example.backofficemvp.Model.BackofficeReview
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class ApplicationEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {

    fun created(application: Application) {
        val key = application.id.toHexString()
        send(
            ApplicationTopics.CREATED,
            key,
            ApplicationCreatedEvent(
                applicationId = key,
                customerId = application.customerId.toHexString(),
                status = application.status,
                createdAt = application.updatedAt,
                updatedAt = application.updatedAt,
            ),
        )
    }

    fun decided(review: BackofficeReview) {
        val key = review.applicationId.toHexString()
        send(
            ApplicationTopics.DECIDED,
            key,
            ApplicationDecidedEvent(
                applicationId = key,
                status = review.status,
                approvalHistory = review.approvalHistory.toEvent(),
                updatedAt = review.updatedAt,
            ),
        )
    }

    private fun send(topic: String, key: String, event: Any) {
        kafkaTemplate.send(topic, key, event).get(10, TimeUnit.SECONDS)
    }
}
