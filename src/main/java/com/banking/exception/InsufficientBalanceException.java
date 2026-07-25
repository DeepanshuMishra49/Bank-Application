package com.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

/**
 * Thrown when a withdrawal or transfer would result in insufficient funds.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(BigDecimal available, BigDecimal requested) {
        super(String.format(
                "Insufficient balance. Available: ₹%.2f, Requested: ₹%.2f",
                available, requested));
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
