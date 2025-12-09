-- V3__drop_uc_users_username_if_exists.sql
-- Safely drop duplicate unique index `uc_users_username` if it exists.
-- Idempotent: will not error if index is missing.

-- Note: Some MySQL versions don't support `DROP INDEX IF EXISTS`,
-- so use information_schema + dynamic SQL to be maximally compatible.

SET @idx_count = (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND INDEX_NAME = 'uc_users_username'
);

SET @sql = IF(@idx_count = 0,
  'SELECT 0',
  'ALTER TABLE `users` DROP INDEX `uc_users_username`'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
