package com.banking.dto.request;

import com.banking.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for employee KYC verification of a customer.
 */
public record KycVerificationRequest(

        @NotNull(message = "Customer ID is required")
        String customerId,

        @NotNull(message = "Document type is required")
        DocumentType documentType,

        @NotBlank(message = "Document number is required")
        String documentNumber,

        boolean verified,

        String rejectionReason
) {}
