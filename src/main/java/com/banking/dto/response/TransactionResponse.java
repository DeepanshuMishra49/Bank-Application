package com.banking.dto.response;

import com.banking.enums.TransactionStatus;
import com.banking.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a single financial transaction.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponse(
        UUID id,
        String referenceNumber,
        String accountNumber,
        TransactionType transactionType,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String description,
        String toAccountNumber,
        String fromAccountNumber,
        TransactionStatus status,
        String channel,
        LocalDateTime createdAt
) {}
