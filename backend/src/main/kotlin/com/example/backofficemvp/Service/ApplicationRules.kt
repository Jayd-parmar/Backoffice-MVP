package com.example.backofficemvp.Service

import com.example.backofficemvp.Model.Application
import java.math.BigDecimal
import java.time.LocalDate

object ApplicationRules {
    private val panPattern = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
    private val mobilePattern = Regex("^[0-9]{10}$")

    fun validate(application: Application) {
        val errors = mutableListOf<String>()
        val personal = application.personalDetails
        if (personal.firstName.isBlank()) errors += "personalDetails.firstName: First name is required"
        if (personal.lastName.isBlank()) errors += "personalDetails.lastName: Last name is required"
        if (personal.dateOfBirth.isAfter(LocalDate.now())) {
            errors += "personalDetails.dateOfBirth: Date of birth must be in the past"
        }
        if (personal.gender.isBlank()) errors += "personalDetails.gender: Gender is required"
        if (!panPattern.matches(personal.panNumber)) errors += "personalDetails.panNumber: PAN number is invalid"
        if (!personal.email.contains("@")) errors += "personalDetails.email: Email is invalid"
        if (!mobilePattern.matches(personal.mobile)) errors += "personalDetails.mobile: Mobile must be 10 digits"

        val forex = application.forexCardDetails
        if (forex.cardType.isBlank()) errors += "forexCardDetails.cardType: Card type is required"
        if (!forex.currency.matches(Regex("^[A-Z]{3}$"))) {
            errors += "forexCardDetails.currency: Currency must be a 3-letter code"
        }
        if (forex.amount <= BigDecimal.ZERO) errors += "forexCardDetails.amount: Amount must be positive"
        if (forex.purpose.isBlank()) errors += "forexCardDetails.purpose: Purpose is required"

        val address = application.addressDetails
        if (address.addressLine1.isBlank()) errors += "addressDetails.addressLine1: Address line 1 is required"
        if (address.city.isBlank()) errors += "addressDetails.city: City is required"
        if (address.state.isBlank()) errors += "addressDetails.state: State is required"
        if (address.country.isBlank()) errors += "addressDetails.country: Country is required"
        if (address.pincode.isBlank()) errors += "addressDetails.pincode: Pincode is required"

        val travel = application.travelDetails
        if (travel.destinationCountry.isBlank()) {
            errors += "travelDetails.destinationCountry: Destination country is required"
        }
        if (travel.purpose.isBlank()) errors += "travelDetails.purpose: Purpose is required"
        if (travel.returnDate.isBefore(travel.departureDate)) {
            errors += "travelDetails.returnDate: Return date must be on or after departure date"
        }

        if (errors.isNotEmpty()) {
            throw IllegalArgumentException(errors.joinToString("; "))
        }
    }

    fun maskPan(panNumber: String): String {
        if (panNumber.length <= 4) return panNumber
        return "*".repeat(panNumber.length - 4) + panNumber.takeLast(4)
    }
}
