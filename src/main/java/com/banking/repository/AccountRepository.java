package com.banking.repository;

import com.banking.entity.Account;
import com.banking.enums.AccountStatus;
import com.banking.enums.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Account} entity with balance and status queries.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(UUID customerId);

    Page<Account> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Account> findByStatus(AccountStatus status, Pageable pageable);

    boolean existsByCustomerIdAndAccountType(UUID customerId, AccountType accountType);

    long countByStatus(AccountStatus status);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.status = 'ACTIVE'")
    java.math.BigDecimal sumTotalActiveBalance();

    @Query("SELECT a FROM Account a WHERE " +
           "a.accountNumber LIKE CONCAT('%', :search, '%') OR " +
           "LOWER(a.customer.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.customer.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Account> searchAccounts(@Param("search") String search, Pageable pageable);
}
