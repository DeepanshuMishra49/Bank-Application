package com.banking.service.impl;

import com.banking.dto.response.DashboardResponse;
import com.banking.entity.Transaction;
import com.banking.enums.AccountStatus;
import com.banking.repository.*;
import com.banking.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link DashboardService} aggregating statistics for dashboards.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KycRepository kycRepository;

    @Override
    public DashboardResponse getAdminDashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        long totalCustomers = customerRepository.count();
        long pendingApprovals = customerRepository.countByApproved(false);
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.countByStatus(AccountStatus.ACTIVE);
        long frozenAccounts = accountRepository.countByStatus(AccountStatus.FROZEN);
        long txnToday = transactionRepository.countByCreatedAtAfter(startOfDay);
        long pendingKyc = kycRepository.countByVerified(false);

        BigDecimal totalDepositsToday = transactionRepository.sumDepositsBetween(startOfDay, endOfDay);
        if (totalDepositsToday == null) totalDepositsToday = BigDecimal.ZERO;

        BigDecimal totalAssets = accountRepository.sumTotalActiveBalance();
        if (totalAssets == null) totalAssets = BigDecimal.ZERO;

        return new DashboardResponse(
                totalCustomers,
                pendingApprovals,
                totalAccounts,
                activeAccounts,
                frozenAccounts,
                txnToday,
                totalDepositsToday,
                totalAssets,
                pendingKyc,
                // Customer fields (null for admin)
                null, 0, null, null
        );
    }

    @Override
    public DashboardResponse getEmployeeDashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        long pendingApprovals = customerRepository.countByApproved(false);
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.countByStatus(AccountStatus.ACTIVE);
        long frozenAccounts = accountRepository.countByStatus(AccountStatus.FROZEN);
        long txnToday = transactionRepository.countByCreatedAtAfter(startOfDay);
        long pendingKyc = kycRepository.countByVerified(false);

        return new DashboardResponse(
                0, pendingApprovals, totalAccounts, activeAccounts, frozenAccounts,
                txnToday, null, null, pendingKyc,
                null, 0, null, null
        );
    }

    @Override
    public DashboardResponse getCustomerDashboard(UUID customerId) {
        List<com.banking.entity.Account> accounts = accountRepository.findByCustomerId(customerId);

        BigDecimal totalBalance = accounts.stream()
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .map(com.banking.entity.Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTransactions = transactionRepository.countByCustomerId(customerId);

        // Get last transaction from first account if available
        BigDecimal lastTxnAmount = null;
        String lastTxnType = null;
        if (!accounts.isEmpty()) {
            List<Transaction> recent = transactionRepository
                    .findTop10ByAccountIdOrderByCreatedAtDesc(accounts.get(0).getId());
            if (!recent.isEmpty()) {
                Transaction last = recent.get(0);
                lastTxnAmount = last.getAmount();
                lastTxnType = last.getTransactionType().name();
            }
        }

        return new DashboardResponse(
                0, 0, 0, 0, 0, 0, null, null, 0,
                totalBalance,
                totalTransactions,
                lastTxnAmount,
                lastTxnType
        );
    }
}
