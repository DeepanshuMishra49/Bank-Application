package com.banking.service.impl;

import com.banking.dto.request.CreateAccountRequest;
import com.banking.dto.request.DepositWithdrawRequest;
import com.banking.dto.request.TransferRequest;
import com.banking.dto.response.AccountResponse;
import com.banking.dto.response.TransactionResponse;
import com.banking.entity.Account;
import com.banking.entity.Customer;
import com.banking.entity.Transaction;
import com.banking.enums.AccountStatus;
import com.banking.enums.TransactionStatus;
import com.banking.enums.TransactionType;
import com.banking.exception.*;
import com.banking.repository.AccountRepository;
import com.banking.repository.CustomerRepository;
import com.banking.repository.TransactionRepository;
import com.banking.service.AccountService;
import com.banking.util.AccountNumberGenerator;
import com.banking.util.BankingConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link AccountService} with full business rule enforcement.
 *
 * <p>Business rules implemented:
 * <ul>
 *   <li>No negative balance allowed</li>
 *   <li>Minimum balance must be maintained after withdrawal</li>
 *   <li>Self-transfer is rejected</li>
 *   <li>Frozen/closed accounts cannot be transacted</li>
 *   <li>Daily transfer limit is enforced</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Value("${banking.account.minimum-balance:500}")
    private BigDecimal globalMinBalance;

    @Override
    public AccountResponse openAccount(CreateAccountRequest request) {
        Customer customer = customerRepository.findByCustomerId(request.customerId())
                .orElseThrow(() -> new UserNotFoundException("customerId", request.customerId()));

        if (!customer.isApproved()) {
            throw new ValidationException("Customer account has not been approved yet");
        }

        // Prevent duplicate account of same type
        if (accountRepository.existsByCustomerIdAndAccountType(customer.getId(), request.accountType())) {
            throw new ValidationException("Customer already has a " + request.accountType() + " account");
        }

        // Generate unique account number
        String accountNumber;
        do {
            accountNumber = accountNumberGenerator.generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .customer(customer)
                .accountType(request.accountType())
                .balance(request.initialDeposit() != null ? request.initialDeposit() : BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        account = accountRepository.save(account);

        // Record initial deposit if provided
        if (request.initialDeposit() != null && request.initialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            Transaction txn = Transaction.builder()
                    .referenceNumber(accountNumberGenerator.generateReferenceNumber())
                    .account(account)
                    .transactionType(TransactionType.DEPOSIT)
                    .amount(request.initialDeposit())
                    .balanceBefore(BigDecimal.ZERO)
                    .balanceAfter(request.initialDeposit())
                    .description("Account opening deposit")
                    .status(TransactionStatus.SUCCESS)
                    .build();
            transactionRepository.save(txn);
        }

        log.info("Account opened: {} for customer: {}", account.getAccountNumber(), customer.getCustomerId());
        return toAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = findActiveAccount(accountNumber);
        return toAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(UUID customerId) {
        return accountRepository.findByCustomerId(customerId).stream()
                .map(this::toAccountResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccountResponse freezeAccount(String accountNumber, String reason) {
        Account account = findAccountByNumber(accountNumber);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new ValidationException("Cannot freeze a closed account");
        }
        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
        log.info("Account frozen: {} - Reason: {}", accountNumber, reason);
        return toAccountResponse(account);
    }

    @Override
    public AccountResponse activateAccount(String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new ValidationException("Cannot activate a closed account");
        }
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        log.info("Account activated: {}", accountNumber);
        return toAccountResponse(account);
    }

    @Override
    public AccountResponse closeAccount(String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidationException(
                    "Account has remaining balance ₹" + account.getBalance() + ". Please withdraw before closing.");
        }
        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
        log.info("Account closed: {}", accountNumber);
        return toAccountResponse(account);
    }

    @Override
    public TransactionResponse deposit(DepositWithdrawRequest request, String ipAddress) {
        Account account = findAccountByNumber(request.accountNumber());

        if (account.getStatus() != AccountStatus.ACTIVE) {
            if (account.getStatus() == AccountStatus.FROZEN) {
                throw new AccountFrozenException(request.accountNumber());
            }
            throw new ValidationException("Account is " + account.getStatus() + " and cannot accept deposits");
        }

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(request.amount());
        account.setBalance(balanceAfter);
        accountRepository.save(account);

        Transaction txn = Transaction.builder()
                .referenceNumber(accountNumberGenerator.generateReferenceNumber())
                .account(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.amount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description(request.description() != null ? request.description() : "Cash deposit")
                .status(TransactionStatus.SUCCESS)
                .ipAddress(ipAddress)
                .build();
        txn = transactionRepository.save(txn);

        log.info("Deposit: ₹{} to account {} | Ref: {}", request.amount(), request.accountNumber(), txn.getReferenceNumber());
        return toTransactionResponse(txn);
    }

    @Override
    public TransactionResponse withdraw(DepositWithdrawRequest request, String ipAddress) {
        Account account = findActiveAccount(request.accountNumber());

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfterWithdrawal = balanceBefore.subtract(request.amount());

        // Enforce minimum balance
        if (balanceAfterWithdrawal.compareTo(account.getMinimumBalance()) < 0) {
            throw new InsufficientBalanceException(
                    "Withdrawal would violate minimum balance of ₹" + account.getMinimumBalance() +
                    ". Available for withdrawal: ₹" + balanceBefore.subtract(account.getMinimumBalance()));
        }

        account.setBalance(balanceAfterWithdrawal);
        accountRepository.save(account);

        Transaction txn = Transaction.builder()
                .referenceNumber(accountNumberGenerator.generateReferenceNumber())
                .account(account)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.amount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfterWithdrawal)
                .description(request.description() != null ? request.description() : "Cash withdrawal")
                .status(TransactionStatus.SUCCESS)
                .ipAddress(ipAddress)
                .build();
        txn = transactionRepository.save(txn);

        log.info("Withdrawal: ₹{} from account {} | Ref: {}", request.amount(), request.accountNumber(), txn.getReferenceNumber());
        return toTransactionResponse(txn);
    }

    @Override
    public TransactionResponse transfer(TransferRequest request, String ipAddress) {
        // Prevent self-transfer
        if (request.fromAccountNumber().equals(request.toAccountNumber())) {
            throw new ValidationException("Self-transfer is not allowed");
        }

        Account fromAccount = findActiveAccount(request.fromAccountNumber());
        Account toAccount = findAccountByNumber(request.toAccountNumber());

        if (toAccount.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException(request.toAccountNumber());
        }
        if (toAccount.getStatus() == AccountStatus.CLOSED) {
            throw new ValidationException("Destination account is closed");
        }

        // Check sufficient balance (after maintaining minimum balance)
        BigDecimal availableBalance = fromAccount.getBalance().subtract(fromAccount.getMinimumBalance());
        if (availableBalance.compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(availableBalance, request.amount());
        }

        // Check daily transfer limit
        BigDecimal projectedTransferred = fromAccount.getTransferredToday().add(request.amount());
        if (projectedTransferred.compareTo(fromAccount.getDailyTransferLimit()) > 0) {
            throw new DailyLimitExceededException(fromAccount.getDailyTransferLimit(), projectedTransferred);
        }

        String referenceNumber = accountNumberGenerator.generateReferenceNumber();
        String description = request.description() != null ? request.description() :
                "Transfer to " + request.toAccountNumber();

        // Debit from source
        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        BigDecimal fromBalanceAfter = fromBalanceBefore.subtract(request.amount());
        fromAccount.setBalance(fromBalanceAfter);
        fromAccount.setTransferredToday(fromAccount.getTransferredToday().add(request.amount()));
        accountRepository.save(fromAccount);

        Transaction debitTxn = Transaction.builder()
                .referenceNumber(referenceNumber)
                .account(fromAccount)
                .transactionType(TransactionType.TRANSFER_OUT)
                .amount(request.amount())
                .balanceBefore(fromBalanceBefore)
                .balanceAfter(fromBalanceAfter)
                .description(description)
                .toAccountNumber(request.toAccountNumber())
                .status(TransactionStatus.SUCCESS)
                .ipAddress(ipAddress)
                .build();
        transactionRepository.save(debitTxn);

        // Credit to destination
        BigDecimal toBalanceBefore = toAccount.getBalance();
        BigDecimal toBalanceAfter = toBalanceBefore.add(request.amount());
        toAccount.setBalance(toBalanceAfter);
        accountRepository.save(toAccount);

        Transaction creditTxn = Transaction.builder()
                .referenceNumber(accountNumberGenerator.generateReferenceNumber())
                .account(toAccount)
                .transactionType(TransactionType.TRANSFER_IN)
                .amount(request.amount())
                .balanceBefore(toBalanceBefore)
                .balanceAfter(toBalanceAfter)
                .description("Transfer from " + request.fromAccountNumber())
                .fromAccountNumber(request.fromAccountNumber())
                .status(TransactionStatus.SUCCESS)
                .ipAddress(ipAddress)
                .build();
        transactionRepository.save(creditTxn);

        log.info("Transfer: ₹{} from {} to {} | Ref: {}", request.amount(),
                request.fromAccountNumber(), request.toAccountNumber(), referenceNumber);
        return toTransactionResponse(debitTxn);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::toAccountResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> searchAccounts(String search, Pageable pageable) {
        return accountRepository.searchAccounts(search, pageable).map(this::toAccountResponse);
    }

    private Account findActiveAccount(String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException(accountNumber);
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new ValidationException("Account " + accountNumber + " is closed");
        }
        if (account.getStatus() == AccountStatus.PENDING_APPROVAL) {
            throw new ValidationException("Account " + accountNumber + " is pending approval");
        }
        return account;
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("accountNumber", accountNumber));
    }

    private AccountResponse toAccountResponse(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getAccountNumber(),
                a.getCustomer() != null ? a.getCustomer().getFullName() : null,
                a.getCustomer() != null ? a.getCustomer().getCustomerId() : null,
                a.getAccountType(),
                a.getBalance(),
                a.getStatus(),
                a.getDailyTransferLimit(),
                a.getTransferredToday(),
                a.getMinimumBalance(),
                a.getIfscCode(),
                a.getBranchName(),
                a.getCreatedAt()
        );
    }

    private TransactionResponse toTransactionResponse(Transaction t) {
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
