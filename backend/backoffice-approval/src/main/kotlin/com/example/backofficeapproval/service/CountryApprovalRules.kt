package com.example.backofficeapproval.service

import com.example.backofficeapproval.model.DocumentCode
import com.example.backofficeapproval.model.DocumentVerification
import com.example.backofficeapproval.model.VerificationGroup

object CountryApprovalRules {
    fun forCountry(destinationCountry: String): List<VerificationGroup> {
        val sequence = when (destinationCountry) {
            "INDIA" -> listOf(
                "AADHAAR_PAN" to listOf(DocumentCode.AADHAAR, DocumentCode.PAN),
                "VOTER_DRIVING" to listOf(DocumentCode.VOTER_ID, DocumentCode.DRIVING_LICENSE),
                "PASSPORT" to listOf(DocumentCode.PASSPORT),
            )
            "UK" -> listOf(
                "AADHAAR_PAN" to listOf(DocumentCode.AADHAAR, DocumentCode.PAN),
                "PASSPORT" to listOf(DocumentCode.PASSPORT),
                "VOTER_DRIVING" to listOf(DocumentCode.VOTER_ID, DocumentCode.DRIVING_LICENSE),
            )
            "US" -> listOf(
                "PASSPORT" to listOf(DocumentCode.PASSPORT),
                "DRIVING" to listOf(DocumentCode.DRIVING_LICENSE),
                "AADHAAR_PAN" to listOf(DocumentCode.AADHAAR, DocumentCode.PAN),
            )
            "NEPAL" -> listOf(
                "PASSPORT" to listOf(DocumentCode.PASSPORT),
            )
            else -> throw IllegalArgumentException("Destination country is not supported")
        }
        return sequence.map { (code, documents) ->
            VerificationGroup(
                code = code,
                documents = documents.map { DocumentVerification(code = it) },
            )
        }
    }
}
