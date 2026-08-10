package com.banking.repository;

import com.banking.entity.Account;
import com.banking.enums.AccountStatus;
import com.banking.enums.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Account} entities.
 *
 * <p>BUG FIX: added {@link #resetAllDailyTransferLimits()} — a single bulk UPDATE
 * used by {@link com.banking.scheduler.DailyTasksScheduler} to reset the
 * {@code transferred_today} column to zero at midnight. Without this method the
 * daily transfer limit could never reset.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(UUID customerId);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByCustomerIdAndAccountType(UUID customerId, AccountType accountType);

    @Query("SELECT a FROM Account a WHERE " +
           "LOWER(a.accountNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.customer.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.customer.lastName)  LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Account> searchAccounts(String search, Pageable pageable);

    // ── BUG FIX: bulk-reset daily transfer counters ───────────────────────────
    // Returns the number of rows updated (useful for logging in the scheduler).
    // @Modifying triggers a flush and clears the persistence-context cache so
    // subsequent reads see the fresh zero values.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Account a SET a.transferredToday = 0")
    int resetAllDailyTransferLimits();
    // ─────────────────────────────────────────────────────────────────────────
}
