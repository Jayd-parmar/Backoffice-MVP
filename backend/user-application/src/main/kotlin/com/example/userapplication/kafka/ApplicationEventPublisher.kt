package com.example.userapplication.kafka

import com.example.userapplication.model.Application
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class ApplicationEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {

    fun submitted(application: Application) {
        val key = application.id.toHexString()
        send(
            ApplicationTopics.SUBMITTED,
            key,
            ApplicationSubmittedEvent(
                applicationId = key,
                customerId = application.customerId.toHexString(),
                status = application.status,
                personalDetails = application.personalDetails,
                forexCardDetails = application.forexCardDetails,
                addressDetails = application.addressDetails,
                travelDetails = application.travelDetails,
                createdAt = application.createdAt,
                updatedAt = application.updatedAt,
            ),
        )
    }

    private fun send(topic: String, key: String, event: Any) {
        kafkaTemplate.send(topic, key, event).get(10, TimeUnit.SECONDS)
    }
}
