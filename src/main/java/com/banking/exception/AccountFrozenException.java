package com.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an operation is attempted on a frozen account.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountFrozenException extends RuntimeException {

    public AccountFrozenException(String accountNumber) {
        super("Account " + accountNumber + " is frozen. Please contact the bank.");
    }
}
