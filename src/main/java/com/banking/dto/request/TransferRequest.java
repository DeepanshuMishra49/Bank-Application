package com.banking.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO for fund transfer between two accounts.
 */
public record TransferRequest(

        @NotBlank(message = "Source account number is required")
        String fromAccountNumber,

        @NotBlank(message = "Destination account number is required")
        String toAccountNumber,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Transfer amount must be at least ₹1.00")
        @DecimalMax(value = "200000.00", message = "Transfer amount cannot exceed ₹2,00,000 per transaction")
        BigDecimal amount,

        @Size(max = 200, message = "Description cannot exceed 200 characters")
        String description
) {}
