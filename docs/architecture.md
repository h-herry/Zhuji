# 筑基 (Zhuji) 架构设计文档

## 目录
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

### 1.1 设计原则
- **领域驱动设计 (DDD)**: 以业务领域为核心，划分限界上下文
- **云原生架构**: 容器化部署，支持弹性伸缩
- **微服务架构**: 服务自治，独立部署和扩展
- **前后端分离**: API 驱动，前端通过网关访问后端服务

### 1.2 整体架构图
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

### 2.1 前端接入层 (Client Layer)
**职责**: 提供用户界面和交互体验

**技术栈**:
- Web端: React 18+ / Vue 3+
- 移动端: Flutter / React Native
- 小程序: 微信小程序 / 支付宝小程序
- 管理端: Vue 3 + Element Plus

**关键设计**:
- SPA (Single Page Application) 架构
- 响应式设计，支持多终端适配
- PWA 支持，可离线访问
- 统一的状态管理 (Redux / Pinia)

### 2.2 API 网关层 (Gateway Layer)
**职责**: 统一入口，负责路由、鉴权、限流、熔断

**核心组件**:
1. **路由转发**: 根据请求路径路由到对应微服务
2. **统一鉴权**: JWT/OAuth2 认证 + 权限校验
3. **限流熔断**: Sentinel 保护后端服务
4. **协议转换**: HTTP/WebSocket 转换
5. **请求日志**: 记录所有请求，便于审计

**配置示例**:
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
**职责**: 实现核心业务逻辑

**设计模式**:
- **DDD 分层**:
  - `interfaces`: 用户接口层（API 控制器）
  - `application`: 应用层（用例编排）
  - `domain`: 领域层（核心业务逻辑）
  - `infrastructure`: 基础设施层（数据访问、外部服务）

**服务划分**:
| 服务名 | 职责 | 数据库 |
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
**职责**: 封装数据访问逻辑，提供统一的数据访问接口

**技术选型**:
- **ORM**: MyBatis-Plus（兼顾性能和灵活性）
- **分库分表**: ShardingSphere（应对大数据量）
- **读写分离**: MyBatis-Plus 多数据源 + ShardingSphere

**最佳实践**:
```java
// 使用 MyBatis-Plus 的 Lambda 查询，避免硬编码字段名
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getUsername, username)
       .eq(User::getStatus, 1)
       .orderByDesc(User::getCreateTime);
```

### 2.5 基础设施层 (Infrastructure Layer)
**职责**: 提供通用的技术能力

**核心组件**:
| 组件 | 作用 | 技术选型 |
|------|------|---------|
| 服务注册与发现 | 服务自动注册和发现 | Nacos |
| 配置中心 | 集中化管理配置，动态刷新 | Nacos Config |
| 限流熔断 | 保护服务，防止雪崩 | Sentinel |
| 分布式事务 | 跨服务事务一致性 | Seata (AT 模式) |
| 链路追踪 | 请求调用链追踪 | SkyWalking |
| 监控告警 | 指标采集、可视化、告警 | Prometheus + Grafana |
| 日志中心 | 日志收集、存储、查询 | ELK (Elasticsearch + Logstash + Kibana) |
| 消息队列 | 异步处理、解耦、削峰 | RabbitMQ / Kafka |
| 分布式缓存 | 缓存热点数据，提升性能 | Redis Cluster |
| 对象存储 | 存储文件、图片、视频 | MinIO / 阿里云 OSS |

---

## 3. 技术选型

### 3.1 核心框架
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 快速开发框架 |
| Spring Cloud | 2023.x | 微服务框架 |
| Spring Cloud Alibaba | 2023.x | 阿里云微服务生态 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| Maven | 3.9+ | 构建工具 |

### 3.2 数据库
| 技术 | 版本 | 说明 |
|------|------|------|
| MySQL | 8.0+ | 主数据库 |
| Redis | 7.x | 缓存 + 分布式锁 |
| ShardingSphere | 5.4+ | 分库分表 |

### 3.3 消息队列
| 技术 | 版本 | 说明 |
|------|------|------|
| RabbitMQ | 3.12+ | 业务消息队列 |
| Kafka | 3.6+ | 日志、大数据场景 |

### 3.4 服务治理
| 技术 | 版本 | 说明 |
|------|------|------|
| Nacos | 2.3+ | 服务注册、配置中心 |
| Sentinel | 1.8+ | 限流熔断 |
| Seata | 1.7+ | 分布式事务 |

### 3.5 监控运维
| 技术 | 版本 | 说明 |
|------|------|------|
| SkyWalking | 9.x | APM 系统 |
| Prometheus | 2.48+ | 监控系统 |
| Grafana | 10.x | 可视化平台 |
| ELK Stack | 8.x | 日志平台 |

### 3.6 容器化
| 技术 | 版本 | 说明 |
|------|------|------|
| Docker | 24.x | 容器引擎 |
| Kubernetes | 1.28+ | 容器编排 |
| Helm | 3.13+ | K8s 包管理 |

---

## 4. 模块依赖

### 4.1 模块依赖图
```
┌───────────────────────────────────────────────────────────────┐
│                    zhuji-starter (启动器)                      │
│  · 依赖所有模块的 API 接口                                      │
│  · 提供 Spring Boot 自动配置                                   │
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
| 模块 | 依赖 | 被依赖 | 说明 |
|------|------|--------|------|
| **common-core** | 无 | 所有模块 | 核心工具类、异常处理、基础实体 |
| **common-security** | common-core | 所有需要鉴权的模块 | JWT认证、权限控制、用户信息获取 |
| **common-i18n** | common-core | 所有需要国际化的模块 | 多语言支持、数据库消息管理、缓存机制 |
| **common-cache** | common-core, Redis | 所有需要缓存的模块 | Redis缓存、分布式锁、缓存工具 |
| **common-log** | common-core, SkyWalking | 所有模块 | 日志切面、链路追踪 |
| **common-config** | common-core, Nacos | 所有模块 | Nacos配置中心集成 |
| **common-mq** | common-core, RabbitMQ | 所有需要异步处理的模块 | 消息队列、消息监听器 |
| **common-task** | common-core | 所有需要定时任务的模块 | Spring Scheduler定时任务、任务日志 |
| **common-monitor** | common-core | 所有模块 | Prometheus监控、Zipkin链路追踪 |
| **common-crypto** | common-core, Jasypt | 所有需要加密的模块 | 数据加密、接口签名校验 |
| **common-export** | common-core, EasyExcel | 所有需要导出的模块 | Excel导出、大文件导出 |
| **common-audit** | common-core, common-security | 所有需要审计的模块 | 操作审计日志 |
| **user-org-service** | common-* | api-gateway | 用户组织管理（多角色/多组织/可配置化） |
| **workflow-service** | common-*, Flowable | api-gateway | Flowable工作流引擎 |
| **notification-service** | common-*, common-mq | api-gateway | 消息通知服务 |
| **file-service** | common-* | api-gateway | 文件上传存储服务 |
| **third-party-service** | common-* | api-gateway | 第三方平台集成服务 |
| **system-service** | common-* | api-gateway | 系统参数配置服务 |
| **system-monitor** | common-*, common-monitor | api-gateway | 系统监控服务 |
| **api-gateway** | common-*, Spring Cloud Gateway | 前端、第三方 | API网关 |

### 4.3 Maven 依赖管理
```xml
<!-- 父 POM 统一管理依赖版本 -->
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
    </dependencies>
</dependencyManagement>
```

---

## 5. 数据流转

### 5.1 请求处理流程
```
客户端 → API 网关 → 微服务 → 数据库
   │         │         │        │
   │         │         │        └─ 返回数据
   │         │         └─ 业务逻辑处理
   │         └─ 鉴权、限流、路由
   └─ HTTPS 请求
```

**详细流程**:
1. 客户端发送 HTTPS 请求到 API 网关
2. API 网关进行统一鉴权（JWT 校验）
3. 鉴权通过后，Sentinel 进行限流熔断检查
4. 检查通过后，网关根据路由规则转发请求到对应微服务
5. 微服务接收请求，执行业务逻辑
6. 微服务访问数据库获取数据
7. 数据库返回数据给微服务
8. 微服务返回响应给网关
9. 网关返回响应给客户端

### 5.2 异步处理流程
```
客户端 → API 网关 → 微服务 → 消息队列 → 异步服务
   │         │         │        │          │
   │         │         │        └─ 异步处理 │
   │         │         └─ 发送消息         └─ 返回结果
   │         └─ 转发请求
   └─ 请求
```

**使用场景**:
- 发送邮件/短信通知
- 生成报表
- 数据同步
- 日志记录

### 5.3 分布式事务流程
```
服务 A → 服务 B → 服务 C
   │        │        │
   │        │        └─ 执行成功，注册分支事务
   │        └─ 执行成功，注册分支事务
   └─ 执行成功，注册分支事务
   
TM (Transaction Manager) 统一提交/回滚所有分支事务
```

**Seata AT 模式**:
1. 一阶段: 执行业务 SQL，生成前后镜像
2. 二阶段: 根据全局事务状态提交或回滚

---

## 6. 安全设计

### 6.1 认证 (Authentication)
**JWT (JSON Web Token)**:
- 无状态认证，适合微服务架构
- Token 包含用户信息和权限
- 支持过期时间和刷新 Token

**OAuth2**:
- 第三方登录（微信、钉钉、GitHub）
- 授权码模式、密码模式、客户端模式

**实现示例**:
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
**RBAC (Role-Based Access Control)**:
- 用户 → 角色 → 权限
- 支持角色继承
- 支持权限细粒度控制

**数据权限**:
- 用户只能访问自己创建的数据
- 用户只能访问自己部门的数据
- 通过 MyBatis 拦截器自动添加数据权限过滤条件

### 6.3 防护 (Protection)
| 威胁 | 防护措施 |
|------|---------|
| XSS | 输入过滤 + 输出转义 |
| CSRF | Token 验证 + SameSite Cookie |
| SQL 注入 | 使用预编译语句 (PreparedStatement) |
| 暴力破解 | 登录失败次数限制 + 验证码 |
| DDoS | 限流 + 熔断 + IP 黑名单 |
| 敏感数据泄露 | 数据加密 + HTTPS |

---

## 7. 性能优化

### 7.1 缓存策略
**一级缓存**: JVM 本地缓存 (Caffeine)
- 缓存热点数据（如配置信息）
- 容量小，访问速度快

**二级缓存**: Redis 分布式缓存
- 缓存用户会话、权限信息
- 支持分布式部署

**缓存更新策略**:
- Cache-Aside: 先更新数据库，再删除缓存
- Read-Through: 缓存未命中时自动加载数据
- Write-Through: 同时更新缓存和数据库

### 7.2 数据库优化
**索引优化**:
- 为常用查询字段建立索引
- 避免索引失效（函数、类型转换）
- 使用覆盖索引减少回表

**SQL 优化**:
- 避免 SELECT *，只查询需要的字段
- 使用 JOIN 代替子查询
- 批量操作代替循环单条操作

**分库分表**:
- 垂直分库: 按业务拆分数据库
- 水平分表: 按用户 ID 取模分表

### 7.3 服务优化
**线程池配置**:
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
```java
@Async("taskExecutor")
public CompletableFuture<String> processAsync() {
    // 异步处理逻辑
}
```

**连接池配置**:
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

### 8.1 插件化架构
**SPI (Service Provider Interface)**:
- 定义标准接口
- 第三方实现接口
- 通过配置文件动态加载实现类

**示例**:
```java
// 定义接口
public interface NotificationService {
    void send(String message);
}

// 配置文件: META-INF/services/com.zhuji.NotificationService
com.zhuji.notification.EmailNotificationService
com.zhuji.notification.SmsNotificationService
```

### 8.2 事件驱动架构
**Spring Event**:
```java
// 发布事件
applicationEventPublisher.publishEvent(new UserCreatedEvent(user));

// 监听事件
@EventListener
public void handleUserCreated(UserCreatedEvent event) {
    // 发送欢迎邮件
}
```

**分布式事件**:
- 使用 RabbitMQ/Kafka 实现跨服务事件传递
- 保证事件最终一致性

### 8.3 多租户支持
**数据隔离方案**:
1. **独立数据库**: 每个租户一个数据库（隔离性最好，成本最高）
2. **共享数据库，独立 Schema**: 每个租户一个 Schema
3. **共享数据库，共享 Schema**: 所有租户共享表，通过 Tenant ID 区分（成本最低，隔离性最差）

**实现方式**:
```java
// MyBatis 拦截器自动添加 Tenant ID 过滤条件
@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class TenantInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 添加 Tenant ID 过滤条件
    }
}
```

---

## 附录

### A. 参考文档
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Cloud 官方文档](https://spring.io/projects/spring-cloud)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [DDD 领域驱动设计](https://domainlanguage.com/)

### B. 术语表
| 术语 | 说明 |
|------|------|
| DDD | Domain-Driven Design，领域驱动设计 |
| RBAC | Role-Based Access Control，基于角色的访问控制 |
| JWT | JSON Web Token，JSON Web 令牌 |
| APM | Application Performance Monitoring，应用性能监控 |

---

**文档版本**: v1.1  
**最后更新**: 2026-05-21  
**维护者**: 筑基架构团队
