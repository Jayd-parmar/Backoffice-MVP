package com.example.backofficeapproval.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

enum class ApplicationStatus {
    DRAFT,
    PENDING_APPROVAL,
    PENDING_APPROVER_1,
    PENDING_APPROVER_2,
    APPROVED,
    REJECTED,
}

enum class DocumentCode {
    AADHAAR,
    PAN,
    PASSPORT,
    VOTER_ID,
    DRIVING_LICENSE,
}

enum class ApprovalAction {
    APPROVED,
    REJECTED,
}

data class PersonalDetails(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val gender: String,
    val panNumber: String,
    val aadhaarNumber: String = "",
    val passportNumber: String = "",
    val voterId: String = "",
    val drivingLicense: String = "",
    val email: String,
    val mobile: String,
)

data class ForexCardDetails(
    val cardType: String,
    val currency: String,
    val amount: BigDecimal,
    val purpose: String,
)

data class AddressDetails(
    val addressLine1: String,
    val city: String,
    val state: String,
    val country: String,
    val pincode: String,
)

data class TravelDetails(
    val destinationCountry: String,
    val departureDate: LocalDate,
    val returnDate: LocalDate,
    val purpose: String,
)

data class ApprovalDecision(
    val userId: ObjectId? = null,
    val action: ApprovalAction? = null,
    val comment: String? = null,
    val actionAt: Instant? = null,
)

data class ApprovalHistory(
    val approver1: ApprovalDecision = ApprovalDecision(),
    val approver2: ApprovalDecision = ApprovalDecision(),
    val approver3: ApprovalDecision = ApprovalDecision(),
)

data class DocumentVerification(
    val code: DocumentCode,
    val verified: Boolean = false,
    val verifiedBy: ObjectId? = null,
    val verifiedAt: Instant? = null,
)

data class VerificationGroup(
    val code: String,
    val documents: List<DocumentVerification> = emptyList(),
)

@Document(collection = "reviews")
data class BackofficeReview(
    @Id val id: ObjectId = ObjectId(),
    @Indexed(unique = true) val applicationId: ObjectId,
    val customerId: ObjectId,
    @Indexed val status: ApplicationStatus,
    val personalDetails: PersonalDetails,
    val forexCardDetails: ForexCardDetails,
    val addressDetails: AddressDetails,
    val travelDetails: TravelDetails,
    val approvalHistory: ApprovalHistory = ApprovalHistory(),
    val verificationGroups: List<VerificationGroup> = emptyList(),
    val currentGroupIndex: Int = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
