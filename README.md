# OfferMate AI

OfferMate AI 是一个面向 PC 端的智能招聘平台，覆盖求职者、招聘者和管理员三类角色。项目围绕招聘业务主流程展开，实现了企业信息维护、岗位发布、简历管理、岗位投递、实时聊天、面试邀请、通知中心、后台审核等能力，并接入 AI 能力辅助简历优化、岗位匹配和模拟面试。

项目采用前后端分离架构，后端基于 Spring Boot 3.3.x 和 Java 17，前端基于 Vue3、Vite 和 Element Plus。后端同时结合 Redis、RabbitMQ、Elasticsearch、MinIO、WebSocket、Spring AI、LangChain4j 等组件，尽量贴近真实业务系统的工程实践。

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

### 求职者端

- 注册、登录、退出
- 创建和维护在线简历
- 设置默认简历
- 浏览、筛选和搜索岗位
- 投递岗位并查看投递进度
- 接收面试邀请并进行接受或拒绝操作
- 与招聘者进行 WebSocket 实时聊天
- 使用 AI 简历优化、岗位匹配、模拟面试等功能

### 招聘者端

- 维护企业资料
- 发布、修改、下架岗位
- 查看岗位收到的投递
- 修改投递状态
- 基于投递记录发送面试邀请
- 与求职者实时沟通
- 使用 AI 生成岗位描述和面试题

### 管理端

- 用户分页查询与启用/禁用
- 企业认证审核
- 岗位审核
- 操作日志查询

## 项目亮点

- **招聘业务闭环完整**：覆盖企业、岗位、简历、投递、沟通、面试邀请、通知和后台审核等核心流程。
- **实时沟通能力**：基于 WebSocket 实现聊天会话、消息落库、未读数统计和在线推送。
- **AI 求职助手**：结合 Spring AI 与 LangChain4j，实现简历优化、岗位匹配、面试题生成和多轮模拟面试。
- **搜索体验优化**：使用 Elasticsearch 构建岗位搜索能力，支持关键词、城市、薪资、学历、经验、行业等条件筛选。
- **异步通知设计**：使用 RabbitMQ 解耦投递通知、面试通知、审核通知和 AI 日志写入。
- **缓存与限流**：使用 Redis 实现岗位缓存、Token 黑名单、防重复投递、AI 调用限流和聊天未读数缓存。
- **文件对象存储**：使用 MinIO 存储头像、企业 Logo，并预留附件简历上传能力。
- **工程化补充**：包含参数校验、统一异常处理、统一返回结构、操作日志、权限边界校验和配置示例。

## 系统架构

```text
Vue3 PC Web
   |
   | HTTP REST / WebSocket
   v
Spring Boot Backend
   |
   |-- MySQL            核心业务数据
   |-- Redis            缓存、限流、黑名单、防重复操作
   |-- RabbitMQ         异步通知、AI 日志
   |-- Elasticsearch    岗位搜索
   |-- MinIO            文件对象存储
   |-- DashScope/Qwen   AI 能力
```

更多说明见：[docs/系统架构.md](docs/系统架构.md)

## 本地运行

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.x
- Redis
- RabbitMQ
- Elasticsearch 8.x
- MinIO

### 1. 初始化数据库

先创建数据库：

```sql
create database if not exists offermate
  default character set utf8mb4
  collate utf8mb4_unicode_ci;
```

导入初始化脚本：

```bash
mysql --default-character-set=utf8mb4 -h <MYSQL_HOST> -P <MYSQL_PORT> -u <MYSQL_USER> -p <MYSQL_DATABASE> < offermate-server/src/main/resources/sql/offermate.sql
```

示例中的 `<MYSQL_HOST>`、`<MYSQL_PORT>`、`<MYSQL_USER>`、`<MYSQL_DATABASE>` 请替换为自己的本地配置。

### 2. 准备后端配置

后端提供了一份示例配置：

```text
offermate-server/src/main/resources/application-example.yml
```

复制为本地开发配置：

```bash
cp offermate-server/src/main/resources/application-example.yml offermate-server/src/main/resources/application-dev.yml
```

然后根据自己的环境填写 MySQL、Redis、RabbitMQ、Elasticsearch、MinIO 和 AI 服务配置。

### 3. 启动后端

```bash
cd offermate-server
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

### 4. 启动前端

```bash
cd offermate-web
npm install
npm run dev
```

默认地址：

```text
http://localhost:5173
```

## Docker Compose 部署

项目可以使用 Docker Compose 编排 MySQL、Redis、RabbitMQ、Elasticsearch、MinIO、后端服务和 Nginx 前端服务。部署说明见：

[docs/部署说明.md](docs/部署说明.md)

## 演示账号

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 求职者 | zhangsan | 123456 | 简历、投递、聊天、AI 求职助手 |
| 招聘者 | recruiter1 | 123456 | 企业信息、岗位发布、投递处理、面试邀请 |
| 管理员 | admin | 123456 | 用户管理、企业审核、岗位审核、操作日志 |

> 演示账号仅用于本地初始化数据和功能体验，实际部署时请自行调整。

## 目录结构

```text
OfferMate-AI
├── offermate-server
│   ├── src/main/java/com/offermate
│   │   ├── controller       接口层
│   │   ├── service          业务层
│   │   ├── mapper           数据访问层
│   │   ├── entity           数据库实体
│   │   ├── dto              请求对象
│   │   ├── vo               响应对象
│   │   ├── config           配置类
│   │   ├── interceptor      登录拦截器
│   │   ├── websocket        实时聊天
│   │   ├── mq               RabbitMQ 消息
│   │   ├── es               Elasticsearch 搜索
│   │   └── util             工具类
│   └── src/main/resources
│       ├── application.yml
│       ├── application-example.yml
│       └── sql/offermate.sql
├── offermate-web
│   ├── src
│   ├── package.json
│   └── vite.config.js
└── docs
    ├── 项目介绍.md
    ├── 系统架构.md
    ├── 接口说明.md
    ├── 数据库设计.md
    └── 部署说明.md
```

## 文档

- [项目介绍](docs/项目介绍.md)
- [系统架构](docs/系统架构.md)
- [接口说明](docs/接口说明.md)
- [数据库设计](docs/数据库设计.md)
- [部署说明](docs/部署说明.md)

## 后续规划

- 补充更完整的 Docker Compose 和 Nginx 模板
- 增加核心业务接口自动化测试
- 优化 Elasticsearch 中文分词和相关度排序
- 增加简历附件解析能力
- 扩展通知中心实时推送
- 完善 CI/CD 流程

