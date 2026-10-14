package com.example.backofficeapproval.repository

import com.example.backofficeapproval.model.ApplicationStatus
import com.example.backofficeapproval.model.BackofficeReview
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface BackofficeRepository : MongoRepository<BackofficeReview, ObjectId> {
    fun findByApplicationId(applicationId: ObjectId): BackofficeReview?
    fun findByStatusAndCurrentGroupIndexOrderByUpdatedAtAsc(
        status: ApplicationStatus,
        currentGroupIndex: Int,
    ): List<BackofficeReview>
}
