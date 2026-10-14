package com.example.backofficeapproval.service

object ApplicationRules {
    fun maskPan(panNumber: String): String {
        if (panNumber.length <= 4) return panNumber
        return "*".repeat(panNumber.length - 4) + panNumber.takeLast(4)
    }
}
