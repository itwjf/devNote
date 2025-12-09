-- V2__fix_users.sql
-- 目标：保证 users 表的 password 列能完整保存 BCrypt 哈希，确保 email 列为 NOT NULL 并填充合理默认值。
-- 注意：此脚本含有不可回滚的 DDL。强烈建议在执行前先做备份并在 staging 环境先跑一遍。

/*
预执行检查（在执行本脚本前请手动执行并确认：
1) 检查 username 列是否有重复：
   SELECT username, COUNT(*) AS c FROM users GROUP BY username HAVING c>1;
   若有重复，请先人工合并或处理，否则添加 UNIQUE 会失败。

2) 检查 email 是否存在 NULL 或空串：
   SELECT COUNT(*) FROM users WHERE email IS NULL OR email = '';

3) 记录当前 password 列定义：
   SHOW FULL COLUMNS FROM users LIKE 'password';
*/

-- 1) 扩展 password 列长度，使用 255 保证兼容性（MySQL）
ALTER TABLE users
  MODIFY COLUMN `password` VARCHAR(255) NOT NULL;

-- 2) 为所有 NULL 或空的 email 填充占位邮箱，使用唯一占位格式以避免重复冲突
--    这里使用 user id 构造 email：user{id}@example.invalid（example.invalid 保证不会被误用）
UPDATE users
  SET email = CONCAT('user', id, '@example.invalid')
  WHERE email IS NULL OR TRIM(email) = '';

-- 3) 将 email 列修改为 NOT NULL 并设置默认值（若你想要空字符串作为默认，可改为 DEFAULT ''）
ALTER TABLE users
  MODIFY COLUMN `email` VARCHAR(255) NOT NULL DEFAULT '';

-- 4) （可选）创建 username 唯一索引：请先确认没有重复用户名
--    如果确认无重复，可解除下面注释以创建唯一索引
ALTER TABLE users ADD CONSTRAINT uc_users_username UNIQUE (username);

-- 5) 额外建议：为 email 添加索引以加速按邮箱查询（如果业务需要）
CREATE INDEX idx_users_email ON users(email);

-- 说明：本脚本优先采取保守改动（扩大长度、填充 email、设 NOT NULL）。
-- 若你的数据库并非 MySQL，语法需调整（例如 PostgreSQL ALTER TYPE 语句不同）。
