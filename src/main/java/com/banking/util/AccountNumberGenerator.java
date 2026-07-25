package com.banking.util;

import com.banking.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique account numbers and transaction reference numbers.
 * Uses SecureRandom for unpredictability and an atomic counter for uniqueness.
 */
@Component
public class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis() % 10000);

    /**
     * Generates a unique 12-digit account number prefixed with "10".
     *
     * @return a unique account number string
     */
    public String generateAccountNumber() {
        // Format: 10 + 8 random digits + 2 sequence digits = 12 total
        long randomPart = (long) (RANDOM.nextDouble() * 100_000_000L);
        long seq = SEQUENCE.getAndIncrement() % 100;
        return String.format("%s%08d%02d", BankingConstants.ACCOUNT_NUMBER_PREFIX, randomPart, seq);
    }

    /**
     * Generates a unique transaction reference number.
     *
     * @return a reference number string like TXN20240101123456789012
     */
    public String generateReferenceNumber() {
        long timestamp = Instant.now().toEpochMilli();
        int random = RANDOM.nextInt(99999);
        return String.format("%s%d%05d", BankingConstants.REFERENCE_PREFIX, timestamp, random);
    }

    /**
     * Generates a customer ID with CUST prefix and zero-padded sequence.
     *
     * @param sequence the numeric sequence
     * @return formatted customer ID
     */
    public String generateCustomerId(long sequence) {
        return String.format("%s%07d", BankingConstants.CUSTOMER_ID_PREFIX, sequence);
    }

    /**
     * Generates an employee ID with EMP prefix and zero-padded sequence.
     *
     * @param sequence the numeric sequence
     * @return formatted employee ID
     */
    public String generateEmployeeId(long sequence) {
        return String.format("%s%05d", BankingConstants.EMPLOYEE_ID_PREFIX, sequence);
    }
}
