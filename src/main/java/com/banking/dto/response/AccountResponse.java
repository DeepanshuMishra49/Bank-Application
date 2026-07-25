package com.banking.dto.response;

import com.banking.enums.AccountStatus;
import com.banking.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for bank account information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccountResponse(
        UUID id,
        String accountNumber,
        String customerName,
        String customerId,
        AccountType accountType,
        BigDecimal balance,
        AccountStatus status,
        BigDecimal dailyTransferLimit,
        BigDecimal transferredToday,
        BigDecimal minimumBalance,
        String ifscCode,
        String branchName,
        LocalDateTime createdAt
) {}
