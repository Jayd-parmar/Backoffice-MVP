package com.example.backofficemvp.Kafka

import com.example.backofficemvp.Model.ApprovalHistory
import com.example.backofficemvp.Model.BackofficeReview
import com.example.backofficemvp.Repository.ApplicationRepository
import com.example.backofficemvp.Repository.BackofficeRepository
import org.bson.types.ObjectId
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ApplicationEventConsumer(
    private val applicationRepository: ApplicationRepository,
    private val backofficeRepository: BackofficeRepository,
) {

    @KafkaListener(topics = [ApplicationTopics.CREATED], groupId = "backoffice-mvp")
    fun onCreated(event: ApplicationCreatedEvent) {
        val applicationId = ObjectId(event.applicationId)
        val existing = backofficeRepository.findByApplicationId(applicationId)
        backofficeRepository.save(
            BackofficeReview(
                id = existing?.id ?: ObjectId(),
                applicationId = applicationId,
                customerId = ObjectId(event.customerId),
                status = event.status,
                approvalHistory = ApprovalHistory(),
                createdAt = existing?.createdAt ?: event.createdAt,
                updatedAt = event.updatedAt,
            )
        )
    }

    @KafkaListener(topics = [ApplicationTopics.DECIDED], groupId = "backoffice-mvp")
    fun onDecided(event: ApplicationDecidedEvent) {
        val application = applicationRepository.findById(ObjectId(event.applicationId)).orElseThrow {
            IllegalStateException("Application not found")
        }
        applicationRepository.save(
            application.copy(
                status = event.status,
                approvalHistory = event.approvalHistory.toModel(),
                updatedAt = event.updatedAt,
            )
        )
    }
}
