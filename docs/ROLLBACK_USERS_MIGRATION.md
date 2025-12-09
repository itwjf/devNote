# 回滚与恢复计划：V2__fix_users.sql + V3__drop_uc_users_username_if_exists.sql

请在对生产库执行任何迁移前严格按照下面步骤操作并保留备份文件。该文档给出回滚策略与恢复步骤。注意：DDL 通常不可自动回滚，推荐的回滚手段是通过备份恢复或手写反向 SQL 并谨慎执行。

## 1. 在执行迁移前的备份（强制）
- 用 mysqldump 做一次完整的数据库备份（示例）：

```powershell
# Windows PowerShell 示例（会提示输入密码）
$ts = Get-Date -Format yyyyMMddHHmm
mysqldump -u <db_user> -p <database_name> > C:\backups\devnote_backup_$ts.sql
```

- 验证备份文件大小与内容：
  - 打开文件前几行确认是 SQL 导出头信息。

## 2. 在 staging 上先执行验证流程（强制）
在 staging（与 prod 相同版本的 DB）执行迁移并验证：
1. 运行迁移：
```powershell
# 使用 Maven Flyway 插件 或 程序启动自动迁移
.\mvnw flyway:migrate -Dflyway.url=jdbc:mysql://<staging-host>:3306/<db> -Dflyway.user=<user> -Dflyway.password=<pwd>
```
2. 验证检查点：
   - 确认 password 列类型已变为 VARCHAR(255)
     ```sql
     SHOW FULL COLUMNS FROM users LIKE 'password';
     ```
   - 确认 email 列无 NULL 并查看示例值
     ```sql
     SELECT id, username, email FROM users WHERE email LIKE 'user%@example.invalid' LIMIT 10;
     ```
   - 确认索引状态（V3 验证）：
     ```sql
     SHOW INDEX FROM users;
     -- 应无 uc_users_username 索引，保留 PRIMARY、username 唯一索引、email 索引
     ```
   - 在 staging 上执行一次注册与一次登录流程，确保登录不受影响。

## 3. 迁移后若需回滚（紧急恢复）
### 方案 A（推荐）：从备份还原（最可靠）
1. 停止对生产数据库的写入（尽量将应用下线或切换为只读模式）。
2. 还原备份：
```powershell
mysql -u <db_user> -p <database_name> < C:\backups\devnote_backup_YYYYMMDDHHMM.sql
```
3. 重启应用并验证。

### 方案 B（若不想做整库还原，可尝试下列手写逆向操作，但风险较高）
> 注意：以下反向 SQL 无法恢复被 UPDATE 覆盖过的原始 email 值，除非你在迁移前单独备份过这些列的原始数据表。

#### V2 回滚操作（高风险）
1. 如果你只是想恢复 `email` 为 NULL（不推荐），可执行：
```sql
ALTER TABLE users MODIFY COLUMN email VARCHAR(255) NULL DEFAULT NULL;
```
2. 如果你想将 password 列恢复到较小长度（如果之前记录了原定义）：
```sql
ALTER TABLE users MODIFY COLUMN password VARCHAR(100);
```
但务必注意：缩短列长度会截断数据，可能导致 BCrypt 哈希被破坏，进而导致登陆失败——因此**不要**在不备份的情况下缩短 password 列。

#### V3 回滚操作（低风险，一般不需要）
V3 是幂等操作，仅删除冗余索引。如果业务需要重建该索引：
```sql
-- 仅在确认业务需要时执行
ALTER TABLE users ADD UNIQUE INDEX uc_users_username (username);
```
注意：重建前请检查是否已存在其他 username 唯一索引，避免重复。

## 4. 推荐的回滚流程（步骤化）
1. 立即停止应用写流量（维护窗口）。
2. 恢复最新备份（mysqldump）。
3. 验证恢复后的数据一致性（检查用户 counts、随机用户登录）。
4. 将变更合并到 bugfix 分支并在 staging 上演练修复步骤。

## 5. 事后清理与补充
- 若你接受迁移并确认 email 被占位填充为 `user{id}@example.invalid`：
  - 考虑后续一条迁移脚本，用业务流程通知用户或管理员补全邮箱，或在下一步引入强制邮箱验证流程以使用户更新邮箱。
- 如果你要为 username 添加 UNIQUE 约束：
  - 先查重并处理重复项，再在单独的迁移脚本中添加约束。
- V3 迁移后索引状态：
  - 通常不需要额外操作，冗余索引已清理
  - 如发现性能问题，可检查查询计划并考虑优化索引策略

---

## 迁移版本概览
- **V1（baseline）**：标记现有 schema 为基线，无数据变更
- **V2（fix users）**：修改 password 长度、email 必需、添加索引（有数据变更）
- **V3（drop duplicate index）**：删除冗余索引（幂等，低风险）

如需我把上述回滚步骤写成可执行脚本或生成单独的 DOWN 脚本（注意仍然不如备份可靠），我可以继续生成。现在我已把迁移 SQL 和回滚计划文件添加到仓库，你审阅后告诉我需要调整的地方或是否准许我继续将 Flyway 依赖与配置加入项目。