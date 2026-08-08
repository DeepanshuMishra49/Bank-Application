-- ============================================================
--  Banking Management System — Demo Customer & Accounts Seed
--  V5__seed_demo_customer_and_accounts.sql
-- ============================================================

-- ── 1. Demo Address ──────────────────────────────────────────
INSERT INTO addresses (id, street, city, state, pin_code, country)
VALUES (
    '00000000-0000-0000-0003-000000000001',
    '123 Financial District, MG Road',
    'Bangalore',
    'Karnataka',
    '560001',
    'India'
) ON CONFLICT DO NOTHING;

-- ── 2. Primary Customer User (deep270804) ────────────────────
-- Password: Customer@123
INSERT INTO users (id, username, email, password, phone, enabled, account_non_locked)
VALUES (
    '00000000-0000-0000-0003-000000000002',
    'deep270804',
    'deep270804@gmail.com',
    '$2b$12$4M5DA24TJTPtbuYnJfT4U.PxC5OT3oYiy59CFpXwX1f9SiancxIMO',
    '9876543210',
    TRUE,
    TRUE
) ON CONFLICT (username) DO NOTHING;

-- Assign ROLE_CUSTOMER to deep270804 safely
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'deep270804' AND r.name = 'ROLE_CUSTOMER'
ON CONFLICT DO NOTHING;

-- Customer Profile for deep270804
INSERT INTO customers (id, customer_id, user_id, first_name, last_name, date_of_birth, gender, occupation, annual_income, approved, approved_by, address_id)
SELECT 
    '00000000-0000-0000-0003-000000000003',
    'CUST0000001',
    u.id,
    'Deepanshu',
    'Mishra',
    '1998-08-27'::date,
    'Male',
    'Software Engineer',
    1200000.00,
    TRUE,
    'admin',
    '00000000-0000-0000-0003-000000000001'
FROM users u WHERE u.username = 'deep270804'
ON CONFLICT (customer_id) DO NOTHING;

-- Verified KYC for CUST0000001
INSERT INTO kyc_details (id, customer_id, document_type, document_number, verified, verified_by, verified_at)
SELECT 
    '00000000-0000-0000-0003-000000000004',
    c.id,
    'AADHAAR_CARD',
    '1234-5678-9012',
    TRUE,
    'admin',
    NOW()
FROM customers c WHERE c.customer_id = 'CUST0000001'
ON CONFLICT DO NOTHING;

-- ── Accounts for CUST0000001 ─────────────────────────────────
-- Account 1: Savings (100020003000)
INSERT INTO accounts (id, account_number, customer_id, account_type, balance, status, daily_transfer_limit, minimum_balance, ifsc_code, branch_name)
SELECT 
    '00000000-0000-0000-0003-000000000005',
    '100020003000',
    c.id,
    'SAVINGS',
    50000.00,
    'ACTIVE',
    100000.00,
    500.00,
    'BANK0001234',
    'Main Branch'
FROM customers c WHERE c.customer_id = 'CUST0000001'
ON CONFLICT (account_number) DO NOTHING;

-- Account 2: Current (100020003001)
INSERT INTO accounts (id, account_number, customer_id, account_type, balance, status, daily_transfer_limit, minimum_balance, ifsc_code, branch_name)
SELECT 
    '00000000-0000-0000-0003-000000000006',
    '100020003001',
    c.id,
    'CURRENT',
    125000.00,
    'ACTIVE',
    250000.00,
    1000.00,
    'BANK0001234',
    'Main Branch'
FROM customers c WHERE c.customer_id = 'CUST0000001'
ON CONFLICT (account_number) DO NOTHING;

-- ── 3. Secondary Customer User (john_doe) for transfers ─────
INSERT INTO users (id, username, email, password, phone, enabled, account_non_locked)
VALUES (
    '00000000-0000-0000-0004-000000000001',
    'john_doe',
    'john@example.com',
    '$2b$12$4M5DA24TJTPtbuYnJfT4U.PxC5OT3oYiy59CFpXwX1f9SiancxIMO',
    '9876543211',
    TRUE,
    TRUE
) ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'john_doe' AND r.name = 'ROLE_CUSTOMER'
ON CONFLICT DO NOTHING;

INSERT INTO customers (id, customer_id, user_id, first_name, last_name, approved, approved_by)
SELECT 
    '00000000-0000-0000-0004-000000000002',
    'CUST0000002',
    u.id,
    'John',
    'Doe',
    TRUE,
    'admin'
FROM users u WHERE u.username = 'john_doe'
ON CONFLICT (customer_id) DO NOTHING;

-- Account for John Doe (100020004000)
INSERT INTO accounts (id, account_number, customer_id, account_type, balance, status, daily_transfer_limit, minimum_balance, ifsc_code, branch_name)
SELECT 
    '00000000-0000-0000-0004-000000000003',
    '100020004000',
    c.id,
    'SAVINGS',
    25000.00,
    'ACTIVE',
    100000.00,
    500.00,
    'BANK0001234',
    'Main Branch'
FROM customers c WHERE c.customer_id = 'CUST0000002'
ON CONFLICT (account_number) DO NOTHING;

-- Fix any existing CHECKING account_type to CURRENT
UPDATE accounts SET account_type = 'CURRENT' WHERE account_type = 'CHECKING';

