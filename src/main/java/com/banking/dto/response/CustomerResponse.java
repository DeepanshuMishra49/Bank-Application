package com.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for customer profile information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomerResponse(
        UUID id,
        String customerId,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String gender,
        String occupation,
        Double annualIncome,
        boolean approved,
        String approvedBy,
        String street,
        String city,
        String state,
        String pinCode,
        String country,
        boolean kycVerified,
        int totalAccounts,
        LocalDateTime createdAt
) {}
