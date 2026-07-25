package com.banking.service.impl;

import com.banking.dto.response.AccountResponse;
import com.banking.dto.response.TransactionResponse;
import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.exception.AccountNotFoundException;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.service.TransactionService;
import com.banking.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link TransactionService} for transaction history and reporting.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final PdfGenerator pdfGenerator;

    @Override
    public Page<TransactionResponse> getTransactionHistory(UUID accountId, Pageable pageable) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<TransactionResponse> getTransactionsByDateRange(
            UUID accountId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return transactionRepository.findByAccountIdAndDateRange(accountId, from, to, pageable)
                .map(this::toResponse);
    }

    @Override
    public List<TransactionResponse> getMiniStatement(UUID accountId) {
        return transactionRepository.findTop10ByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] generateStatementPdf(UUID accountId, LocalDateTime from, LocalDateTime to) throws Exception {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("id", accountId.toString()));

        List<TransactionResponse> transactions = transactionRepository
                .findByAccountIdAndDateRange(accountId, from, to, Pageable.unpaged())
                .getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        String customerName = account.getCustomer() != null
                ? account.getCustomer().getFullName()
                : "Account Holder";

        log.info("Generating PDF statement for account: {} ({} transactions)", account.getAccountNumber(), transactions.size());
        return pdfGenerator.generateStatement(account.getAccountNumber(), customerName, from, to, transactions);
    }

    @Override
    public TransactionResponse findByReferenceNumber(String referenceNumber) {
        return transactionRepository.findByReferenceNumber(referenceNumber)
                .map(this::toResponse)
                .orElseThrow(() -> new AccountNotFoundException("referenceNumber", referenceNumber));
    }

    @Override
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::toResponse);
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getReferenceNumber(),
                t.getAccount() != null ? t.getAccount().getAccountNumber() : null,
                t.getTransactionType(),
                t.getAmount(),
                t.getBalanceBefore(),
                t.getBalanceAfter(),
                t.getDescription(),
                t.getToAccountNumber(),
                t.getFromAccountNumber(),
                t.getStatus(),
                t.getChannel(),
                t.getCreatedAt()
        );
    }
}
