package com.banking.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO for customer self-registration.
 */
public record RegisterRequest(

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be 2-100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100, message = "Last name must be 2-100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Please provide a valid 10-digit Indian mobile number")
        String phone,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must contain uppercase, lowercase, digit, and special character"
        )
        String password,

        @NotBlank(message = "Please confirm your password")
        String confirmPassword
) {}
