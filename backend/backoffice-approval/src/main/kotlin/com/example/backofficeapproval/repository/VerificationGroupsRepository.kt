package com.example.backofficeapproval.repository

import com.example.backofficeapproval.model.VerificationGroupsDocument
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface VerificationGroupsRepository : MongoRepository<VerificationGroupsDocument, ObjectId> {
    fun findByApplicationId(applicationId: ObjectId): VerificationGroupsDocument?
}
