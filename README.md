# 筑基 (Zhuji) - Java 企业级快速开发脚手架

## 🎯 项目定位
**筑基**是一个基于领域驱动设计(DDD)和云原生架构的Java底层架构脚手架，提供企业级应用开发的全套基础设施和最佳实践，帮助开发者快速构建高质量、可扩展的微服务应用。

## 📦 项目结构
```
zhuji/
├── common-core/              # 公共核心模块（全局常量/枚举/异常定义/基础工具类）
├── common-security/          # 公共安全模块（Spring Security配置/JWT生成与校验/OAuth2资源服务器）
├── common-cache/             # 公共缓存模块（Redis封装/缓存抽象层/分布式锁）
├── common-log/               # 公共日志模块（Logstash/Prometheus/Grafana/SkyWalking）
├── common-config/            # 公共配置模块（Nacos配置加载/动态配置刷新）
├── common-mq/                # 公共消息队列模块
├── common-i18n/              # 公共国际化模块（多语言支持/多语言可配置化）
├── user-org-service/         # 用户/组织架构/角色/权限管理（多角色/多组织/可配置化）
├── workflow-service/         # 工作流引擎集成（Flowable 7.0）
├── notification-service/     # 通知服务（邮件/短信/推送）
├── file-service/             # 文件服务（上传/下载/存储）
├── third-party-service/      # 第三方集成服务（API适配器/协议转换）
├── system-service/           # 系统参数服务（数据字典/参数配置/分类管理）
├── system-monitor/           # 系统监控服务（健康检查/性能指标/告警管理）
├── api-gateway/              # API网关
├── infra/                    # 基础设施配置模块
│   ├── sql/                  # SQL脚本模块
│   │   ├── init_user_org.sql      # 用户组织数据库初始化
│   │   ├── init_user_org_extend.sql # 用户组织扩展（配置化）数据库初始化
│   │   ├── init_notification.sql  # 通知服务数据库初始化
│   │   ├── init_file.sql          # 文件服务数据库初始化
│   │   └── init_system.sql        # 系统参数数据库初始化
│   ├── redis/                # Redis配置模块
│   ├── logstash/             # Logstash配置模块
│   ├── prometheus/           # Prometheus配置模块
│   ├── grafana/              # Grafana配置模块
│   ├── skywalking/           # SkyWalking配置模块
│   ├── seata/                # Seata配置模块
│   └── docker-compose.yml    # Docker编排文件模块
├── docs/                     # 项目文档模块
└── README.md                 # 项目说明文档模块
```

## 🏗️ 架构设计

### 整体架构分层
```
┌─────────────────────────────────────────────────────────────────┐
│                    前端接入层 (Frontend Layer)                   │
│                  React / Vue / 移动端 / 小程序                   │
└─────────────────────────────────────────────────────────────────┘
                              │ HTTPS/WebSocket
┌─────────────────────────────────────────────────────────────────┐
│              API 网关层 (API Gateway Layer)                      │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  Spring Cloud Gateway                                    │   │
│   │  ✓ 统一鉴权 (JWT/OAuth2)  ✓ 限流熔断 (Sentinel)       │   │
│   │  ✓ 路由转发              ✓ 协议转换                      │   │
│   └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │ REST/gRPC
┌─────────────────────────────────────────────────────────────────┐
│               服务层 (Service Layer - DDD架构)                   │
│   ┌──────────────┬──────────────┬──────────────┬──────────────┐ │
│   │ 用户组织服务  │  多语言服务   │  API集成服务  │  工作流服务  │ │
│   │ (UserOrg)   │   (i18n)     │ (ThirdParty) │ (Workflow) │ │
│   │              │              │              │              │ │
│   │ · 多角色管理 │ · 资源加载   │ · 适配器模式  │ · Flowable │ │
│   │ · 多组织管理 │ · 语言切换   │ · 协议转换    │ · 流程引擎  │ │
│   │ · 可配置化   │ · 热加载     │ · 重试机制    │ · 节点流转  │ │
│   │ · 权限控制   │ · 多语言配置 │              │ · 状态机    │ │
│   └──────────────┴──────────────┴──────────────┴──────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │ JPA/MyBatis
┌─────────────────────────────────────────────────────────────────┐
│              基础设施层 (Infrastructure Layer)                    │
│   · 数据持久化: MySQL + ShardingSphere + MyBatis-Plus           │
│   · 缓存体系:   Redis Cluster + 分布式锁                        │
│   · 消息队列:   RabbitMQ/Kafka + 事务消息                       │
│   · 服务治理:   Nacos + Sentinel + Seata                        │
│   · 监控体系:   SkyWalking + Prometheus + Grafana               │
│   · 日志体系:   SLF4J + Logback + ELK                          │
└─────────────────────────────────────────────────────────────────┘
```

## 📦 模块化设计

### 核心模块划分
| 模块名称 | 职责说明 | 关键技术 |
|---------|---------|---------|
| **common-core** | 全局常量/枚举/异常定义/基础工具类 | Lombok, Hutool |
| **common-security** | Spring Security配置/JWT生成与校验 | JWT, Spring Security, Sentinel |
| **common-cache** | Redis封装/缓存抽象层/分布式锁 | RedisTemplate, Spring Cache |
| **common-log** | 日志规范/日志拦截/链路追踪 | SLF4J, Logback, MDC, ELK |
| **common-config** | Nacos配置加载/动态配置刷新 | Nacos Config |
| **common-mq** | 消息队列封装/统一消息接口 | RabbitMQ, Kafka |
| **common-i18n** | 国际化支持/多语言切换/资源热加载/多语言可配置化 | Spring MessageSource |
| **user-org-service** | 用户/组织架构/角色/权限管理（多角色/多组织/可配置化） | MyBatis-Plus, RBAC, Seata |
| **workflow-service** | Flowable工作流引擎/流程定义/实例管理/任务流转 | Flowable 7.0 |
| **notification-service** | 邮件/短信/推送通知服务 | Spring Mail, RabbitMQ |
| **file-service** | 文件上传/下载/存储服务 | MinIO, Local Storage |
| **third-party-service** | 第三方集成/API适配器/协议转换/重试机制 | Feign, HttpClient, Retry |
| **system-service** | 系统参数/数据字典/参数配置/分类管理 | MyBatis-Plus, i18n |
| **system-monitor** | 系统监控/健康检查/性能指标/告警管理 | Micrometer, Prometheus, Actuator |
| **api-gateway** | 网关路由/鉴权/限流/熔断 | Spring Cloud Gateway |

## 🛠️ 技术栈清单
| 类别 | 技术选型 | 版本 |
|------|---------|------|
| **基础框架** | Spring Boot + Spring Cloud | 3.2.x / 2023.x |
| **ORM** | MyBatis-Plus + 动态SQL | 3.5+ |
| **缓存** | Redis Cluster + Spring Cache | 7.x |
| **服务治理** | Nacos + Sentinel | 2.3+ / 1.8+ |
| **消息队列** | RabbitMQ + Kafka | 3.12+ / 3.6+ |
| **分布式事务** | Seata | 1.7+ |
| **链路追踪** | SkyWalking + Zipkin | 9.x |
| **监控告警** | Prometheus + Grafana | 2.x |
| **日志收集** | ELK (ElasticSearch + Logstash + Kibana) | 8.x |
| **工作流引擎** | Flowable | 7.0.0 |
| **容器化** | Docker + Docker Compose | 24.x |
| **自动化构建** | Maven (多模块) | 3.9+ |
| **Java版本** | JDK 17+ | 21 |

## ✨ 功能特性

### 已完成功能
- [x] **用户组织管理** - 用户CRUD/角色权限/组织架构树/多角色/多组织/可配置化
- [x] **工作流引擎** - Flowable 7.0 集成/流程定义/发布/实例管理/任务流转
- [x] **多语言支持** - 中英文切换/资源热加载/多语言可配置化/数据库消息管理
- [x] **通知服务** - 邮件/短信/推送/微信多渠道发送
- [x] **文件服务** - 本地存储/MinIO云存储/分片上传
- [x] **系统参数配置** - 数据字典/参数配置/分类管理/多语言支持
- [x] **系统监控** - 健康检查/性能指标/内存/线程/CPU监控
- [x] **API网关** - 路由转发/JWT鉴权/限流熔断
- [x] **第三方集成** - 适配器模式/HTTP调用/短信发送/重试机制
- [x] **公共核心模块** - 统一响应/异常处理/工具类/雪花ID生成
- [x] **公共安全模块** - JWT认证/权限控制/用户信息获取
- [x] **公共缓存模块** - Redis配置/分布式锁/缓存工具
- [x] **公共日志模块** - 日志切面/链路追踪
- [x] **测试覆盖** - JUnit 5/Mockito/Rest Assured/JaCoCo代码覆盖率
- [x] **配置中心** - Nacos配置中心集成/配置热更新
- [x] **服务注册与发现** - Nacos服务注册与发现
- [x] **限流熔断** - Sentinel限流熔断集成
- [x] **链路追踪** - Zipkin分布式链路追踪
- [x] **数据库迁移** - Flyway数据库迁移
- [x] **消息队列** - RabbitMQ消息队列集成
- [x] **定时任务** - Spring Scheduler定时任务
- [x] **API文档增强** - Knife4j注解完善
- [x] **监控告警** - Prometheus+Grafana监控告警
- [x] **安全加固** - 接口签名校验/敏感信息加密
- [x] **数据导出** - EasyExcel数据导出
- [x] **审计日志** - 操作审计日志

### 核心特性详解

#### 1. 用户管理可配置化
- **多角色支持**：用户可同时拥有多个角色，支持设置主角色
- **多组织支持**：用户可同时属于多个组织，支持设置主组织
- **用户配置管理**：支持用户级别的自定义配置
- **角色配置管理**：支持角色级别的自定义配置
- **组织配置管理**：支持组织级别的自定义配置
- **全局配置管理**：支持系统级别的全局配置

#### 2. 多语言可配置化
- **语言切换API**：支持运行时切换语言
- **多语言支持**：简体中文、英文、繁体中文、日语、韩语
- **配置化语言**：支持的语言列表可在数据库配置
- **Cookie持久化**：语言设置通过Cookie持久化
- **数据库消息管理**：支持通过API动态管理多语言消息
- **消息提供者机制**：支持数据库和properties文件双源消息，数据库优先
- **缓存刷新**：支持手动刷新消息缓存

#### 3. Flowable工作流集成
- **流程定义管理**：流程模型的创建、编辑、部署
- **流程实例管理**：流程实例的启动、查询、挂起、终止
- **任务管理**：任务的查询、领取、完成、转交
- **历史记录**：流程历史、任务历史的查询
- **流程变量**：支持复杂业务数据的流转

## 🚀 快速开始

### 前置要求
- JDK 17+
- Maven 3.9+
- MySQL 8.0+ (需支持JSON类型)
- Redis 7.x+
- Nacos 2.3+ (可选)
- Docker & Docker Compose (可选)

### 编译项目
```bash
# 克隆项目
git clone https://github.com/h-herry/Zhuji.git
cd zhuji

# 编译项目
mvn clean compile -DskipTests

# 打包项目
mvn clean package -DskipTests
```

### 初始化数据库
```bash
# 执行用户组织服务数据库初始化
mysql -u root -p < infra/sql/init_user_org.sql
mysql -u root -p < infra/sql/init_user_org_extend.sql

# 执行其他服务数据库初始化...
```

### 启动服务
```bash
# 启动基础设施 (使用Docker)
cd infra
docker-compose up -d

# 启动用户组织服务
cd user-org-service
mvn spring-boot:run

# 启动工作流服务
cd workflow-service
mvn spring-boot:run

# 启动其他服务...
```

### API文档
启动服务后访问 Knife4j 文档:
- 用户组织服务: http://localhost:8081/doc.html
- 工作流服务: http://localhost:8084/doc.html
- 通知服务: http://localhost:8082/doc.html
- 文件服务: http://localhost:8083/doc.html
- 第三方服务: http://localhost:8085/doc.html
- 系统参数服务: http://localhost:8086/doc.html
- 系统监控服务: http://localhost:8087/doc.html

### 监控端点
系统监控服务提供以下监控端点：
- 健康检查: GET /api/v1/monitor/health
- 系统指标: GET /api/v1/monitor/metrics
- Actuator: GET /actuator/health, /actuator/prometheus, /actuator/info

### API调用示例

#### 用户多角色分配
```bash
# 为用户分配多个角色
POST /api/v1/users/{userId}/roles
Content-Type: application/json

[1, 2, 3]  # 角色ID列表

# 参数说明：
# - 第一个角色自动设为主角色（可通过isPrimary参数控制）
# - 支持同时分配多个角色
```

#### 语言切换
```bash
# 切换语言
POST /api/v1/i18n/switch?lang=en_US

# 获取支持的语言列表
GET /api/v1/i18n/languages

# 获取当前语言
GET /api/v1/i18n/current
```

#### 多语言消息管理
```bash
# 分页查询多语言消息
GET /api/v1/i18n/messages?page=1&size=10&locale=zh_CN

# 创建或更新多语言消息
POST /api/v1/i18n/messages
Content-Type: application/json
{
  "messageKey": "common.success",
  "locale": "zh_CN",
  "messageValue": "操作成功",
  "module": "common"
}

# 获取单个语言的所有消息
GET /api/v1/i18n/messages/locale/zh_CN

# 刷新消息缓存
POST /api/v1/i18n/messages/refresh

# 测试获取消息
GET /api/v1/i18n/messages/test?messageKey=common.success&locale=zh_CN
```

#### 测试相关
```bash
# 运行单元测试
mvn test

# 运行测试并生成覆盖率报告
mvn clean test jacoco:report

# 查看覆盖率报告
# 打开 target/site/jacoco/index.html
```

### 🚀 新增功能详解

#### 1. Nacos 配置中心
- **配置管理** - 支持从Nacos配置中心加载配置
- **配置热更新** - 支持配置变更后动态刷新
- **配置隔离** - 支持命名空间和分组隔离

#### 2. Nacos 服务注册与发现
- **服务注册** - 自动注册服务到Nacos
- **服务发现** - 支持通过服务名发现其他服务
- **负载均衡** - 集成Spring Cloud LoadBalancer

#### 3. Sentinel 限流熔断
- **限流规则** - 支持QPS限流、线程数限流
- **熔断降级** - 支持慢调用比例、异常比例熔断
- **规则持久化** - 支持通过Nacos持久化限流规则
- **实时监控** - 集成Sentinel Dashboard

#### 4. Zipkin 链路追踪
- **分布式追踪** - 支持全链路追踪
- **依赖分析** - 自动分析服务依赖关系
- **性能分析** - 可查看各服务调用耗时

#### 5. Flyway 数据库迁移
- **版本管理** - 自动管理数据库版本
- **SQL迁移** - 支持SQL脚本迁移
- **基线管理** - 支持基线设置

#### 6. RabbitMQ 消息队列
- **消息发送** - 支持多种消息发送模式
- **消息消费** - 支持手动/自动确认
- **消息重试** - 支持消息重试机制

#### 7. Spring Scheduler 定时任务
- **Cron表达式** - 支持Cron表达式配置
- **任务日志** - 自动记录任务执行日志
- **任务耗时** - 自动记录任务执行耗时

#### 8. Prometheus+Grafana 监控
- **指标采集** - 自动采集JVM、HTTP等指标
- **自定义指标** - 支持自定义业务指标
- **可视化** - 集成Grafana仪表板

#### 9. 安全加固
- **接口签名** - 支持接口签名校验
- **数据加密** - 支持敏感数据加密存储
- **Jasypt集成** - 配置文件加密

#### 10. EasyExcel 导出
- **大文件导出** - 支持百万级数据导出
- **注解驱动** - 支持注解配置导出样式
- **高性能** - 基于Sax解析，内存占用低

#### 11. 审计日志
- **操作记录** - 自动记录用户操作
- **请求响应** - 支持记录请求和响应
- **日志注解** - 通过注解快速配置

## 📖 文档目录

- [架构设计](docs/architecture.md) - 系统架构详细说明
- [模块开发指南](docs/module-development.md) - 如何开发新的业务模块
- [基础设施指南](docs/infrastructure-guide.md) - 中间件配置与使用
- [部署指南](docs/deployment.md) - 生产环境部署方案
- [API文档](docs/api-docs.md) - 服务API接口文档

## 🎓 技术亮点

### 1. DDD分层架构
采用领域驱动设计，将业务逻辑分为应用层、领域层、基础设施层，保持领域模型的纯粹性

### 2. 用户管理可配置化
通过多角色、多组织的灵活配置，实现精细化的权限管理和组织架构设计

### 3. Flowable工作流引擎集成
集成业界领先的开源工作流引擎Flowable 7.0，支持复杂的业务流程编排

### 4. 国际化(i18n)设计
使用 Spring MessageSource 实现多语言支持，支持中英文切换，可扩展更多语言

### 5. 适配器模式集成第三方服务
通过统一的 `ApiAdapter` 接口，我们可以轻松扩展新的第三方服务集成，如短信、支付、地图等

### 6. 重试机制
集成 Spring Retry 和熔断器，提高系统容错能力

### 7. 全面监控体系
集成 Micrometer + Prometheus + Grafana，提供全面的系统监控能力

## 📄 License

MIT License

## 🙏 致谢

感谢 JetBrains 提供的 IDEA 开发工具支持。
