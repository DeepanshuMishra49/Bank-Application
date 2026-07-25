package com.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

/**
 * Thrown when a transfer would exceed the account's daily transfer limit.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class DailyLimitExceededException extends RuntimeException {

    public DailyLimitExceededException(BigDecimal limit, BigDecimal attempted) {
        super(String.format(
                "Daily transfer limit exceeded. Limit: ₹%.2f, Attempted total: ₹%.2f",
                limit, attempted));
    }
}
