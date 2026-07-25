package com.banking.dto.request;

import com.banking.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO for creating a new bank account for a customer.
 */
public record CreateAccountRequest(

        @NotNull(message = "Customer ID is required")
        String customerId,

        @NotNull(message = "Account type is required")
        AccountType accountType,

        @Positive(message = "Initial deposit must be positive")
        BigDecimal initialDeposit
) {}
