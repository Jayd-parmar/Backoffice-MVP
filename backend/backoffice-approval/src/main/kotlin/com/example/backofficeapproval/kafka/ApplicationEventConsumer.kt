package com.example.backofficeapproval.kafka

import com.example.backofficeapproval.model.ApplicationStatus
import com.example.backofficeapproval.service.CountryApprovalRules
import com.example.backofficeapproval.model.ApprovalHistory
import com.example.backofficeapproval.model.BackofficeReview
import com.example.backofficeapproval.repository.BackofficeRepository
import org.bson.types.ObjectId
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ApplicationEventConsumer(
    private val backofficeRepository: BackofficeRepository,
) {

    @KafkaListener(topics = [ApplicationTopics.SUBMITTED], groupId = "backoffice-approval")
    fun onSubmitted(event: ApplicationSubmittedEvent) {
        val applicationId = ObjectId(event.applicationId)
        val existing = backofficeRepository.findByApplicationId(applicationId)
        if (existing != null) {
            if (!event.updatedAt.isAfter(existing.updatedAt)) {
                return
            }
            if (existing.status != ApplicationStatus.REJECTED) {
                return
            }
        }
        backofficeRepository.save(
            BackofficeReview(
                id = existing?.id ?: ObjectId(),
                applicationId = applicationId,
                customerId = ObjectId(event.customerId),
                status = ApplicationStatus.PENDING_APPROVAL,
                personalDetails = event.personalDetails,
                forexCardDetails = event.forexCardDetails,
                addressDetails = event.addressDetails,
                travelDetails = event.travelDetails,
                approvalHistory = ApprovalHistory(),
                verificationGroups = CountryApprovalRules.forCountry(event.travelDetails.destinationCountry),
                currentGroupIndex = 0,
                createdAt = existing?.createdAt ?: event.createdAt,
                updatedAt = event.updatedAt,
            )
        )
    }
}
