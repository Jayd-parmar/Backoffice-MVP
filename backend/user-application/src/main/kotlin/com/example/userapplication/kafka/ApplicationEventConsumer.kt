package com.example.userapplication.kafka

import com.example.userapplication.model.ApplicationStatus
import com.example.userapplication.repository.ApplicationRepository
import org.bson.types.ObjectId
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ApplicationEventConsumer(
    private val applicationRepository: ApplicationRepository,
) {

    @KafkaListener(topics = [ApplicationTopics.DECIDED], groupId = "user-application")
    fun onDecided(event: ApplicationDecidedEvent) {
        if (event.status != ApplicationStatus.PENDING_APPROVAL &&
            event.status != ApplicationStatus.APPROVED &&
            event.status != ApplicationStatus.REJECTED
        ) {
            return
        }
        val application = applicationRepository.findById(ObjectId(event.applicationId)).orElseThrow {
            IllegalStateException("Application not found")
        }
        if (!event.updatedAt.isAfter(application.updatedAt)) {
            return
        }
        applicationRepository.save(
            application.copy(
                status = event.status,
                approvalHistory = event.approvalHistory.toModel(),
                verificationGroups = event.verificationGroups,
                currentGroupIndex = event.currentGroupIndex,
                updatedAt = event.updatedAt,
            )
        )
    }
}
