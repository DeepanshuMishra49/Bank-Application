-- ============================================================
--  V6__fix_checking_account_type.sql
--  Converts any legacy 'CHECKING' account types to 'CURRENT'
-- ============================================================
UPDATE accounts SET account_type = 'CURRENT' WHERE account_type = 'CHECKING';
