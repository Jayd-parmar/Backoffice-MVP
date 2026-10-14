package com.example.userapplication.security

import com.example.userapplication.model.Role
import com.example.userapplication.service.JWTService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JWTAuthFilter(
    private val jwtService: JWTService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authorizationHeader = request.getHeader("Authorization")
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            if (jwtService.validateAccessToken(authorizationHeader)) {
                val userId = jwtService.getUserIdFromJWT(authorizationHeader)
                val role = jwtService.getRoleFromJWT(authorizationHeader)
                if (role != null && Role.entries.any { it.name == role }) {
                    val auth = UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_$role")),
                    )
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}
