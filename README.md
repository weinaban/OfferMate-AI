# OfferMate AI

OfferMate AI 是一个面向 PC 端的智能招聘平台，业务形态参考 Boss 直聘，覆盖求职者、招聘者、管理员三类角色。项目以 Spring Boot 单体后端为核心，配套 Vue3 前端，实现了从企业维护、岗位发布、简历管理、岗位投递、实时沟通、面试邀请到后台审核的招聘业务闭环，并结合 AI 能力提供简历优化、岗位匹配和模拟面试等求职辅助功能。

本项目适合作为 Java 后端秋招展示项目，重点体现：业务建模、权限控制、缓存与消息队列、全文搜索、WebSocket 实时通信、对象存储、AI 接入和 Docker 化部署能力。

## 技术栈

**后端**

- Java 17
- Spring Boot 3.3.x
- MyBatis-Plus
- MySQL
- JWT + 拦截器 + ThreadLocal
- Redis
- RabbitMQ
- Elasticsearch 8.x
- MinIO
- WebSocket
- Spring AI
- LangChain4j

**前端**

- Vue3
- JavaScript
- Vite
- Element Plus

**部署**

- Docker Compose
- Nginx
- MySQL
- Redis
- RabbitMQ
- Elasticsearch
- MinIO

## 核心功能

### 用户与权限

- 用户注册、登录、退出
- JWT 登录校验与 Token 黑名单
- 求职者、招聘者、管理员三类角色权限控制
- ThreadLocal 保存当前登录用户上下文

### 招聘业务闭环

- 招聘者维护企业信息
- 招聘者发布、修改、下架岗位
- 求职者在线创建简历、设置默认简历
- 求职者投递岗位，防重复投递
- 招聘者查看收到的投递并更新投递状态
- 面试邀请发送、接受、拒绝、取消
- 管理后台审核企业和岗位

### 实时聊天

- WebSocket 实时聊天
- JWT 校验 WebSocket 连接
- 聊天会话与消息落库
- 未读消息统计
- 会话列表展示对方名称、头像、公司、岗位等信息

### AI 求职助手

- AI 简历优化
- AI 分模块优化技能、项目经历、自我评价
- AI 生成岗位描述
- AI 生成面试题
- LangChain4j 岗位匹配分析
- LangChain4j 模拟面试多轮问答、评分与报告
- AI 调用日志与 Redis 每日限流

### 搜索、文件与通知

- Elasticsearch 岗位全文搜索
- Redis 岗位缓存、投递锁、Token 黑名单、聊天未读缓存
- RabbitMQ 异步通知与 AI 日志异步写入
- MinIO 文件上传，支持头像、企业 Logo、附件简历预留
- 操作日志与审计日志

## 项目亮点

- **招聘业务闭环完整**：覆盖用户、企业、岗位、简历、投递、聊天、面试、通知、后台审核等核心流程。
- **工程化能力充分**：接入 Redis、RabbitMQ、Elasticsearch、MinIO、WebSocket，并提供统一异常处理、参数校验、审计日志和权限边界校验。
- **AI 能力贴合业务**：不是简单聊天接口，而是围绕简历优化、岗位匹配、面试题生成和模拟面试构建实际场景。
- **可部署性强**：支持本地启动和 Docker Compose 部署，适合演示、二次开发和学习。
- **后端结构清晰**：Controller、Service、Mapper、DTO、VO 分层明确，保持黑马项目风格，便于阅读和扩展。

## 系统架构

```text
Vue3 PC Web
   |
   | HTTP REST / WebSocket
   v
Spring Boot Backend
   |
   |-- MySQL：核心业务数据
   |-- Redis：缓存、限流、黑名单、防重复操作
   |-- RabbitMQ：异步通知、AI 日志
   |-- Elasticsearch：岗位搜索
   |-- MinIO：头像、Logo、附件对象存储
   |-- DashScope / Qwen：Spring AI 与 LangChain4j AI 能力
```

更多架构说明见：[docs/系统架构.md](docs/系统架构.md)

## 本地启动方式

### 1. 准备基础环境

- JDK 17
- Maven 3.8+
- Node.js 18+
- MySQL 8.x
- Redis
- RabbitMQ
- Elasticsearch 8.x
- MinIO

### 2. 初始化数据库

```sql
create database if not exists offermate
  default character set utf8mb4
  collate utf8mb4_unicode_ci;
```

执行初始化 SQL：

```bash
mysql --default-character-set=utf8mb4 -h localhost -P 3306 -u root -p offermate < offermate-server/src/main/resources/sql/offermate.sql
```

### 3. 配置后端

公开仓库只提交 `application.yml` 和 `application-example.yml`，本地真实配置放在 `application-dev.yml`，该文件已加入 `.gitignore`。

复制示例配置：

```bash
cp offermate-server/src/main/resources/application-example.yml offermate-server/src/main/resources/application-dev.yml
```

然后修改 `offermate-server/src/main/resources/application-dev.yml`，重点配置：

- MySQL
- Redis
- RabbitMQ
- Elasticsearch
- MinIO
- DashScope API Key

建议使用环境变量保存密钥，不要把真实 API Key、数据库密码、JWT Secret、MinIO SecretKey 等敏感信息提交到 GitHub。

### 4. 启动后端

```bash
cd offermate-server
mvn spring-boot:run
```

默认后端地址：

```text
http://localhost:8080
```

### 5. 启动前端

```bash
cd offermate-web
npm install
npm run dev
```

默认前端地址：

```text
http://localhost:5173
```

## Docker Compose 部署方式

项目支持使用 Docker Compose 编排 MySQL、Redis、RabbitMQ、Elasticsearch、MinIO、后端服务和 Nginx 前端服务。部署前请确认已安装：

- Docker
- Docker Compose

推荐流程：

```bash
docker compose up -d
```

部署配置说明见：[docs/部署说明.md](docs/部署说明.md)

## 演示账号

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 求职者 | zhangsan | 123456 | 简历、投递、聊天、AI 求职助手 |
| 招聘者 | recruiter1 | 123456 | 企业信息、岗位发布、投递处理、面试邀请 |
| 管理员 | admin | 123456 | 用户管理、企业审核、岗位审核、操作日志 |

> 实际演示账号以数据库初始化数据为准。

## 页面截图

截图建议放在 `docs/images/` 目录。

### 首页

![首页](docs/images/home.png)

### 岗位详情

![岗位详情](docs/images/job-detail.png)

### 简历管理

![简历管理](docs/images/resume.png)

### 实时聊天

![实时聊天](docs/images/chat.png)

### AI 模拟面试

![AI 模拟面试](docs/images/ai-interview.png)

### 管理后台

![管理后台](docs/images/admin.png)

## 目录结构

```text
OfferMate-AI
├── offermate-server                 # Spring Boot 后端
│   ├── src/main/java/com/offermate
│   │   ├── controller               # 接口层
│   │   ├── service                  # 业务层
│   │   ├── mapper                   # MyBatis-Plus Mapper
│   │   ├── entity                   # 数据库实体
│   │   ├── dto                      # 请求 DTO
│   │   ├── vo                       # 响应 VO
│   │   ├── config                   # 配置类
│   │   ├── interceptor              # JWT 拦截器
│   │   ├── websocket                # WebSocket 聊天
│   │   ├── mq                       # RabbitMQ 消息
│   │   ├── es                       # Elasticsearch 搜索
│   │   └── util                     # 工具类
│   └── src/main/resources
│       ├── application.yml          # 公共配置，提交 GitHub
│       ├── application-example.yml  # 示例配置，提交 GitHub
│       ├── application-dev.yml      # 本地真实配置，不提交 GitHub
│       └── sql/offermate.sql
├── offermate-web                    # Vue3 前端
│   ├── src
│   ├── package.json
│   └── vite.config.js
└── docs                             # 项目文档
    ├── 项目介绍.md
    ├── 系统架构.md
    ├── 接口说明.md
    ├── 数据库设计.md
    └── 部署说明.md
```

## 后续规划

- 完善 Docker Compose 一键部署脚本和 Nginx 配置模板
- 补充接口自动化测试和核心业务单元测试
- 增加企业端数据看板
- 增加简历附件解析和简历完整度分析
- 优化 Elasticsearch 中文分词和搜索相关度
- 增加通知中心 WebSocket 实时推送
- 完善 CI/CD 流程

## 文档

- [项目介绍](docs/项目介绍.md)
- [系统架构](docs/系统架构.md)
- [接口说明](docs/接口说明.md)
- [数据库设计](docs/数据库设计.md)
- [部署说明](docs/部署说明.md)
