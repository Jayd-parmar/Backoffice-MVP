package com.example.backofficemvp.Model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "users")
data class User(
    @Id val id: ObjectId = ObjectId(),
    @Indexed(unique = true) val email: String,
    val hashedPassword: String?,
    val role: Role = Role.CUSTOMER,
    val name: String = "",
    val phone: String? = null,
    val active: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
