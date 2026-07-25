package com.banking.repository;

import com.banking.entity.Transaction;
import com.banking.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Transaction} entity with date-range and analytics queries.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    List<Transaction> findTop10ByAccountIdOrderByCreatedAtDesc(UUID accountId);

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountIdAndDateRange(
            @Param("accountId") UUID accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.transactionType IN ('TRANSFER_OUT', 'WITHDRAWAL', 'DEBIT') " +
           "AND t.createdAt >= :since")
    BigDecimal sumDebitsAfter(@Param("accountId") UUID accountId, @Param("since") LocalDateTime since);

    long countByCreatedAtAfter(LocalDateTime after);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.transactionType = 'DEPOSIT' " +
           "AND t.createdAt BETWEEN :start AND :end")
    BigDecimal sumDepositsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") UUID customerId);
}
