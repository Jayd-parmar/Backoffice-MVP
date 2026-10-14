package com.example.backofficeapproval.repository

import com.example.backofficeapproval.model.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String): User?
}
