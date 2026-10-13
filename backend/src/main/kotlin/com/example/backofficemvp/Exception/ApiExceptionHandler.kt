package com.example.backofficemvp.Exception

import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class ApiExceptionHandler {

    data class ErrorResponse(val message: String)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
            .ifBlank { "Invalid request" }
        return ResponseEntity.badRequest().body(ErrorResponse(message))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun unreadableBody(): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(ErrorResponse("Invalid request body"))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun badCredentials(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(ex.message ?: "Invalid credentials"))
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun status(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val message = ex.reason ?: ex.statusCode.toString()
        return ResponseEntity.status(ex.statusCode).body(ErrorResponse(message))
    }

    @ExceptionHandler(DuplicateKeyException::class)
    fun duplicate(): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse("A record with this value already exists"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(ErrorResponse(ex.message ?: "Invalid request"))
    }
}