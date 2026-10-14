package com.example.userapplication.repository

import com.example.userapplication.model.VerificationGroupsDocument
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface VerificationGroupsRepository : MongoRepository<VerificationGroupsDocument, ObjectId> {
    fun findByApplicationId(applicationId: ObjectId): VerificationGroupsDocument?
}
