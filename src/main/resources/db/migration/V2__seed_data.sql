-- ============================================================
--  Banking Management System — Seed Data
--  V2__seed_data.sql
--  Default roles + admin user (password: Admin@123)
-- ============================================================

-- ── Roles ────────────────────────────────────────────────────
INSERT INTO roles (id, name) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ROLE_ADMIN'),
    ('00000000-0000-0000-0000-000000000002', 'ROLE_EMPLOYEE'),
    ('00000000-0000-0000-0000-000000000003', 'ROLE_CUSTOMER')
ON CONFLICT (name) DO NOTHING;

-- ── Admin User ───────────────────────────────────────────────
-- Password: Admin@123  (BCrypt strength 12)
INSERT INTO users (id, username, email, password, phone, enabled, account_non_locked)
VALUES (
    '00000000-0000-0000-0001-000000000001',
    'admin',
    'admin@bank.com',
    '$2a$12$D4cL.XVPNYFdNaVn3Z.UiOhOxeRNBjrb9L6VlzO/TZrJiXwtlV/0S',
    '9000000000',
    TRUE,
    TRUE
)
ON CONFLICT (username) DO NOTHING;

-- ── Assign ADMIN role ────────────────────────────────────────
INSERT INTO user_roles (user_id, role_id)
SELECT '00000000-0000-0000-0001-000000000001', id
FROM   roles WHERE name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- ── Demo Employee User ────────────────────────────────────────
-- Password: Employee@123  (BCrypt strength 12)
INSERT INTO users (id, username, email, password, phone, enabled, account_non_locked)
VALUES (
    '00000000-0000-0000-0002-000000000001',
    'emp001',
    'employee@bank.com',
    '$2a$12$LkzGqhqf2XBJfJAalqXxQenGzVGMvMbKD4WS7kgkDrAjIKpHx/F7G',
    '9100000001',
    TRUE,
    TRUE
)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT '00000000-0000-0000-0002-000000000001', id
FROM   roles WHERE name = 'ROLE_EMPLOYEE'
ON CONFLICT DO NOTHING;

INSERT INTO employees (id, user_id, employee_id, designation, department)
VALUES (
    '00000000-0000-0000-0002-000000000002',
    '00000000-0000-0000-0002-000000000001',
    'EMP-00001',
    'Branch Manager',
    'Operations'
)
ON CONFLICT (employee_id) DO NOTHING;
