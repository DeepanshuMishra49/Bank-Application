package com.banking.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for the login form submission.
 */
public record LoginRequest(

        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,

        @NotBlank(message = "Password is required")
        String password,

        boolean rememberMe
) {}
