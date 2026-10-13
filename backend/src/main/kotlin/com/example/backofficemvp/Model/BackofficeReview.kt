package com.example.backofficemvp.Model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "backoffice")
data class BackofficeReview(
    @Id val id: ObjectId = ObjectId(),
    @Indexed(unique = true) val applicationId: ObjectId,
    val customerId: ObjectId,
    @Indexed val status: ApplicationStatus,
    val approvalHistory: ApprovalHistory = ApprovalHistory(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
