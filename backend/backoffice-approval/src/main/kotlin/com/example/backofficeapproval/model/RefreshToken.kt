package com.example.backofficeapproval.model

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("refresh_tokens")
data class RefreshToken(
    val userId: ObjectId,
    val hashedToken: String,
    val createsAt: Instant = Instant.now(),
    @Indexed(expireAfter = "0s")
    val expiresAt: Instant,
)
