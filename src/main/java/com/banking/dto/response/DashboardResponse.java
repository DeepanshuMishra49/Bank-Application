package com.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Response DTO for admin and customer dashboard analytics.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DashboardResponse(
        // Admin stats
        long totalCustomers,
        long pendingApprovals,
        long totalAccounts,
        long activeAccounts,
        long frozenAccounts,
        long totalTransactionsToday,
        BigDecimal totalDepositsToday,
        BigDecimal totalAssetsUnderManagement,
        long pendingKyc,

        // Customer stats
        BigDecimal totalBalance,
        long totalTransactions,
        BigDecimal lastTransactionAmount,
        String lastTransactionType
) {}
