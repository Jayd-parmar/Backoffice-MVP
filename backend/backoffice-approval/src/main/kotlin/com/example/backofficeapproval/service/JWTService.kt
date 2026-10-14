package com.example.backofficeapproval.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date

@Service
class JWTService(
    @Value("\${jwt.secret}") private val jwtSecret: String,
) {
    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))

    private val accessTokenValidityInMS = 15L * 60 * 1000L
    val refreshTokenValidityInMS = 30 * 24 * 60 * 60 * 1000L

    private fun generateToken(
        userId: String,
        role: String,
        type: String,
        expiry: Long,
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userId: String, role: String): String =
        generateToken(userId, role, type = "access", expiry = accessTokenValidityInMS)

    fun generateRefreshToken(userId: String, role: String): String =
        generateToken(userId, role, type = "refresh", expiry = refreshTokenValidityInMS)

    fun validateAccessToken(token: String): Boolean {
        val claims = parseToken(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "access"
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseToken(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "refresh"
    }

    fun getUserIdFromJWT(token: String): String {
        val actualToken = parseToken(token) ?: throw IllegalArgumentException("Invalid JWT token")
        return actualToken.subject
    }

    fun getRoleFromJWT(token: String): String? {
        val claims = parseToken(token) ?: return null
        return claims["role"] as? String
    }

    fun parseToken(token: String): Claims? {
        val rawToken = if (token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else {
            token
        }
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}
