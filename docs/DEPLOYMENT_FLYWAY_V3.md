# Flyway 完整部署说明（V1 → V2 → V3）

## 目标
在 staging/production 环境完整引入 Flyway 并执行所有迁移（V1 baseline → V2 修复 users 表 → V3 清理重复索引），确保数据库结构一致且可追踪。

## 前置条件
- 已有数据库备份（Navicat 或 mysqldump）
- 代码已拉取最新，包含所有迁移文件：
  - `V1__baseline.sql`（占位，用于 baseline）
  - `V2__fix_users.sql`（修改 password 长度、email 必需、索引）
  - `V3__drop_uc_users_username_if_exists.sql`（清理重复索引）
- 环境变量或配置文件中的数据库连接信息正确
- Flyway 已在 `pom.xml` 和 `application.yml` 中配置完成

## 部署步骤

### 1. 备份（如未备份）
```bash
# 示例：使用 mysqldump（替换连接参数）
mysqldump -h <host> -P <port> -u <user> -p devnote > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 2. 运行 Flyway 迁移
在项目根目录执行：
```powershell
# Windows PowerShell
.\mvnw.cmd -DskipTests=true flyway:migrate

# 或 Linux/macOS
./mvnw -DskipTests=true flyway:migrate
```

说明：
- 如果是首次在现有库上运行，Flyway 会先执行 baseline（标记为 V1）
- 然后依次应用 V2（修复 users 表）和 V3（清理重复索引）

### 3. 验证
```sql
-- 检查 users 表结构
SHOW FULL COLUMNS FROM users;

-- 检查当前索引
SHOW INDEX FROM users;

-- 检查 Flyway 版本历史
SELECT * FROM flyway_schema_history ORDER BY installed_on DESC LIMIT 5;

-- 检查是否有空邮箱（应为 0）
SELECT COUNT(*) AS empty_emails FROM users WHERE email IS NULL OR TRIM(email) = '';
```

预期结果：
- Schema 版本应为 3
- `password` 列为 VARCHAR(255) NOT NULL
- `email` 列为 NOT NULL，无空值
- 索引：PRIMARY、username 唯一索引（UKr…）、email 索引；无 `uc_users_username`
- `flyway_schema_history` 包含 V1（baseline）、V2、V3 记录

## 迁移内容概览
- **V1（baseline）**：标记现有 schema 为基线，不修改数据
- **V2（fix users）**：
  - `password` 列改为 VARCHAR(255)
  - 为空邮箱填充 `user{id}@example.invalid`
  - `email` 列设为 NOT NULL
  - 添加 username 唯一约束和 email 索引
- **V3（drop duplicate index）**：删除可能存在的冗余索引 `uc_users_username`

## 回滚
如需回滚，参考 `ROLLBACK_USERS_MIGRATION.md` 或从备份恢复。

## 注意事项
- V3 脚本是幂等的：索引不存在时不会报错
- 建议在低峰期执行
- 执行前确认应用无正在运行的长事务