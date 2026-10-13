package com.example.backofficemvp.Controller

import com.example.backofficemvp.Model.Role
import com.example.backofficemvp.Service.AuthService
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/auth", "/auth"])
class AuthController(
    val authService: AuthService
) {

    data class RegisterRequest(
        @field:NotBlank(message = "Email is required")
        @field:Email(message = "Email is invalid")
        val email: String,
        @field:NotBlank(message = "Password is required")
        @field:Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
        val password: String,
        @field:NotNull(message = "Role is required")
        val role: Role,
        @field:NotBlank(message = "Name is required")
        @field:Size(max = 100, message = "Name must be at most 100 characters")
        val name: String,
        @field:Pattern(regexp = "^$|^[0-9+\\- ]{7,20}$", message = "Phone is invalid")
        val phone: String? = null,
    )

    data class AuthRequest(
        @field:NotBlank(message = "Email is required")
        @field:Email(message = "Email is invalid")
        val email: String,
        @field:NotBlank(message = "Password is required")
        @field:Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
        val password: String
    )

    data class RefreshRequest(
        @field:NotBlank(message = "Refresh token is required")
        val refreshToken: String
    )

    data class MessageResponse(
        val message: String
    )

    @PostMapping(value = ["/register"])
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody body: RegisterRequest): MessageResponse {
        authService.register(
            email = body.email,
            password = body.password,
            role = body.role,
            name = body.name,
            phone = body.phone,
        )
        return MessageResponse("Registered successfully")
    }

    @PostMapping(value = ["/login"])
    fun login(@Valid @RequestBody body: AuthRequest): AuthService.PairToken {
        return authService.login(body.email, body.password)
    }

    @PostMapping(value = ["/refresh"])
    fun refresh(@Valid @RequestBody body: RefreshRequest): AuthService.PairToken {
        return authService.refreshToken(refreshToken = body.refreshToken)
    }
}
