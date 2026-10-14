package com.example.userapplication.repository

import com.example.userapplication.model.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String): User?
}
