package com.example.backofficeapproval.service

import com.example.backofficeapproval.model.RefreshToken
import com.example.backofficeapproval.model.Role
import com.example.backofficeapproval.model.User
import com.example.backofficeapproval.repository.RefreshTokenRepository
import com.example.backofficeapproval.repository.UserRepository
import com.example.backofficeapproval.security.HashEncoder
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val jwtService: JWTService,
    private val userRepository: UserRepository,
    private val passwordEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    data class PairToken(
        val accessToken: String,
        val refreshToken: String,
    )

    fun register(email: String, password: String, role: Role, name: String, phone: String?) {
        if (role != Role.APPROVER_1 && role != Role.APPROVER_2 && role != Role.APPROVER_3) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only approvers can register here")
        }
        val normalizedEmail = email.trim().lowercase()
        if (userRepository.findByEmail(normalizedEmail) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered")
        }
        val now = Instant.now()
        userRepository.save(
            User(
                email = normalizedEmail,
                hashedPassword = passwordEncoder.encode(password),
                role = role,
                name = name.trim(),
                phone = phone?.trim()?.takeIf { it.isNotEmpty() },
                active = true,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    fun login(email: String, password: String): PairToken {
        val user = userRepository.findByEmail(email.trim().lowercase())
            ?: throw BadCredentialsException("Invalid credentials")

        if (!passwordEncoder.matches(password, user.hashedPassword)) {
            throw BadCredentialsException("Invalid credentials")
        }
        if (!user.active) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User is inactive")
        }

        return issueTokens(user)
    }

    fun storeRefreshToken(userId: ObjectId, rawToken: String) {
        val hashedToken = hashToken(rawToken)
        val expiresInMs = jwtService.refreshTokenValidityInMS
        val expiresAt = Instant.now().plusMillis(expiresInMs)
        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                hashedToken = hashedToken,
                expiresAt = expiresAt,
            )
        )
    }

    @Transactional
    fun refreshToken(refreshToken: String): PairToken {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw BadCredentialsException("Invalid refresh token")
        }
        val userId = jwtService.getUserIdFromJWT(refreshToken)
        val user = userRepository.findById(userId).orElseThrow {
            BadCredentialsException("User not found")
        }
        if (!user.active) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User is inactive")
        }
        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndHashedToken(user.id, hashed)
            ?: throw BadCredentialsException("refresh token not found")
        refreshTokenRepository.deleteByUserIdAndHashedToken(user.id, hashed)

        return issueTokens(user)
    }

    fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val digestBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(digestBytes)
    }

    private fun issueTokens(user: User): PairToken {
        val userId = user.id.toHexString()
        val role = user.role.name
        val newAccessToken = jwtService.generateAccessToken(userId, role)
        val newRefreshToken = jwtService.generateRefreshToken(userId, role)
        storeRefreshToken(user.id, newRefreshToken)
        return PairToken(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
        )
    }
}
