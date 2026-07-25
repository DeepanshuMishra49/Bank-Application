package com.banking.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO for deposit and withdrawal operations on an account.
 */
public record DepositWithdrawRequest(

        @NotBlank(message = "Account number is required")
        String accountNumber,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Amount must be at least ₹1.00")
        @DecimalMax(value = "1000000.00", message = "Amount cannot exceed ₹10,00,000 per transaction")
        BigDecimal amount,

        @Size(max = 200)
        String description
) {}
