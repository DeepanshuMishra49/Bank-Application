package com.banking.service;

import com.banking.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for transaction history and reporting.
 */
public interface TransactionService {

    Page<TransactionResponse> getTransactionHistory(UUID accountId, Pageable pageable);

    Page<TransactionResponse> getTransactionsByDateRange(
            UUID accountId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    List<TransactionResponse> getMiniStatement(UUID accountId);

    byte[] generateStatementPdf(UUID accountId, LocalDateTime from, LocalDateTime to) throws Exception;

    TransactionResponse findByReferenceNumber(String referenceNumber);

    Page<TransactionResponse> getAllTransactions(Pageable pageable);
}
