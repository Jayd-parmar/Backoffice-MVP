package com.example.backofficemvp.Repository

import com.example.backofficemvp.Model.Application
import com.example.backofficemvp.Model.ApplicationStatus
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface ApplicationRepository : MongoRepository<Application, ObjectId> {
    fun findByCustomerId(customerId: ObjectId): Application?
    fun findByStatusOrderByUpdatedAtAsc(status: ApplicationStatus): List<Application>
}
