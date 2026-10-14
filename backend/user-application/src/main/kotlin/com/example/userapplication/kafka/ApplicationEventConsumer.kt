package com.example.userapplication.kafka

import com.example.userapplication.model.ApplicationStatus
import com.example.userapplication.repository.ApplicationRepository
import com.example.userapplication.service.VerificationGroupService
import org.bson.types.ObjectId
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ApplicationEventConsumer(
    private val applicationRepository: ApplicationRepository,
    private val verificationGroupService: VerificationGroupService,
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
        verificationGroupService.save(application.id, event.verificationGroups)
        applicationRepository.save(
            application.copy(
                status = event.status,
                approvalHistory = event.approvalHistory.toModel(),
                currentGroupIndex = event.currentGroupIndex,
                updatedAt = event.updatedAt,
            )
        )
    }
}
