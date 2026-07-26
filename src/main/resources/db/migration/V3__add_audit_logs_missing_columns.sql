-- ============================================================
--  V3__add_audit_logs_missing_columns.sql
--  Adds columns required by the AuditLog entity that were
--  absent from the original V1 schema definition.
-- ============================================================

ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS success       BOOLEAN      NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS error_message VARCHAR(500);
