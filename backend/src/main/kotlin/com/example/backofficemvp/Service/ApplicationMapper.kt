package com.example.backofficemvp.Service

import com.example.backofficemvp.Dto.ApplicationRequest
import com.example.backofficemvp.Dto.ApplicationResponse
import com.example.backofficemvp.Dto.ApprovalDecisionResponse
import com.example.backofficemvp.Dto.ApprovalResponse
import com.example.backofficemvp.Model.AddressDetails
import com.example.backofficemvp.Model.Application
import com.example.backofficemvp.Model.ApplicationStatus
import com.example.backofficemvp.Model.ApprovalHistory
import com.example.backofficemvp.Model.ApprovalDecision
import com.example.backofficemvp.Model.BackofficeReview
import com.example.backofficemvp.Model.ForexCardDetails
import com.example.backofficemvp.Model.PersonalDetails
import com.example.backofficemvp.Model.TravelDetails
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
            destinationCountry = travelDetails.destinationCountry.trim(),
            departureDate = travelDetails.departureDate,
            returnDate = travelDetails.returnDate,
            purpose = travelDetails.purpose.trim().uppercase(),
        ),
        approvalHistory = approvalHistory,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Application.toResponse(maskPanNumber: Boolean): ApplicationResponse {
    val personal = if (maskPanNumber) {
        personalDetails.copy(panNumber = ApplicationRules.maskPan(personalDetails.panNumber))
    } else {
        personalDetails
    }
    return ApplicationResponse(
        id = id.toHexString(),
        customerId = customerId.toHexString(),
        status = status,
        personalDetails = personal,
        forexCardDetails = forexCardDetails,
        addressDetails = addressDetails,
        travelDetails = travelDetails,
        approvalHistory = ApprovalResponse(
            approver1 = approvalHistory.approver1.toResponse(),
            approver2 = approvalHistory.approver2.toResponse(),
        ),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun toApprovalResponse(
    review: BackofficeReview,
    application: Application,
    maskPanNumber: Boolean,
): ApplicationResponse {
    val personal = if (maskPanNumber) {
        application.personalDetails.copy(panNumber = ApplicationRules.maskPan(application.personalDetails.panNumber))
    } else {
        application.personalDetails
    }
    return ApplicationResponse(
        id = application.id.toHexString(),
        customerId = application.customerId.toHexString(),
        status = review.status,
        personalDetails = personal,
        forexCardDetails = application.forexCardDetails,
        addressDetails = application.addressDetails,
        travelDetails = application.travelDetails,
        approvalHistory = ApprovalResponse(
            approver1 = review.approvalHistory.approver1.toResponse(),
            approver2 = review.approvalHistory.approver2.toResponse(),
        ),
        createdAt = review.createdAt,
        updatedAt = review.updatedAt,
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
