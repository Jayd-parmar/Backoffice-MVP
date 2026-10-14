package com.example.userapplication.security

import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthFilter: JWTAuthFilter,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/auth/**", "/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required")
                }
                it.accessDeniedHandler { _, response, _ ->
                    writeError(response, HttpServletResponse.SC_FORBIDDEN, "You do not have permission to perform this action")
                }
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    private fun writeError(response: HttpServletResponse, status: Int, message: String) {
        response.status = status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write("""{"message":"$message"}""")
    }

    @Bean
    fun jwtFilterRegistration(filter: JWTAuthFilter): FilterRegistrationBean<JWTAuthFilter> {
        return FilterRegistrationBean(filter).apply { isEnabled = false }
    }
}
