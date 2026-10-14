package com.example.userapplication.service

import com.example.userapplication.dto.ApplicationRequest
import com.example.userapplication.dto.DocumentVerificationResponse
import com.example.userapplication.dto.VerificationGroupResponse
import com.example.userapplication.model.DocumentCode
import com.example.userapplication.model.VerificationGroup
import com.example.userapplication.dto.ApplicationResponse
import com.example.userapplication.dto.ApprovalDecisionResponse
import com.example.userapplication.dto.ApprovalResponse
import com.example.userapplication.model.AddressDetails
import com.example.userapplication.model.Application
import com.example.userapplication.model.ApplicationStatus
import com.example.userapplication.model.ApprovalDecision
import com.example.userapplication.model.ApprovalHistory
import com.example.userapplication.model.ForexCardDetails
import com.example.userapplication.model.PersonalDetails
import com.example.userapplication.model.TravelDetails
import org.bson.types.ObjectId
import java.time.Instant

fun ApplicationRequest.toApplication(
    id: ObjectId = ObjectId(),
    customerId: ObjectId,
    status: ApplicationStatus,
    approvalHistory: ApprovalHistory,
    createdAt: Instant,
    updatedAt: Instant,
): Application {
    return Application(
        id = id,
        customerId = customerId,
        status = status,
        personalDetails = PersonalDetails(
            firstName = personalDetails.firstName.trim(),
            lastName = personalDetails.lastName.trim(),
            dateOfBirth = personalDetails.dateOfBirth,
            gender = personalDetails.gender.trim().uppercase(),
            panNumber = personalDetails.panNumber.trim().uppercase(),
            aadhaarNumber = personalDetails.aadhaarNumber.trim(),
            passportNumber = personalDetails.passportNumber.trim().uppercase(),
            voterId = personalDetails.voterId.trim().uppercase(),
            drivingLicense = personalDetails.drivingLicense.trim().uppercase(),
            email = personalDetails.email.trim().lowercase(),
            mobile = personalDetails.mobile.trim(),
        ),
        forexCardDetails = ForexCardDetails(
            cardType = forexCardDetails.cardType.trim().uppercase(),
            currency = forexCardDetails.currency.trim().uppercase(),
            amount = forexCardDetails.amount,
            purpose = forexCardDetails.purpose.trim().uppercase(),
        ),
        addressDetails = AddressDetails(
            addressLine1 = addressDetails.addressLine1.trim(),
            city = addressDetails.city.trim(),
            state = addressDetails.state.trim(),
            country = addressDetails.country.trim(),
            pincode = addressDetails.pincode.trim(),
        ),
        travelDetails = TravelDetails(
            destinationCountry = travelDetails.destinationCountry.trim().uppercase(),
            departureDate = travelDetails.departureDate,
            returnDate = travelDetails.returnDate,
            purpose = travelDetails.purpose.trim().uppercase(),
        ),
        approvalHistory = approvalHistory,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Application.toResponse(): ApplicationResponse {
    return ApplicationResponse(
        id = id.toHexString(),
        customerId = customerId.toHexString(),
        status = status,
        personalDetails = personalDetails,
        forexCardDetails = forexCardDetails,
        addressDetails = addressDetails,
        travelDetails = travelDetails,
        approvalHistory = ApprovalResponse(
            approver1 = approvalHistory.approver1.toResponse(),
            approver2 = approvalHistory.approver2.toResponse(),
            approver3 = approvalHistory.approver3.toResponse(),
        ),
        verificationGroups = verificationGroups.map { it.toResponse() },
        currentGroupIndex = currentGroupIndex,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

private fun ApprovalDecision.toResponse(): ApprovalDecisionResponse {
    return ApprovalDecisionResponse(
        userId = userId?.toHexString(),
        action = action,
        comment = comment,
        actionAt = actionAt,
    )
}

private fun VerificationGroup.toResponse(): VerificationGroupResponse {
    return VerificationGroupResponse(
        code = code,
        label = groupLabel(code),
        documents = documents.map { document ->
            DocumentVerificationResponse(
                code = document.code,
                label = documentLabel(document.code),
                verified = document.verified,
            )
        },
    )
}

fun documentLabel(code: DocumentCode): String {
    return when (code) {
        DocumentCode.AADHAAR -> "Aadhaar"
        DocumentCode.PAN -> "PAN"
        DocumentCode.PASSPORT -> "Passport"
        DocumentCode.VOTER_ID -> "Voter ID"
        DocumentCode.DRIVING_LICENSE -> "Driving licence"
    }
}

fun groupLabel(code: String): String {
    return when (code) {
        "AADHAAR_PAN" -> "Aadhaar and PAN"
        "VOTER_DRIVING" -> "Voter ID and driving licence"
        "PASSPORT" -> "Passport"
        "DRIVING" -> "Driving licence"
        else -> code
    }
}
