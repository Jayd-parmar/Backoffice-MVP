package com.example.userapplication.controller

import com.example.userapplication.dto.ApplicationRequest
import com.example.userapplication.dto.ApplicationResponse
import com.example.userapplication.security.CurrentUser
import com.example.userapplication.service.ApplicationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/applications")
class ApplicationController(
    private val applicationService: ApplicationService,
    private val currentUser: CurrentUser,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    fun create(@Valid @RequestBody body: ApplicationRequest): ApplicationResponse {
        return applicationService.create(currentUser.require(), body)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    fun get(@PathVariable id: String): ApplicationResponse {
        return applicationService.get(currentUser.require(), id)
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody body: ApplicationRequest,
    ): ApplicationResponse {
        return applicationService.update(currentUser.require(), id, body)
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('CUSTOMER')")
    fun submit(@PathVariable id: String): ApplicationResponse {
        return applicationService.submit(currentUser.require(), id)
    }
}
