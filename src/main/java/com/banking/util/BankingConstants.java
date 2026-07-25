package com.banking.util;

/**
 * Centralized constants for the Banking Management System.
 * Avoids magic numbers and hardcoded strings throughout the codebase.
 */
public final class BankingConstants {

    private BankingConstants() {
        // Utility class — prevent instantiation
    }

    // ─── Security ─────────────────────────────────────────────────────────────
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final long LOCK_DURATION_MINUTES = 15L;
    public static final int BCRYPT_STRENGTH = 12;

    // ─── Account ──────────────────────────────────────────────────────────────
    public static final String ACCOUNT_NUMBER_PREFIX = "10";
    public static final int ACCOUNT_NUMBER_LENGTH = 12;
    public static final String DEFAULT_IFSC = "BANK0001234";
    public static final String DEFAULT_BRANCH = "Main Branch";

    // ─── Customer IDs ─────────────────────────────────────────────────────────
    public static final String CUSTOMER_ID_PREFIX = "CUST";
    public static final String EMPLOYEE_ID_PREFIX = "EMP";

    // ─── Transaction ──────────────────────────────────────────────────────────
    public static final String REFERENCE_PREFIX = "TXN";
    public static final int REFERENCE_LENGTH = 16;
    public static final String MINI_STATEMENT_COUNT_STR = "10";
    public static final int MINI_STATEMENT_COUNT = 10;

    // ─── Pagination ───────────────────────────────────────────────────────────
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // ─── Roles ────────────────────────────────────────────────────────────────
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";

    // ─── Redirect URLs ────────────────────────────────────────────────────────
    public static final String ADMIN_DASHBOARD_URL = "/admin/dashboard";
    public static final String EMPLOYEE_DASHBOARD_URL = "/employee/dashboard";
    public static final String CUSTOMER_DASHBOARD_URL = "/customer/dashboard";
    public static final String LOGIN_URL = "/login";
    public static final String LOGOUT_URL = "/logout";
    public static final String ACCESS_DENIED_URL = "/access-denied";

    // ─── Flash Attribute Keys ────────────────────────────────────────────────
    public static final String FLASH_SUCCESS = "successMessage";
    public static final String FLASH_ERROR = "errorMessage";
    public static final String FLASH_WARNING = "warningMessage";

    // ─── File Upload ──────────────────────────────────────────────────────────
    public static final String ALLOWED_IMAGE_EXTENSIONS = ".jpg,.jpeg,.png,.gif";
    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L; // 5 MB

    // ─── Date Formats ─────────────────────────────────────────────────────────
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String DATETIME_FORMAT = "dd-MM-yyyy HH:mm:ss";

    // ─── Audit Actions ────────────────────────────────────────────────────────
    public static final String AUDIT_LOGIN = "USER_LOGIN";
    public static final String AUDIT_LOGOUT = "USER_LOGOUT";
    public static final String AUDIT_LOGIN_FAILED = "LOGIN_FAILED";
    public static final String AUDIT_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String AUDIT_TRANSFER = "FUND_TRANSFER";
    public static final String AUDIT_DEPOSIT = "DEPOSIT";
    public static final String AUDIT_WITHDRAWAL = "WITHDRAWAL";
    public static final String AUDIT_ACCOUNT_FROZEN = "ACCOUNT_FROZEN";
    public static final String AUDIT_ACCOUNT_ACTIVATED = "ACCOUNT_ACTIVATED";
    public static final String AUDIT_KYC_VERIFIED = "KYC_VERIFIED";
    public static final String AUDIT_PASSWORD_CHANGED = "PASSWORD_CHANGED";
}
