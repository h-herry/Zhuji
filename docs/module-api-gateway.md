# API网关 (api-gateway)

## 1. 模块概述

API网关是系统的统一入口，提供路由转发、认证授权、限流熔断、日志记录等功能，是微服务架构的核心组件。

### 1.1 主要功能

- **路由转发**：根据路径转发到对应服务
- **认证授权**：JWT Token验证
- **限流熔断**：Sentinel限流熔断
- **请求日志**：记录请求访问日志
- **跨域处理**：统一跨域配置

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Cloud Gateway | 2023.x | 网关框架 |
| Sentinel | 1.8.x | 限流熔断 |
| Redis | 7.x | 分布式缓存 |
| Nacos | 2.x | 服务发现 |

---

## 2. 路由配置

### 2.1 路由规则

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-org-service
          uri: lb://user-org-service
          predicates:
            - Path=/api/v1/auth/**
            - Path=/api/v1/users/**
            - Path=/api/v1/roles/**
            - Path=/api/v1/permissions/**
            - Path=/api/v1/orgs/**
          filters:
            - StripPrefix=0

        - id: notification-service
          uri: lb://notification-service
          predicates:
            - Path=/api/v1/notifications/**
          filters:
            - StripPrefix=0

        - id: file-service
          uri: lb://file-service
          predicates:
            - Path=/api/v1/files/**
          filters:
            - StripPrefix=0

        - id: system-monitor
          uri: lb://system-monitor
          predicates:
            - Path=/api/v1/monitor/**
          filters:
            - StripPrefix=0
```

### 2.2 服务映射

| 路径 | 目标服务 | 说明 |
|------|----------|------|
| /api/v1/auth/** | user-org-service | 认证接口 |
| /api/v1/users/** | user-org-service | 用户接口 |
| /api/v1/roles/** | user-org-service | 角色接口 |
| /api/v1/permissions/** | user-org-service | 权限接口 |
| /api/v1/orgs/** | user-org-service | 组织接口 |
| /api/v1/notifications/** | notification-service | 通知接口 |
| /api/v1/files/** | file-service | 文件接口 |
| /api/v1/workflows/** | workflow-service | 工作流接口 |
| /api/v1/thirdparty/** | third-party-service | 第三方接口 |
| /api/v1/system/** | system-service | 系统接口 |
| /api/v1/monitor/** | system-monitor | 监控接口 |

---

## 3. 过滤器链

### 3.1 全局过滤器

```
Request → TraceIdFilter → JwtAuthFilter → RateLimiterFilter → ProxyFilter → Response
```

#### 3.1.1 TraceIdFilter - 链路追踪

```java
// 为每个请求生成唯一traceId，便于日志追踪
// Header: X-Trace-Id
```

#### 3.1.2 JwtAuthFilter - JWT认证

```java
// 1. 验证Token有效性
// 2. 解析用户信息
// 3. 传递用户信息到下游服务
// Header: X-User-Id, X-Username
```

#### 3.1.3 RateLimiterFilter - 限流

```java
// 基于Redis的令牌桶限流
// 默认限流规则：1000请求/分钟
```

---

## 4. 安全配置

### 4.1 公开路径（无需认证）

```yaml
- /api/v1/auth/login
- /api/v1/auth/register
- /actuator/**
- /swagger-ui/**
- /v3/api-docs/**
```

### 4.2 认证流程

```
┌────────┐     ┌────────┐     ┌────────┐     ┌────────┐
│ Client │────>│Gateway │────>│Filter  │────>│Service │
└────────┘     └────────┘     └────────┘     └────────┘
                  │               │
                  │    1. Check   │
                  │       Path    │
                  │               │
                  │    2. Verify  │
                  │       JWT     │
                  │               │
                  │    3. Forward │
                  │       Request │
```

---

## 5. 全局异常处理

### 5.1 异常响应

```json
{
    "code": 401,
    "message": "Token已过期",
    "data": null,
    "timestamp": 1716192000000
}
```

### 5.2 异常码映射

| HTTP状态码 | 业务码 | 说明 |
|------------|--------|------|
| 400 | 400 | 请求参数错误 |
| 401 | 401 | 未授权 |
| 403 | 403 | 禁止访问 |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 内部错误 |
| 503 | 503 | 服务不可用（熔断） |

---

## 6. 使用示例

### 6.1 前端调用

```javascript
// 登录
const response = await fetch('http://localhost:8080/api/v1/auth/login', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        username: 'admin',
        password: 'admin123'
    })
});

// 后续请求
const userResponse = await fetch('http://localhost:8080/api/v1/users/1', {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});
```

---

## 7. 配置说明

### 7.1 application.yml

```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
  redis:
    host: localhost
    port: 6379

sentinel:
  eager:
    enabled: true

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
```

---

## 8. 注意事项

1. **路径匹配**：注意路由顺序和路径匹配规则
2. **Token传递**：确保Token正确传递到下游服务
3. **限流配置**：根据实际业务调整限流阈值
4. **超时配置**：合理设置路由超时时间
