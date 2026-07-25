package com.banking.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO for updating customer profile information.
 */
public record UpdateProfileRequest(

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100)
        String lastName,

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
        String phone,

        String gender,
        String occupation,
        Double annualIncome,
        String street,
        String city,
        String state,
        String pinCode
) {}
