-- V1__baseline.sql
-- Flyway baseline marker: this file intentionally does not change schema.
-- Purpose: when Flyway runs with baseline-on-migrate=true it will record the current
-- schema state as version 1 without applying DDL. This avoids modifying existing
-- production/staging schema while enabling Flyway management for future migrations.

-- NOTE: Do NOT include DDL in this file when using baseline strategy.
-- If you later want to create initial schema from scratch, create a separate V1__init.sql
-- and remove baseline-on-migrate or adjust strategy accordingly.
