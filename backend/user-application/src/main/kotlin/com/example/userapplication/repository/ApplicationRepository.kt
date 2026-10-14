package com.example.userapplication.repository

import com.example.userapplication.model.Application
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface ApplicationRepository : MongoRepository<Application, ObjectId> {
    fun findByCustomerId(customerId: ObjectId): Application?
}
