package com.banking.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO for admin/employee creating a new customer profile.
 */
public record CreateCustomerRequest(

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100)
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
        String phone,

        @NotBlank(message = "Password is required")
        @Size(min = 8)
        String password,

        String gender,
        String occupation,
        Double annualIncome,
        String street,
        String city,
        String state,
        String pinCode
) {}
