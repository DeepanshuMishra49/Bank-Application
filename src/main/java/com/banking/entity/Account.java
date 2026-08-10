package com.banking.entity;

import com.banking.enums.AccountStatus;
import com.banking.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Bank account entity managing balance, status, and transaction history.
 * Each account belongs to one customer and has a globally unique account number.
 *
 * <p>BUG FIX — optimistic locking: a {@code @Version} field was added.
 * Without it, two concurrent HTTP requests (e.g. a deposit and a transfer
 * hitting the same account at the same millisecond) would both read the same
 * balance, each compute their own new balance, and the second write would
 * silently overwrite the first — corrupting the balance.
 *
 * With {@code @Version}, the second write will throw
 * {@link org.springframework.orm.ObjectOptimisticLockingFailureException},
 * which the caller should catch and retry (or return HTTP 409 Conflict).
 * The corresponding DB column is added in V7__add_account_version.sql.
 */
@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_accounts_account_number", columnList = "account_number"),
        @Index(name = "idx_accounts_customer_id",   columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity {

    // ── BUG FIX: optimistic-locking version column ────────────────────────────
    // JPA will automatically increment this on every UPDATE and will throw
    // ObjectOptimisticLockingFailureException if another transaction already
    // changed the row since we read it, preventing the lost-update anomaly
    // on financial operations.
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
    // ─────────────────────────────────────────────────────────────────────────

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.PENDING_APPROVAL;

    @Column(name = "daily_transfer_limit", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal dailyTransferLimit = new BigDecimal("100000.00");

    @Column(name = "transferred_today", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal transferredToday = BigDecimal.ZERO;

    @Column(name = "minimum_balance", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal minimumBalance = new BigDecimal("500.00");

    @Column(name = "ifsc_code", length = 20)
    @Builder.Default
    private String ifscCode = "BANK0001234";

    @Column(name = "branch_name", length = 100)
    @Builder.Default
    private String branchName = "Main Branch";

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();
}
