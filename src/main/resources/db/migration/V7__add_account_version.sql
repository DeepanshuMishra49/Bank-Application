-- ============================================================
--  V7__add_account_version.sql
--  Adds the JPA @Version column to the accounts table.
--
--  BUG FIX: Without this column every concurrent deposit/withdrawal/
--  transfer pair on the same account is susceptible to a lost-update
--  race condition — two transactions both read the same balance,
--  compute different new values, and one silently overwrites the other.
--
--  The `version` column is incremented by Hibernate on every UPDATE.
--  If another transaction already incremented it since our read,
--  Hibernate throws ObjectOptimisticLockingFailureException, and the
--  caller must retry or return HTTP 409 Conflict.
-- ============================================================

ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
