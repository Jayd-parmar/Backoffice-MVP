package com.example.backofficemvp.Repository

import com.example.backofficemvp.Model.ApplicationStatus
import com.example.backofficemvp.Model.BackofficeReview
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface BackofficeRepository : MongoRepository<BackofficeReview, ObjectId> {
    fun findByApplicationId(applicationId: ObjectId): BackofficeReview?
    fun findByStatusOrderByUpdatedAtAsc(status: ApplicationStatus): List<BackofficeReview>
}
