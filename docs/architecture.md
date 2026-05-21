# 筑基 (Zhuji) 架构设计文档
# Zhuji Architecture Design Document

## 目录
## Table of Contents
1. [架构概览](#1-架构概览)
2. [分层设计](#2-分层设计)
3. [技术选型](#3-技术选型)
4. [模块依赖](#4-模块依赖)
5. [数据流转](#5-数据流转)
6. [安全设计](#6-安全设计)
7. [性能优化](#7-性能优化)
8. [扩展性设计](#8-扩展性设计)

---

## 1. 架构概览
## 1. Architecture Overview

### 1.1 设计原则
### 1.1 Design Principles
- **领域驱动设计 (DDD)**: 以业务领域为核心，划分限界上下文
- **Domain-Driven Design (DDD)**: Focus on business domain, define bounded contexts
- **云原生架构**: 容器化部署，支持弹性伸缩
- **Cloud Native Architecture**: Containerized deployment, supports elastic scaling
- **微服务架构**: 服务自治，独立部署和扩展
- **Microservices Architecture**: Service autonomy, independent deployment and scaling
- **前后端分离**: API 驱动，前端通过网关访问后端服务
- **Frontend-Backend Separation**: API-driven, frontend accesses backend services through gateway

### 1.2 整体架构图
### 1.2 Overall Architecture Diagram
```
┌─────────────────────────────────────────────────────────────────────┐
│                         客户端层 (Client Layer)                      │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐       │
│  │  Web端   │ 移动端    │ 小程序    │ 第三方    │  管理端  │       │
│  │ (React)  │(Flutter) │(微信/支付宝)│(API调用) │ (Vue)   │       │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘       │
└─────────────────────────────────────────────────────────────────────┘
                              │ HTTPS (TLS 1.3)
┌─────────────────────────────────────────────────────────────────────┐
│                      API 网关层 (Gateway Layer)                     │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │            Spring Cloud Gateway                               │  │
│   │  ┌──────────┬──────────┬──────────┬──────────┐            │  │
│   │  │ 统一鉴权  │ 限流熔断  │ 路由转发  │ 协议转换  │            │  │
│   │  │ (JWT/OA) │(Sentinel)│(LoadBal) │(HTTP/WS) │            │  │
│   │  └──────────┴──────────┴──────────┴──────────┘            │  │
│   └─────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              │ REST/gRPC
┌─────────────────────────────────────────────────────────────────────┐
│                      服务层 (Service Layer)                          │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │                  业务微服务 (Microservices)                  │  │
│   │  ┌──────────┬──────────┬──────────┬──────────┐            │  │
│   │  │用户组织   │ 多语言    │ API集成   │ 工作流   │            │  │
│   │  │服务      │ 服务      │ 服务      │ 服务     │            │  │
│   │  └──────────┴──────────┴──────────┴──────────┘            │  │
│   │  ┌──────────┬──────────┬──────────┬──────────┐            │  │
│   │  │ 通知服务  │ 文件服务  │ 定时任务  │ 监控服务  │            │  │
│   │  └──────────┴──────────┴──────────┴──────────┘            │  │
│   └─────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              │ JPA/MyBatis
┌─────────────────────────────────────────────────────────────────────┐
│                    数据访问层 (Data Access Layer)                     │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │              MyBatis-Plus + ShardingSphere                   │  │
│   └─────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              │ JDBC
┌─────────────────────────────────────────────────────────────────────┐
│                      基础设施层 (Infrastructure)                      │
│   ┌──────────┬──────────┬──────────┬──────────┬──────────┐       │
│   │  MySQL   │  Redis   │ RabbitMQ │  Nacos   │ Sentinel │       │
│   │ (主从)    │ (Cluster)│          │(配置中心) │(限流)    │       │
│   └──────────┴──────────┴──────────┴──────────┴──────────┘       │
│   ┌──────────┬──────────┬──────────┬──────────┬──────────┐       │
│   │ Seata    │SkyWalking│Prometheus│ Grafana  │  ELK     │       │
│   │(分布式事务)│(链路追踪) │(指标采集) │(可视化)  │(日志)    │       │
│   └──────────┴──────────┴──────────┴──────────┴──────────┘       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. 分层设计
## 2. Layered Design

### 2.1 前端接入层 (Client Layer)
### 2.1 Client Layer
**职责**: 提供用户界面和交互体验
**Responsibility**: Provide user interface and interaction experience

**技术栈**:
**Tech Stack**:
- Web端: React 18+ / Vue 3+
- Web: React 18+ / Vue 3+
- 移动端: Flutter / React Native
- Mobile: Flutter / React Native
- 小程序: 微信小程序 / 支付宝小程序
- Mini Program: WeChat Mini Program / Alipay Mini Program
- 管理端: Vue 3 + Element Plus
- Admin Panel: Vue 3 + Element Plus

**关键设计**:
**Key Design**:
- SPA (Single Page Application) 架构
- SPA (Single Page Application) architecture
- 响应式设计，支持多终端适配
- Responsive design, supports multi-terminal adaptation
- PWA 支持，可离线访问
- PWA support, offline access available
- 统一的状态管理 (Redux / Pinia)
- Unified state management (Redux / Pinia)

### 2.2 API 网关层 (Gateway Layer)
### 2.2 API Gateway Layer
**职责**: 统一入口，负责路由、鉴权、限流、熔断
**Responsibility**: Unified entry point, responsible for routing, authentication, rate limiting, circuit breaking

**核心组件**:
**Core Components**:
1. **路由转发**: 根据请求路径路由到对应微服务
2. **Route Forwarding**: Route requests to corresponding microservices based on paths
3. **统一鉴权**: JWT/OAuth2 认证 + 权限校验
4. **Unified Authentication**: JWT/OAuth2 authentication + permission verification
5. **限流熔断**: Sentinel 保护后端服务
6. **Rate Limiting & Circuit Breaking**: Sentinel protects backend services
7. **协议转换**: HTTP/WebSocket 转换
8. **Protocol Conversion**: HTTP/WebSocket conversion
9. **请求日志**: 记录所有请求，便于审计
10. **Request Logging**: Record all requests for audit purposes

**配置示例**:
**Configuration Example**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-org-service
          uri: lb://user-org-service
          predicates:
            - Path=/api/v1/users/**
          filters:
            - StripPrefix=1
            - name: SentinelGatewayFilter
```

### 2.3 服务层 (Service Layer)
### 2.3 Service Layer
**职责**: 实现核心业务逻辑
**Responsibility**: Implement core business logic

**设计模式**:
**Design Patterns**:
- **DDD 分层**:
- **DDD Layering**:
  - `interfaces`: 用户接口层（API 控制器）
  - `interfaces`: Interface layer (API controllers)
  - `application`: 应用层（用例编排）
  - `application`: Application layer (use case orchestration)
  - `domain`: 领域层（核心业务逻辑）
  - `domain`: Domain layer (core business logic)
  - `infrastructure`: 基础设施层（数据访问、外部服务）
  - `infrastructure`: Infrastructure layer (data access, external services)

**服务划分**:
**Service Division**:
| 服务名 | 职责 | 数据库 |
| Service Name | Responsibility | Database |
|--------|------|--------|
| user-org-service | 用户、角色、权限、组织架构（多角色/多组织/可配置化） | user_org_db |
| workflow-service | Flowable工作流引擎（流程定义/实例/任务管理） | workflow_db |
| notification-service | 消息通知（邮件、短信、推送） | notification_db |
| file-service | 文件上传、存储、分发 | file_db |
| third-party-service | 第三方平台集成（适配器模式/重试机制） | third_party_db |
| system-service | 系统参数、数据字典、参数配置 | system_db |
| system-monitor | 系统监控、健康检查、性能指标 | monitor_db |
| api-gateway | API网关（路由/鉴权/限流） | - |

### 2.4 数据访问层 (Data Access Layer)
### 2.4 Data Access Layer
**职责**: 封装数据访问逻辑，提供统一的数据访问接口
**Responsibility**: Encapsulate data access logic, provide unified data access interface

**技术选型**:
**Technology Selection**:
- **ORM**: MyBatis-Plus（兼顾性能和灵活性）
- **ORM**: MyBatis-Plus (balances performance and flexibility)
- **分库分表**: ShardingSphere（应对大数据量）
- **Database Sharding**: ShardingSphere (handles large data volumes)
- **读写分离**: MyBatis-Plus 多数据源 + ShardingSphere
- **Read-Write Separation**: MyBatis-Plus multi-datasource + ShardingSphere

**最佳实践**:
**Best Practices**:
```java
// 使用 MyBatis-Plus 的 Lambda 查询，避免硬编码字段名
// Use MyBatis-Plus Lambda query to avoid hardcoding field names
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getUsername, username)
       .eq(User::getStatus, 1)
       .orderByDesc(User::getCreateTime);
```

### 2.5 基础设施层 (Infrastructure Layer)
### 2.5 Infrastructure Layer
**职责**: 提供通用的技术能力
**Responsibility**: Provide common technical capabilities

**核心组件**:
**Core Components**:
| 组件 | 作用 | 技术选型 |
| Component | Purpose | Technology Selection |
|------|------|---------|
| 服务注册与发现 | 服务自动注册和发现 | Nacos |
| Service Discovery | Service auto-registration and discovery | Nacos |
| 配置中心 | 集中化管理配置，动态刷新 | Nacos Config |
| Configuration Center | Centralized config management, dynamic refresh | Nacos Config |
| 限流熔断 | 保护服务，防止雪崩 | Sentinel |
| Rate Limiting & Circuit Breaking | Protect services, prevent cascade failures | Sentinel |
| 分布式事务 | 跨服务事务一致性 | Seata (AT 模式) |
| Distributed Transaction | Cross-service transaction consistency | Seata (AT mode) |
| 链路追踪 | 请求调用链追踪 | SkyWalking |
| Distributed Tracing | Request call chain tracing | SkyWalking |
| 监控告警 | 指标采集、可视化、告警 | Prometheus + Grafana |
| Monitoring & Alerting | Metric collection, visualization, alerting | Prometheus + Grafana |
| 日志中心 | 日志收集、存储、查询 | ELK (Elasticsearch + Logstash + Kibana) |
| Log Center | Log collection, storage, query | ELK (Elasticsearch + Logstash + Kibana) |
| 消息队列 | 异步处理、解耦、削峰 | RabbitMQ / Kafka |
| Message Queue | Async processing, decoupling, traffic shaping | RabbitMQ / Kafka |
| 分布式缓存 | 缓存热点数据，提升性能 | Redis Cluster |
| Distributed Cache | Cache hot data, improve performance | Redis Cluster |
| 对象存储 | 存储文件、图片、视频 | MinIO / 阿里云 OSS |
| Object Storage | Store files, images, videos | MinIO / Alibaba Cloud OSS |

---

## 3. 技术选型
## 3. Technology Stack

### 3.1 核心框架
### 3.1 Core Frameworks
| 技术 | 版本 | 说明 |
| Technology | Version | Description |
|------|------|------|
| Spring Boot | 3.2.x | 快速开发框架 | Rapid development framework |
| Spring Cloud | 2023.x | 微服务框架 | Microservices framework |
| Spring Cloud Alibaba | 2023.x | 阿里云微服务生态 | Alibaba Cloud microservices ecosystem |
| MyBatis-Plus | 3.5.x | ORM 框架 | ORM framework |
| Maven | 3.9+ | 构建工具 | Build tool |

### 3.2 数据库
### 3.2 Database
| 技术 | 版本 | 说明 |
| Technology | Version | Description |
|------|------|------|
| MySQL | 8.0+ | 主数据库 | Primary database |
| Redis | 7.x | 缓存 + 分布式锁 | Cache + distributed lock |
| ShardingSphere | 5.4+ | 分库分表 | Database sharding |

### 3.3 消息队列
### 3.3 Message Queue
| 技术 | 版本 | 说明 |
| Technology | Version | Description |
|------|------|------|
| RabbitMQ | 3.12+ | 业务消息队列 | Business message queue |
| Kafka | 3.6+ | 日志、大数据场景 | Logging, big data scenarios |

### 3.4 服务治理
### 3.4 Service Governance
| 技术 | 版本 | 说明 |
| Technology | Version | Description |
|------|------|------|
| Nacos | 2.3+ | 服务注册、配置中心 | Service registration, config center |
| Sentinel | 1.8+ | 限流熔断 | Rate limiting and circuit breaking |
| Seata | 1.7+ | 分布式事务 | Distributed transaction |

### 3.5 监控运维
### 3.5 Monitoring and Operations
| 技术 | 版本 | 说明 |
| Technology | Version | Description |
|------|------|------|
| SkyWalking | 9.x | APM 系统 | APM system |
| Prometheus | 2.48+ | 监控系统 | Monitoring system |
| Grafana | 10.x | 可视化平台 | Visualization platform |
| ELK Stack | 8.x | 日志平台 | Logging platform |

### 3.6 容器化
### 3.6 Containerization
| 技术 | 版本 | 说明 |
| Technology | Version | Description |
|------|------|------|
| Docker | 24.x | 容器引擎 | Container engine |
| Kubernetes | 1.28+ | 容器编排 | Container orchestration |
| Helm | 3.13+ | K8s 包管理 | K8s package manager |

---

## 4. 模块依赖
## 4. Module Dependencies

### 4.1 模块依赖图
### 4.1 Module Dependency Diagram
```
┌───────────────────────────────────────────────────────────────┐
│                    zhuji-starter (启动器)                      │
│  · 依赖所有模块的 API 接口                                      │
│  · Depends on API interfaces of all modules                     │
│  · 提供 Spring Boot 自动配置                                   │
│  · Provides Spring Boot auto-configuration                      │
└───────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼────────┐  ┌───────▼────────┐  ┌───────▼────────┐
│ common-module  │  │ service-module │  │ gateway-module │
│ (通用模块)      │  │ (业务模块)      │  │ (网关模块)      │
└───────┬────────┘  └───────┬────────┘  └───────┬────────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
                    ┌────────▼────────┐
                    │ infrastructure │
                    │ (基础设施模块)   │
                    └─────────────────┘
```

### 4.2 模块说明
### 4.2 Module Description
| 模块 | 依赖 | 被依赖 | 说明 |
| Module | Dependencies | Dependents | Description |
|------|------|--------|------|
| **common-core** | 无 | 所有模块 | 核心工具类、异常处理、基础实体 |
| Core utilities, exception handling, base entities |
| **common-security** | common-core | 所有需要鉴权的模块 | JWT认证、权限控制、用户信息获取 |
| JWT authentication, permission control, user info retrieval |
| **common-i18n** | common-core | 所有需要国际化的模块 | 多语言支持、数据库消息管理、缓存机制 |
| Multi-language support, database message management, caching mechanism |
| **common-cache** | common-core, Redis | 所有需要缓存的模块 | Redis缓存、分布式锁、缓存工具 |
| Redis cache, distributed lock, cache utilities |
| **common-log** | common-core, SkyWalking | 所有模块 | 日志切面、链路追踪 |
| Logging aspect, distributed tracing |
| **common-config** | common-core, Nacos | 所有模块 | Nacos配置中心集成 |
| Nacos config center integration |
| **common-mq** | common-core, RabbitMQ | 所有需要异步处理的模块 | 消息队列、消息监听器 |
| Message queue, message listeners |
| **common-task** | common-core | 所有需要定时任务的模块 | Spring Scheduler定时任务、任务日志 |
| Spring Scheduler tasks, task logging |
| **common-monitor** | common-core | 所有模块 | Prometheus监控、Zipkin链路追踪 |
| Prometheus monitoring, Zipkin tracing |
| **common-crypto** | common-core, Jasypt | 所有需要加密的模块 | 数据加密、接口签名校验 |
| Data encryption, API signature verification |
| **common-export** | common-core, EasyExcel | 所有需要导出的模块 | Excel导出、大文件导出 |
| Excel export, large file export |
| **common-audit** | common-core, common-security | 所有需要审计的模块 | 操作审计日志 |
| Operation audit logs |
| **user-org-service** | common-* | api-gateway | 用户组织管理（多角色/多组织/可配置化） |
| User org management (multi-role/multi-org/configurable) |
| **workflow-service** | common-*, Flowable | api-gateway | Flowable工作流引擎 |
| Flowable workflow engine |
| **notification-service** | common-*, common-mq | api-gateway | 消息通知服务 |
| Message notification service |
| **file-service** | common-* | api-gateway | 文件上传存储服务 |
| File upload and storage service |
| **third-party-service** | common-* | api-gateway | 第三方平台集成服务 |
| Third-party integration service |
| **system-service** | common-* | api-gateway | 系统参数配置服务 |
| System parameter config service |
| **system-monitor** | common-*, common-monitor | api-gateway | 系统监控服务 |
| System monitoring service |
| **api-gateway** | common-*, Spring Cloud Gateway | 前端、第三方 | API网关 |
| API gateway |

### 4.3 Maven 依赖管理
### 4.3 Maven Dependency Management
```xml
<!-- 父 POM 统一管理依赖版本 -->
<!-- Parent POM for centralized dependency version management -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.zhuji</groupId>
            <artifactId>common-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.zhuji</groupId>
            <artifactId>common-security</artifactId>
            <version>${project.version}</version>
        </dependency>
        <!-- 其他模块 -->
        <!-- Other modules -->
    </dependencies>
</dependencyManagement>
```

---

## 5. 数据流转
## 5. Data Flow

### 5.1 请求处理流程
### 5.1 Request Processing Flow
```
客户端 → API 网关 → 微服务 → 数据库
Client → API Gateway → Microservice → Database
   │         │         │        │
   │         │         │        └─ 返回数据
   │         │         │        └─ Return data
   │         │         └─ 业务逻辑处理
   │         │         └─ Business logic processing
   │         └─ 鉴权、限流、路由
   │         └─ Authentication, rate limiting, routing
   └─ HTTPS 请求
   └─ HTTPS Request
```

**详细流程**:
**Detailed Flow**:
1. 客户端发送 HTTPS 请求到 API 网关
2. Client sends HTTPS request to API Gateway
3. API 网关进行统一鉴权（JWT 校验）
4. API Gateway performs unified authentication (JWT verification)
5. 鉴权通过后，Sentinel 进行限流熔断检查
6. After authentication passes, Sentinel checks rate limiting and circuit breaking
7. 检查通过后，网关根据路由规则转发请求到对应微服务
8. After checks pass, gateway forwards request to corresponding microservice based on routing rules
9. 微服务接收请求，执行业务逻辑
10. Microservice receives request, executes business logic
11. 微服务访问数据库获取数据
12. Microservice accesses database to retrieve data
13. 数据库返回数据给微服务
14. Database returns data to microservice
15. 微服务返回响应给网关
16. Microservice returns response to gateway
17. 网关返回响应给客户端
18. Gateway returns response to client

### 5.2 异步处理流程
### 5.2 Async Processing Flow
```
客户端 → API 网关 → 微服务 → 消息队列 → 异步服务
Client → API Gateway → Microservice → Message Queue → Async Service
   │         │         │        │          │
   │         │         │        └─ 异步处理 │
   │         │         │        └─ Async processing
   │         │         └─ 发送消息
   │         │         └─ Send message
   │         └─ 转发请求
   │         └─ Forward request
   └─ 请求
   └─ Request
```

**使用场景**:
**Use Cases**:
- 发送邮件/短信通知
- Send email/SMS notifications
- 生成报表
- Generate reports
- 数据同步
- Data synchronization
- 日志记录
- Logging

### 5.3 分布式事务流程
### 5.3 Distributed Transaction Flow
```
服务 A → 服务 B → 服务 C
Service A → Service B → Service C
   │        │        │
   │        │        └─ 执行成功，注册分支事务
   │        │        └─ Execute successfully, register branch transaction
   │        └─ 执行成功，注册分支事务
   │        └─ Execute successfully, register branch transaction
   └─ 执行成功，注册分支事务
   └─ Execute successfully, register branch transaction
   
TM (Transaction Manager) 统一提交/回滚所有分支事务
TM (Transaction Manager) uniformly commits/rolls back all branch transactions
```

**Seata AT 模式**:
**Seata AT Mode**:
1. 一阶段: 执行业务 SQL，生成前后镜像
2. Phase 1: Execute business SQL, generate before and after images
3. 二阶段: 根据全局事务状态提交或回滚
4. Phase 2: Commit or rollback based on global transaction status

---

## 6. 安全设计
## 6. Security Design

### 6.1 认证 (Authentication)
### 6.1 Authentication
**JWT (JSON Web Token)**:
- 无状态认证，适合微服务架构
- Stateless authentication, suitable for microservices architecture
- Token 包含用户信息和权限
- Token contains user information and permissions
- 支持过期时间和刷新 Token
- Supports expiration time and refresh token

**OAuth2**:
- 第三方登录（微信、钉钉、GitHub）
- Third-party login (WeChat, DingTalk, GitHub)
- 授权码模式、密码模式、客户端模式
- Authorization Code, Password, Client Credentials modes

**实现示例**:
**Implementation Example**:
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
```

### 6.2 授权 (Authorization)
### 6.2 Authorization
**RBAC (Role-Based Access Control)**:
- 用户 → 角色 → 权限
- User → Role → Permission
- 支持角色继承
- Supports role inheritance
- 支持权限细粒度控制
- Supports fine-grained permission control

**数据权限**:
**Data Permissions**:
- 用户只能访问自己创建的数据
- Users can only access data created by themselves
- 用户只能访问自己部门的数据
- Users can only access data from their own department
- 通过 MyBatis 拦截器自动添加数据权限过滤条件
- Auto-add data permission filter conditions via MyBatis interceptor

### 6.3 防护 (Protection)
### 6.3 Protection
| 威胁 | 防护措施 |
| Threat | Protection Measures |
|------|---------|
| XSS | 输入过滤 + 输出转义 |
| Input filtering + output escaping |
| CSRF | Token 验证 + SameSite Cookie |
| SQL 注入 | 使用预编译语句 (PreparedStatement) |
| Use prepared statements |
| 暴力破解 | 登录失败次数限制 + 验证码 |
| Login failure count limit + CAPTCHA |
| DDoS | 限流 + 熔断 + IP 黑名单 |
| Rate limiting + circuit breaking + IP blacklist |
| 敏感数据泄露 | 数据加密 + HTTPS |
| Data encryption + HTTPS |

---

## 7. 性能优化
## 7. Performance Optimization

### 7.1 缓存策略
### 7.1 Caching Strategy
**一级缓存**: JVM 本地缓存 (Caffeine)
**Level 1 Cache**: JVM Local Cache (Caffeine)
- 缓存热点数据（如配置信息）
- Cache hot data (e.g., configuration info)
- 容量小，访问速度快
- Small capacity, fast access

**二级缓存**: Redis 分布式缓存
**Level 2 Cache**: Redis Distributed Cache
- 缓存用户会话、权限信息
- Cache user sessions, permission info
- 支持分布式部署
- Supports distributed deployment

**缓存更新策略**:
**Cache Update Strategy**:
- Cache-Aside: 先更新数据库，再删除缓存
- First update database, then delete cache
- Read-Through: 缓存未命中时自动加载数据
- Auto-load data when cache miss
- Write-Through: 同时更新缓存和数据库
- Update cache and database simultaneously

### 7.2 数据库优化
### 7.2 Database Optimization
**索引优化**:
**Index Optimization**:
- 为常用查询字段建立索引
- Index frequently queried fields
- 避免索引失效（函数、类型转换）
- Avoid index invalidation (functions, type conversion)
- 使用覆盖索引减少回表
- Use covering indexes to reduce table lookups

**SQL 优化**:
**SQL Optimization**:
- 避免 SELECT *，只查询需要的字段
- Avoid SELECT *, only query needed fields
- 使用 JOIN 代替子查询
- Use JOIN instead of subqueries
- 批量操作代替循环单条操作
- Batch operations instead of looped single operations

**分库分表**:
**Database Sharding**:
- 垂直分库: 按业务拆分数据库
- Vertical sharding: Split databases by business
- 水平分表: 按用户 ID 取模分表
- Horizontal sharding: Shard tables by user ID modulus

### 7.3 服务优化
### 7.3 Service Optimization
**线程池配置**:
**Thread Pool Configuration**:
```yaml
spring:
  task:
    execution:
      pool:
        core-size: 8
        max-size: 16
        queue-capacity: 100
```

**异步处理**:
**Async Processing**:
```java
@Async("taskExecutor")
public CompletableFuture<String> processAsync() {
    // 异步处理逻辑
    // Async processing logic
}
```

**连接池配置**:
**Connection Pool Configuration**:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

---

## 8. 扩展性设计
## 8. Scalability Design

### 8.1 插件化架构
### 8.1 Plugin Architecture
**SPI (Service Provider Interface)**:
- 定义标准接口
- Define standard interfaces
- 第三方实现接口
- Third-party implements interfaces
- 通过配置文件动态加载实现类
- Dynamically load implementations via config file

**示例**:
**Example**:
```java
// 定义接口
// Define interface
public interface NotificationService {
    void send(String message);
}

// 配置文件: META-INF/services/com.zhuji.NotificationService
com.zhuji.notification.EmailNotificationService
com.zhuji.notification.SmsNotificationService
```

### 8.2 事件驱动架构
### 8.2 Event-Driven Architecture
**Spring Event**:
```java
// 发布事件
// Publish event
applicationEventPublisher.publishEvent(new UserCreatedEvent(user));

// 监听事件
// Listen to event
@EventListener
public void handleUserCreated(UserCreatedEvent event) {
    // 发送欢迎邮件
    // Send welcome email
}
```

**分布式事件**:
**Distributed Events**:
- 使用 RabbitMQ/Kafka 实现跨服务事件传递
- Use RabbitMQ/Kafka for cross-service event delivery
- 保证事件最终一致性
- Guarantee eventual consistency

### 8.3 多租户支持
### 8.3 Multi-Tenancy Support
**数据隔离方案**:
**Data Isolation Solutions**:
1. **独立数据库**: 每个租户一个数据库（隔离性最好，成本最高）
2. **独立数据库**: One database per tenant (best isolation, highest cost)
3. **共享数据库，独立 Schema**: 每个租户一个 Schema
4. **共享数据库，独立 Schema**: One schema per tenant
5. **共享数据库，共享 Schema**: 所有租户共享表，通过 Tenant ID 区分（成本最低，隔离性最差）
6. **共享数据库，共享 Schema**: All tenants share tables, differentiated by Tenant ID (lowest cost, worst isolation)

**实现方式**:
**Implementation**:
```java
// MyBatis 拦截器自动添加 Tenant ID 过滤条件
// MyBatis interceptor auto-adds Tenant ID filter conditions
@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class TenantInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 添加 Tenant ID 过滤条件
        // Add Tenant ID filter conditions
    }
}
```

---

## 附录
## Appendix

### A. 参考文档
### A. References
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Cloud 官方文档](https://spring.io/projects/spring-cloud)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [DDD 领域驱动设计](https://domainlanguage.com/)

### B. 术语表
### B. Glossary
| 术语 | 说明 |
| Term | Description |
|------|------|
| DDD | Domain-Driven Design，领域驱动设计 |
| RBAC | Role-Based Access Control，基于角色的访问控制 |
| JWT | JSON Web Token，JSON Web 令牌 |
| APM | Application Performance Monitoring，应用性能监控 |

---

**文档版本**: v1.1  
**Document Version**: v1.1  
**最后更新**: 2026-05-21  
**Last Update**: 2026-05-21  
**维护者**: 筑基架构团队
**Maintainer**: Zhuji Architecture Team
