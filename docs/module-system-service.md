# 系统参数服务 (system-service)

## 1. 模块概述

系统参数服务提供系统级配置参数的管理功能，支持参数分类、动态配置、配置变更通知等。

### 1.1 主要功能

- **参数管理**：系统参数CRUD
- **参数分类**：按模块分类管理
- **动态配置**：运行时修改配置
- **配置变更通知**：配置变更时通知相关服务

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 基础框架 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| Redis | 7.x | 缓存 |

---

## 2. 数据库设计

### 2.1 系统参数表 (system_parameter)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| param_key | VARCHAR(100) | 参数键 |
| param_value | TEXT | 参数值 |
| param_type | VARCHAR(50) | 参数类型 |
| category | VARCHAR(50) | 分类 |
| description | VARCHAR(255) | 描述 |
| status | TINYINT | 状态 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

---

## 3. API接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/system/parameters | 获取所有参数 |
| GET | /api/v1/system/parameters/{key} | 获取参数值 |
| PUT | /api/v1/system/parameters/{key} | 更新参数 |
| DELETE | /api/v1/system/parameters/{key} | 删除参数 |

---

## 4. 参数分类

| 分类 | 说明 | 示例 |
|------|------|------|
| SYSTEM | 系统配置 | system.name, system.version |
| SECURITY | 安全配置 | password.expire.days, login.max.attempts |
| BUSINESS | 业务配置 | order.timeout, notification.enabled |

---

## 5. 使用示例

### 5.1 获取参数

```java
@Autowired
private SystemParameterService parameterService;

public String getSystemName() {
    return parameterService.getValue("system.name", "默认系统");
}
```

### 5.2 更新参数

```java
public void updateSystemName(String name) {
    parameterService.update("system.name", name);
}
```
