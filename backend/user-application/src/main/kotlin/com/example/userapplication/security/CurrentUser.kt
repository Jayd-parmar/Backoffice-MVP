package com.example.userapplication.security

import com.example.userapplication.model.User
import com.example.userapplication.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class CurrentUser(
    private val userRepository: UserRepository,
) {
    fun require(): User {
        val userId = SecurityContextHolder.getContext().authentication?.principal as? String
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
        }
        if (!user.active) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User is inactive")
        }
        return user
    }
}
