package com.banking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for adding a new beneficiary to a customer's saved payees.
 */
public record AddBeneficiaryRequest(

        @NotBlank(message = "Beneficiary name is required")
        @Size(min = 2, max = 100)
        String name,

        @NotBlank(message = "Account number is required")
        String accountNumber,

        @Size(max = 100)
        String bankName,

        @Size(max = 20)
        String ifscCode,

        @Size(max = 50)
        String nickname
) {}
