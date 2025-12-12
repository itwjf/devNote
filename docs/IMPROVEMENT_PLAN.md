# devNote 项目改进计划（审计报告 / Markdown 版本）

> 生成时间：2025-12-08  

## 1. 报告摘要
本报告面向项目 `devNote`（Spring Boot + Thymeleaf + Spring Security + JPA/MySQL），目的在于梳理当前存在的架构与工程质量问题，给出逐步改进计划（包含建议、实现方法、优先级、难度估算与验收标准），便于团队评审与分阶段实施。总体方向：先固化稳定性与数据一致性（DB 迁移、测试、CI），再补强安全/运维/可观测性，最后推进性能与升级（Java 21 等）。

---

## 2. 当前高风险问题（需要尽快处理）
- 缺少数据库迁移管理（Flyway/Liquibase） → 导致实体与数据库结构不一致（已发生 `email` NOT NULL 错误）。  
- 测试覆盖严重不足（单元 & 集成测试） → 升级/重构风险高。  
- CI / PR 自动化缺失 → 无自动门禁，合并风险高。  
- 安全配置隐患（CSRF 禁用、角色管理不一致、无失败限制） → 影响生产安全与账户安全。  

---

## 3. 改进目标（按阶段）
- 阶段 0（Quick Wins，0–3 天）：
  - 引入 Flyway 并建立初始迁移脚本。  
  - 固定 `password` 列长度（BCrypt）并规范 `role` 存储（短名）。  
  - 修复已知运行时错误（如角色前缀问题）。
- 阶段 1（1–2 周）：
  - 单元测试与集成测试覆盖关键路径（注册、登录、权限、文章 CRUD）。  
  - 建立 CI（GitHub Actions）执行构建、测试和静态检查。  
  - DTO + Bean Validation + 全局异常处理。  
- 阶段 2（2–6 周）：
  - 邮箱验证、密码重置与异步邮件。  
  - 审计字段、结构化日志、监控（Micrometer/Prometheus）。  
  - 容器化（Dockerfile + docker-compose）。  
- 阶段 3（长期）：
  - 性能优化（分页、缓存 Redis）。  
  - Java 21 升级与依赖更新（按模块分批完成）。  
  - API-first/SPA 分离（如需移动端支持）。

---

## 4. 详细改进项（每项含建议、实现方法、原因、优先级、难度、估算、验收标准）

### 4.1 引入数据库迁移 — Flyway（强烈建议）
- 建议：将当前 schema 导出成 `V1__init.sql`，并在 `src/main/resources/db/migration` 中启用 Flyway。
- 实施方法：
  1. 在 `pom.xml` 添加 `org.flywaydb:flyway-core`（或 Gradle 相应依赖）。
  2. 导出现有数据库建表 SQL 为 `V1__init.sql`（含 users、posts、关系、索引）。
  3. 提交迁移文件并在本地新 DB 上运行验证。
- 原因：管理 schema 演进，避免运行时报错，便于回滚与审计。
- 优先级：高；难度：低；估算：0.5–1 人天。
- 验收：新环境 `mvnw -DskipTests=true spring-boot:run` 时 Flyway 自动执行，迁移成功且应用启动。

### 4.2 password 字段长度与安全（必须）
- 建议：在 `User.password` 上使用 `@Column(length=60, nullable=false)`，并在迁移脚本中扩展 DB 列长度。
- 原因：BCrypt 哈希长度约 60，显式约束避免截断。
- 优先级：高；难度：低；估算：0.25–0.5 人天。
- 验收：注册后 DB 中 `password` 字符串完整，登录成功。

### 4.3 统一角色约定与代码规范化 ✅
- 建议：实体存短名（`USER`），`CustomUserDetailsService` 映射时加 `ROLE_` 前缀；在 `User.setRole` 中规范化输入（去除前缀）。
- 实施状态：已完成
  - `User.setRole()` 方法会去除 `ROLE_` 前缀，存储干净的短名
  - `CustomUserDetailsService` 在映射时添加 `ROLE_` 前缀
  - 添加了详细的注释说明角色约定
- 原因：避免重复前缀导致错误（已发生）。
- 优先级：高；难度：低；估算：0.25–0.5 人天。
- 验收：登录不再出现 `ROLE_` 错误，代码有注释说明角色约定。

### 4.4 测试覆盖（单元 + 集成） ✅
- 建议：
  - 单元：`UserServiceImpl` 用 Mockito 验证注册、异常等。
  - 集成：`@SpringBootTest` + Testcontainers MySQL 做注册/登录端到端。
- 实施状态：已完成
  - 创建了 `UserServiceImplTest` 单元测试，覆盖注册、查找、隐私设置等功能
  - 创建了 `UserIntegrationTest` 集成测试，使用 Testcontainers 测试真实数据库场景
  - 添加了 JUnit 5、Mockito、Testcontainers、H2 等测试依赖
  - 配置了测试环境专用的 `application-test.properties`
- 原因：预防回归、支持升级与重构。
- 优先级：高；难度：中；估算：2–5 人天。
- 验收：CI 中 tests 阶段通过，覆盖关键边界条件。

### 4.5 CI（GitHub Actions） ✅
- 建议：添加 `ci.yml`，执行 JDK matrix（可先 JDK 17），运行 `mvn -B test`、Checkstyle、SpotBugs。
- 实施状态：已完成
  - 创建了 `.github/workflows/ci.yml` 配置文件
  - 配置了多阶段流水线：测试、构建、安全扫描、代码质量检查
  - 集成了 MySQL Testcontainers 用于集成测试
  - 添加了 Checkstyle、SpotBugs、OWASP Dependency Check 插件
  - 配置了测试报告上传和构建产物保存
- 原因：保证 PR 质量与构建可复现性。
- 优先级：高；难度：中；估算：1–2 人天。
- 验收：PR 自动触发并显示合格/失败状态。

### 4.6 DTO + Bean Validation + 全局异常处理
- 建议：为所有表单引入 DTO，使用 `@Validated` + `BindingResult`；建立统一 `@ControllerAdvice` 错误处理返回一致视图或 JSON。
- 原因：安全、易维护、便于前端显示错误。
- 优先级：中；难度：中；估算：1–2 人天。
- 验收：表单错误展示规范，异常以统一方式记录。

### 4.7 邮件验证与密码重置
- 建议：实现注册邮箱验证（token 存 DB + 过期策略）与密码重置流程。
- 原因：保证账号真实性与用户自助恢复。
- 优先级：中；难度：中；估算：2–4 人天。
- 验收：邮件发送/接收及验证流程可成功完成。

### 4.8 可观察性（日志、指标、追踪）
- 建议：Micrometer + Prometheus + Grafana，结构化日志（JSON），健康检查端点。
- 原因：生产监控、报警与性能分析。
- 优先级：中；难度：中；估算：2–4 人天。
- 验收：Prometheus 能抓取指标，Grafana 有基础 dashboard。

### 4.9 容器化（Docker）与本地 compose
- 建议：提供 `Dockerfile` 与 `docker-compose.yml`（包含 MySQL、Prometheus 可选）。
- 原因：统一部署、方便 CI/CD。
- 优先级：中；难度：中；估算：1–3 人天。
- 验收：`docker-compose up` 能在本地运行完整栈。

### 4.10 Java 21 升级计划（长期）
- 建议：先分析依赖兼容性（maven plugin），按模块分阶段升级；先在 CI 上跑 JDK 21 build。
- 原因：利用新特性与性能改进，但风险较高需谨慎。
- 优先级：低/中；难度：高；估算：5–15 人天（视依赖复杂度）。
- 验收：在 JDK 21 下所有测试通过且部署验证正常。

---

## 5. 推荐实施节奏（Roadmap）
- Sprint 0（0–3 天）：Flyway 初始迁移 + `password` 长度 + role 规范（PR1）。  
- Sprint 1（1 周）：测试覆盖与 CI（PR2）。  
- Sprint 2（1–2 周）：DTO/校验/异常处理 + 邮件验证（PR3）。  
- Sprint 3（2–4 周）：监控/容器化/缓存（PR4）。  
- Sprint 4（长期）：Java 21 升级与进一步优化。

---

## 6. 风险与回滚策略
- 所有 DB 变更先在 staging 测试并做好备份（mysqldump）。Flyway 无自动回滚，需准备 DOWN 脚本或备份还原步骤。  
- 依赖升级需在独立分支中逐步完成，若出现兼容问题可回退合并（PR revert）并在 CI 驱动器上验证回退成功。  
- 关键安全改动（例如 CSRF 启用）应先在 staging 验证表单行为并更新前端 token 注入逻辑。

---

## 7. 验收交付物（每个 Sprint 的 PR 应包含）
- 变更代码 + 说明（CHANGELOG） + 迁移 SQL（若 DB 更改）  
- 自动化测试通过截图/CI 链接  
- 回滚/恢复步骤说明  
- 小型运行手册或 README 更新（如何在本地/生产运行）

---

## 8. 估算汇总（粗略）
- Quick Wins: 1–3 人天  
- Core: 5–12 人天  
- Enhancements: 5–15 人天  
- Java 21 升级与大型重构: 5–20 人天（取决复杂度）

---

## 9. 下步建议（请选择）
- 选项 A：我代为生成 Sprint 0 的 PR（Flyway 初始化迁移 + `password` length + role 规范化），并在本地验证后交付一个可 review 的 PR。  
- 选项 B（你已选）：仅把本 Markdown 报告存为仓库文档（我已完成）。  
- 选项 C：按上面 Roadmap 逐项实施（需你逐步批准每个 PR）。

---

## 附录：常用命令与快速操作示例
```powershell
# 运行应用（开发）
.\mvnw spring-boot:run

# 本地打包
.\mvnw -DskipTests=true package

# 运行 Flyway（Maven 插件）
.\mvnw flyway:migrate -Dflyway.url=jdbc:mysql://localhost:3306/devnote -Dflyway.user=root -Dflyway.password=...
```

---

如需我将该 Markdown 直接保存到其他位置或把文件内容输出到你的剪贴板（或生成 PR），告诉我你的偏好。