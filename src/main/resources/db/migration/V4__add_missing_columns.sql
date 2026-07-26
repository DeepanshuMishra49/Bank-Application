-- ============================================================
--  V4__add_missing_columns.sql
--  Adds all columns present in JPA entities but absent from V1:
--    • beneficiaries  → nickname, active
--    • kyc_details    → document_front_url, document_back_url, rejection_reason
-- ============================================================

-- ── Beneficiaries ─────────────────────────────────────────────
ALTER TABLE beneficiaries
    ADD COLUMN IF NOT EXISTS nickname VARCHAR(50),
    ADD COLUMN IF NOT EXISTS active   BOOLEAN NOT NULL DEFAULT TRUE;

-- ── KYC Details ───────────────────────────────────────────────
ALTER TABLE kyc_details
    ADD COLUMN IF NOT EXISTS document_front_url  TEXT,
    ADD COLUMN IF NOT EXISTS document_back_url   TEXT,
    ADD COLUMN IF NOT EXISTS rejection_reason    VARCHAR(500);
