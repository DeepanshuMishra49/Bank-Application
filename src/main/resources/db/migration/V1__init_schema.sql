-- ============================================================
--  Banking Management System — Initial Schema
--  V1__init_schema.sql
--  Matches JPA entities exactly (UUIDs, column names, indexes)
-- ============================================================

-- ── Roles ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS roles (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(30)  NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

-- ── Users ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    username            VARCHAR(50)  NOT NULL UNIQUE,
    email               VARCHAR(100) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,
    phone               VARCHAR(15)  UNIQUE,
    enabled             BOOLEAN      NOT NULL DEFAULT TRUE,
    account_non_locked  BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_attempts     INT          NOT NULL DEFAULT 0,
    lock_time           TIMESTAMP,
    profile_picture_url VARCHAR(500),
    last_login_at       TIMESTAMP,
    last_login_ip       VARCHAR(50),
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_phone    ON users (phone);

-- ── User ↔ Roles Join Table ───────────────────────────────────
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- ── Addresses ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS addresses (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    street     VARCHAR(255),
    city       VARCHAR(100),
    state      VARCHAR(100),
    pin_code   VARCHAR(20),
    country    VARCHAR(100) DEFAULT 'India',
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    CONSTRAINT pk_addresses PRIMARY KEY (id)
);

-- ── Customers ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS customers (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    customer_id   VARCHAR(20)  NOT NULL UNIQUE,
    user_id       UUID         NOT NULL UNIQUE,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender        VARCHAR(10),
    occupation    VARCHAR(100),
    annual_income DOUBLE PRECISION,
    approved      BOOLEAN      NOT NULL DEFAULT FALSE,
    approved_by   VARCHAR(100),
    address_id    UUID,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP,
    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT fk_customers_user    FOREIGN KEY (user_id)    REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_customers_address FOREIGN KEY (address_id) REFERENCES addresses (id)
);

CREATE INDEX IF NOT EXISTS idx_customers_customer_id ON customers (customer_id);

-- ── Employees ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employees (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL UNIQUE,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    designation VARCHAR(100),
    department  VARCHAR(100),
    branch      VARCHAR(100),
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    first_name  VARCHAR(100) NOT NULL DEFAULT '',
    last_name   VARCHAR(100) NOT NULL DEFAULT '',
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP,
    CONSTRAINT pk_employees PRIMARY KEY (id),
    CONSTRAINT fk_employees_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_employees_employee_id ON employees (employee_id);

-- ── Accounts ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS accounts (
    id                  UUID          NOT NULL DEFAULT gen_random_uuid(),
    account_number      VARCHAR(20)   NOT NULL UNIQUE,
    customer_id         UUID          NOT NULL,
    account_type        VARCHAR(20)   NOT NULL,
    balance             NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING_APPROVAL',
    daily_transfer_limit NUMERIC(19,2) NOT NULL DEFAULT 100000.00,
    transferred_today   NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    minimum_balance     NUMERIC(19,2) NOT NULL DEFAULT 500.00,
    ifsc_code           VARCHAR(20)   DEFAULT 'BANK0001234',
    branch_name         VARCHAR(100)  DEFAULT 'Main Branch',
    created_at          TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP,
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT fk_accounts_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts (account_number);
CREATE INDEX IF NOT EXISTS idx_accounts_customer_id    ON accounts (customer_id);

-- ── Transactions ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS transactions (
    id                UUID          NOT NULL DEFAULT gen_random_uuid(),
    reference_number  VARCHAR(30)   NOT NULL UNIQUE,
    account_id        UUID          NOT NULL,
    transaction_type  VARCHAR(20)   NOT NULL,
    amount            NUMERIC(19,2) NOT NULL,
    balance_before    NUMERIC(19,2) NOT NULL,
    balance_after     NUMERIC(19,2) NOT NULL,
    description       VARCHAR(500),
    to_account_number   VARCHAR(20),
    from_account_number VARCHAR(20),
    status            VARCHAR(20)   NOT NULL DEFAULT 'SUCCESS',
    ip_address        VARCHAR(50),
    channel           VARCHAR(30)   DEFAULT 'WEB',
    created_at        TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP,
    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id    ON transactions (account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_reference     ON transactions (reference_number);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at    ON transactions (created_at);

-- ── Beneficiaries ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS beneficiaries (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    customer_id    UUID         NOT NULL,
    name           VARCHAR(200) NOT NULL,
    account_number VARCHAR(20)  NOT NULL,
    bank_name      VARCHAR(200),
    ifsc_code      VARCHAR(20),
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP,
    CONSTRAINT pk_beneficiaries PRIMARY KEY (id),
    CONSTRAINT fk_beneficiaries_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
);

-- ── KYC Details ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS kyc_details (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    customer_id     UUID         NOT NULL UNIQUE,
    document_type   VARCHAR(30)  NOT NULL,
    document_number VARCHAR(100) NOT NULL,
    verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    verified_by     VARCHAR(100),
    verified_at     TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP,
    CONSTRAINT pk_kyc_details PRIMARY KEY (id),
    CONSTRAINT fk_kyc_details_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
);

-- ── Audit Logs ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_logs (
    id            UUID          NOT NULL DEFAULT gen_random_uuid(),
    action        VARCHAR(200)  NOT NULL,
    performed_by  VARCHAR(200),
    entity_type   VARCHAR(100),
    entity_id     VARCHAR(100),
    details       TEXT,
    ip_address    VARCHAR(50),
    created_at    TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_performed_by ON audit_logs (performed_by);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at   ON audit_logs (created_at);
