package com.banking.service;

import com.banking.dto.request.CreateAccountRequest;
import com.banking.dto.request.DepositWithdrawRequest;
import com.banking.dto.request.TransferRequest;
import com.banking.dto.response.AccountResponse;
import com.banking.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for bank account and transaction operations.
 */
public interface AccountService {

    AccountResponse openAccount(CreateAccountRequest request);

    AccountResponse getAccountByNumber(String accountNumber);

    List<AccountResponse> getAccountsByCustomerId(UUID customerId);

    AccountResponse freezeAccount(String accountNumber, String reason);

    AccountResponse activateAccount(String accountNumber);

    AccountResponse closeAccount(String accountNumber);

    TransactionResponse deposit(DepositWithdrawRequest request, String ipAddress);

    TransactionResponse withdraw(DepositWithdrawRequest request, String ipAddress);

    TransactionResponse transfer(TransferRequest request, String ipAddress);

    Page<AccountResponse> getAllAccounts(Pageable pageable);

    Page<AccountResponse> searchAccounts(String search, Pageable pageable);
}
