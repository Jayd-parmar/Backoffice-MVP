package com.example.backofficemvp.Security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class HashEncoder {
    private val encoder = BCryptPasswordEncoder()

    fun encode(raw: String): String? = encoder.encode(raw)

    fun matches(raw: String, encoded: String?): Boolean = encoder.matches(raw, encoded)
}