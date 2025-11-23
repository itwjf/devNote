# DevNote 博客系统

> 💬 本项目为个人技术展示项目，旨在求职后端开发岗位时展示 Spring Boot 全栈开发能力与系统架构设计水平。  
> 展现了从用户体系、权限控制到文章管理、关注系统的完整实现。

---

## 🌟 项目简介

**DevNote** 是一个基于 **Spring Boot + JPA + MySQL** 构建的个人博客系统，支持用户注册、登录、发布文章、编辑资料、用户主页展示、粉丝关注等功能。  
该项目以“简洁、实用、可扩展”为目标，体现了后端系统的分层设计与高可维护性。

---
## 💡 项目亮点

- 🔐 **Spring Security 权限体系**：支持登录认证与未登录访问限制。
- 🧩 **分层架构设计**：Controller → Service → Repository → Entity 层次清晰。
- 👤 **用户模块完善**：包括用户注册、登录、主页展示、资料编辑、头像上传。
- 🔁 **关注系统实现**：支持关注/取消关注、粉丝与关注数统计。
- 🧱 **JPA + MySQL 数据持久层**：使用 Repository 自动生成 SQL，简洁高效。
- 🪶 **Thymeleaf 前端模板引擎**：简洁直观的页面展示层。
- ⚙️ **可扩展性设计**：项目架构为后续功能（点赞、收藏、搜索、私信）留出接口。

---

## 🛠️ 技术栈

| 技术                  | 说明              |
|---------------------|-----------------|
| **Spring Boot**     | 后端框架（快速开发、自动配置） |
| **Spring Security** | 安全与权限控制         |
| **Spring Data JPA** | ORM 持久化层        |
| **MySQL**           | 数据存储            |
| **Thymeleaf**       | 前端模板渲染引擎        |
| **BCrypt**          | 用户密码加密          |
| **Lombok**          | 简化实体类代码         |
| **HTML + CSS**      | 页面展示            |

---

## 🏗️ 系统架构
```markdown
com.example.devnote
├── controller # 控制层：处理请求
├── service # 业务逻辑层
├── repository # 数据访问层（JPA接口）
├── entity # 数据实体类（User / Post / Follow / Like / Favorite / Comment）
├── config # 安全与配置类(Spring Security配置 / WebConfig)
└── templates # 前端模板 (Thymeleaf)
```


## ✅ 已实现功能

### 🧍 用户模块
- 用户注册与登录（Spring Security + BCrypt）
- 用户主页展示（个人信息 + 文章列表）
- 编辑个人资料（支持头像上传与简介修改）
- 登录状态检测（未登录不能发文）

### 🧠 文章模块
- 创建、查看、编辑、删除文章
- 首页文章按时间倒序显示
- 文章详情页展示

### 💞 用户关注模块（重点）
- 关注与取消关注功能（Follow / Unfollow）
- 防止重复关注与自我关注校验
- 实时粉丝与关注统计展示
- 数据表设计：`Follow` 表独立存储关注关系（follower_id, following_id）

---

## 🚀 如何运行

### 克隆项目：
   ```bash
   git clone https://gitee.com/ItWjf/devnote-blog.git
   ```
### 配置数据库： 
   确保已安装 MySQL，在application.yml中修改数据库配置
```yml
   spring:
   datasource:
      # 连接本地 MySQL，使用端口 3306
      url: jdbc:mysql://localhost:3306/devnote?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
      username: root
      password: 你的密码
      driver-class-name: com.mysql.cj.jdbc.Driver
```

### 启动项目
./mvnw spring-boot:run

### 访问应用
http://localhost:8080

## 📝 开发说明
本项目通过 Spring Boot 提供动态页面服务，不可使用 VS Code Live Server 等静态服务器预览。
所有页面跳转必须通过 http://localhost:8080 访问，以确保后端路由正常工作。

## 作者信息
* 作者：王君福
* 项目目标：求职后端开发岗位
* 联系邮箱：junfu_wang@163.com


## 🙌 感谢
感谢 Spring Boot 官方文档与开源社区，本项目为学习实践项目。