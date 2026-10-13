package com.example.backofficemvp.Repository

import com.example.backofficemvp.Model.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository: MongoRepository<User, String> {
    fun findByEmail(email: String): User?
}