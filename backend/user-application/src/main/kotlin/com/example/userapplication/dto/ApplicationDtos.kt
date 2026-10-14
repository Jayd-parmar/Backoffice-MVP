package com.example.userapplication.dto

import com.example.userapplication.model.AddressDetails
import com.example.userapplication.model.DocumentCode
import com.example.userapplication.model.ApplicationStatus
import com.example.userapplication.model.ApprovalAction
import com.example.userapplication.model.ForexCardDetails
import com.example.userapplication.model.PersonalDetails
import com.example.userapplication.model.TravelDetails
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class PersonalDetailsRequest(
    @field:NotBlank(message = "First name is required")
    @field:Size(max = 80, message = "First name must be at most 80 characters")
    val firstName: String,
    @field:NotBlank(message = "Last name is required")
    @field:Size(max = 80, message = "Last name must be at most 80 characters")
    val lastName: String,
    @field:NotNull(message = "Date of birth is required")
    @field:Past(message = "Date of birth must be in the past")
    val dateOfBirth: LocalDate,
    @field:NotBlank(message = "Gender is required")
    @field:Size(max = 20, message = "Gender must be at most 20 characters")
    val gender: String,
    @field:NotBlank(message = "PAN number is required")
    @field:Pattern(regexp = "^[A-Za-z]{5}[0-9]{4}[A-Za-z]$", message = "PAN number is invalid")
    val panNumber: String,
    @field:Size(max = 40, message = "Aadhaar number must be at most 40 characters")
    val aadhaarNumber: String = "",
    @field:Size(max = 40, message = "Passport number must be at most 40 characters")
    val passportNumber: String = "",
    @field:Size(max = 40, message = "Voter ID must be at most 40 characters")
    val voterId: String = "",
    @field:Size(max = 40, message = "Driving licence must be at most 40 characters")
    val drivingLicense: String = "",
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email is invalid")
    val email: String,
    @field:NotBlank(message = "Mobile is required")
    @field:Pattern(regexp = "^[0-9]{10}$", message = "Mobile must be 10 digits")
    val mobile: String,
)

data class ForexCardDetailsRequest(
    @field:NotBlank(message = "Card type is required")
    @field:Size(max = 40, message = "Card type must be at most 40 characters")
    val cardType: String,
    @field:NotBlank(message = "Currency is required")
    @field:Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a 3-letter code")
    val currency: String,
    @field:NotNull(message = "Amount is required")
    @field:Positive(message = "Amount must be positive")
    val amount: BigDecimal,
    @field:NotBlank(message = "Purpose is required")
    @field:Size(max = 40, message = "Purpose must be at most 40 characters")
    val purpose: String,
)

data class AddressDetailsRequest(
    @field:NotBlank(message = "Address line 1 is required")
    @field:Size(max = 200, message = "Address line 1 must be at most 200 characters")
    val addressLine1: String,
    @field:NotBlank(message = "City is required")
    @field:Size(max = 80, message = "City must be at most 80 characters")
    val city: String,
    @field:NotBlank(message = "State is required")
    @field:Size(max = 80, message = "State must be at most 80 characters")
    val state: String,
    @field:NotBlank(message = "Country is required")
    @field:Size(max = 80, message = "Country must be at most 80 characters")
    val country: String,
    @field:NotBlank(message = "Pincode is required")
    @field:Size(max = 12, message = "Pincode must be at most 12 characters")
    val pincode: String,
)

data class TravelDetailsRequest(
    @field:NotBlank(message = "Destination country is required")
    @field:Pattern(
        regexp = "(?i)INDIA|US|UK|NEPAL",
        message = "Destination must be India, the United States, the United Kingdom, or Nepal",
    )
    val destinationCountry: String,
    @field:NotNull(message = "Departure date is required")
    val departureDate: LocalDate,
    @field:NotNull(message = "Return date is required")
    val returnDate: LocalDate,
    @field:NotBlank(message = "Purpose is required")
    @field:Size(max = 40, message = "Purpose must be at most 40 characters")
    val purpose: String,
)

data class ApplicationRequest(
    @field:Valid
    @field:NotNull(message = "Personal details are required")
    val personalDetails: PersonalDetailsRequest,
    @field:Valid
    @field:NotNull(message = "Forex card details are required")
    val forexCardDetails: ForexCardDetailsRequest,
    @field:Valid
    @field:NotNull(message = "Address details are required")
    val addressDetails: AddressDetailsRequest,
    @field:Valid
    @field:NotNull(message = "Travel details are required")
    val travelDetails: TravelDetailsRequest,
)

data class ApprovalDecisionResponse(
    val userId: String?,
    val action: ApprovalAction?,
    val comment: String?,
    val actionAt: Instant?,
)

data class ApprovalResponse(
    val approver1: ApprovalDecisionResponse,
    val approver2: ApprovalDecisionResponse,
    val approver3: ApprovalDecisionResponse,
)

data class DocumentVerificationResponse(
    val code: DocumentCode,
    val label: String,
    val verified: Boolean,
)

data class VerificationGroupResponse(
    val code: String,
    val label: String,
    val documents: List<DocumentVerificationResponse>,
)

data class ApplicationResponse(
    val id: String,
    val customerId: String,
    val status: ApplicationStatus,
    val personalDetails: PersonalDetails,
    val forexCardDetails: ForexCardDetails,
    val addressDetails: AddressDetails,
    val travelDetails: TravelDetails,
    val approvalHistory: ApprovalResponse,
    val verificationGroups: List<VerificationGroupResponse> = emptyList(),
    val currentGroupIndex: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
)
