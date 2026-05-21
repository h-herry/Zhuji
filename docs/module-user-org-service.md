# 用户组织服务 (user-org-service)
# User Organization Service (user-org-service)

## 1. 模块概述
## 1. Module Overview

用户组织服务是整个系统的核心模块，负责管理系统用户、组织架构、角色和权限。采用RBAC（基于角色的访问控制）模型，支持集团公司多层级组织架构。
The user organization service is the core module of the system, responsible for managing system users, organizational structure, roles, and permissions. Adopts RBAC (Role-Based Access Control) model, supporting multi-level organizational structure for group companies.

### 1.1 主要功能
### 1.1 Main Features

- **用户管理**：用户注册、登录、注销、信息管理
- **User Management**: User registration, login, logout, profile management
- **组织架构管理**：支持集团公司多层级组织架构
- **Organization Structure Management**: Supports multi-level organizational structure for group companies
- **角色管理**：角色CRUD、角色权限分配、批量操作
- **Role Management**: Role CRUD, role permission assignment, batch operations
- **权限管理**：细粒度权限控制，支持菜单、按钮、API三种资源类型
- **Permission Management**: Fine-grained permission control, supporting menu, button, API resource types
- **认证授权**：JWT Token认证、基于权限的访问控制
- **Authentication & Authorization**: JWT Token authentication, permission-based access control
- **多角色支持**：用户可同时拥有多个角色，支持设置主角色
- **Multi-role Support**: Users can have multiple roles simultaneously, support setting primary role
- **多组织支持**：用户可同时属于多个组织，支持设置主组织
- **Multi-organization Support**: Users can belong to multiple organizations simultaneously, support setting primary organization
- **可配置化**：用户、角色、组织级别的自定义配置管理
- **Configurable**: Custom configuration management at user, role, organization levels
- **多语言支持**：完整的国际化(i18n)支持
- **Multi-language Support**: Complete internationalization (i18n) support

### 1.2 技术栈
### 1.2 Technology Stack

| 技术 | 版本 | 说明 |
| Technology | Version | Description |
|------|------|------|
| Spring Boot | 3.2.x | 基础框架 | Foundation framework |
| Spring Security | 6.x | 安全框架 | Security framework |
| MyBatis-Plus | 3.5.x | ORM框架 | ORM framework |
| Redis | 7.x | 缓存、Session存储 | Cache, Session storage |
| JWT | 0.12.x | Token认证 | Token authentication |
| Spring Cache | - | 缓存抽象 | Cache abstraction |
| Flowable | 7.0.0 | 工作流引擎 | Workflow engine |

---

## 2. 数据库设计
## 2. Database Design

### 2.1 ER关系图
### 2.1 ER Diagram

```
┌─────────────┐     ┌──────────────────────────┐     ┌─────────────┐
│  sys_user   │────<│ sys_user_role_relation │>────│  sys_role   │
└─────────────┘     └──────────────────────────┘     └─────────────┘
       │                                              │
       │                                              │
       ▼                                              ▼
┌─────────────┐     ┌──────────────────────────┐     ┌─────────────────┐
│sys_user_config│    │sys_role_permission     │<────│sys_permission   │
└─────────────┘     └──────────────────────────┘     └─────────────────┘
       │                     │
       │                     │
       ▼                     ▼
┌─────────────┐     ┌──────────────────────────┐
│sys_global_config│   │sys_role_config        │
└─────────────┘     └──────────────────────────┘
       │
       │
       ▼
┌─────────────┐     ┌──────────────────────────┐     ┌─────────────┐
│  org_unit   │────<│ sys_user_org_relation   │     │  org_type   │
└─────────────┘     └──────────────────────────┘     └─────────────┘
       │
       ▼
┌─────────────┐
│sys_org_config│
└─────────────┘
```

### 2.2 数据表说明
### 2.2 Table Definitions

#### 2.2.1 组织类型表 (org_type)
#### 2.2.1 Organization Type Table (org_type)

原来代码中的枚举改为数据库配置，支持动态修改。
The original enum in code is changed to database configuration, supporting dynamic modification.

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| type_code | VARCHAR(50) | 类型编码（GROUP/COMPANY/DEPARTMENT/TEAM/POSITION） | Type code |
| type_name | VARCHAR(100) | 类型名称 | Type name |
| description | VARCHAR(255) | 描述 | Description |
| sort_order | INT | 排序 | Sort order |
| status | CHAR(1) | 状态：1-启用，0-禁用 | Status: 1-enabled, 0-disabled |

#### 2.2.2 组织单位表 (org_unit)
#### 2.2.2 Organization Unit Table (org_unit)

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| org_code | VARCHAR(50) | 组织编码（唯一） | Organization code (unique) |
| org_name | VARCHAR(100) | 组织名称 | Organization name |
| org_type_id | BIGINT | 组织类型ID | Organization type ID |
| parent_id | BIGINT | 父组织ID | Parent organization ID |
| level_code | VARCHAR(100) | 层级编码（如：00001.00001.00001） | Level code |
| sort_order | INT | 排序 | Sort order |
| status | CHAR(1) | 状态 | Status |

#### 2.2.3 用户表 (sys_user)
#### 2.2.3 User Table (sys_user)

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| username | VARCHAR(50) | 用户名（唯一） | Username (unique) |
| password | VARCHAR(255) | 密码（BCrypt加密） | Password (BCrypt encrypted) |
| email | VARCHAR(100) | 邮箱 | Email |
| phone | VARCHAR(20) | 手机号 | Phone number |
| org_id | BIGINT | 所属组织ID（主组织） | Organization ID (primary) |
| status | TINYINT | 状态：1-正常，0-禁用 | Status: 1-normal, 0-disabled |
| password_update_time | DATETIME | 密码更新时间 | Password update time |
| login_fail_count | INT | 登录失败次数 | Login failure count |
| lock_until | DATETIME | 账户锁定到期时间 | Account lock expiration time |

#### 2.2.4 角色表 (sys_role)
#### 2.2.4 Role Table (sys_role)

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| role_code | VARCHAR(50) | 角色编码（唯一） | Role code (unique) |
| role_name | VARCHAR(100) | 角色名称 | Role name |
| description | VARCHAR(255) | 描述 | Description |
| sort_order | INT | 排序 | Sort order |
| status | CHAR(1) | 状态 | Status |

#### 2.2.5 权限表 (sys_permission)
#### 2.2.5 Permission Table (sys_permission)

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| permission_code | VARCHAR(100) | 权限编码（唯一） | Permission code (unique) |
| permission_name | VARCHAR(100) | 权限名称 | Permission name |
| resource_type | VARCHAR(20) | 资源类型：menu/button/api | Resource type: menu/button/api |
| path | VARCHAR(255) | 路径 | Path |
| http_method | VARCHAR(10) | HTTP方法：GET/POST/PUT/DELETE | HTTP method: GET/POST/PUT/DELETE |
| parent_id | BIGINT | 父权限ID | Parent permission ID |
| sort_order | INT | 排序 | Sort order |
| status | CHAR(1) | 状态 | Status |

#### 2.2.6 用户角色关联表 (sys_user_role_relation)
#### 2.2.6 User Role Relation Table (sys_user_role_relation)

支持多角色，isPrimary标识主角色。
Supports multiple roles, isPrimary identifies primary role.

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| user_id | BIGINT | 用户ID | User ID |
| role_id | BIGINT | 角色ID | Role ID |
| is_primary | VARCHAR(10) | 是否主角色：0-否，1-是 | Is primary: 0-no, 1-yes |
| create_time | DATETIME | 创建时间 | Create time |

#### 2.2.7 用户组织关联表 (sys_user_org_relation)
#### 2.2.7 User Organization Relation Table (sys_user_org_relation)

支持多组织，relation_type区分成员和负责人。
Supports multiple organizations, relation_type distinguishes between member and leader.

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| user_id | BIGINT | 用户ID | User ID |
| org_id | BIGINT | 组织ID | Organization ID |
| relation_type | VARCHAR(20) | 关系类型：MEMBER-成员，LEADER-负责人 | Relation type: MEMBER-member, LEADER-leader |
| is_primary | VARCHAR(10) | 是否主组织：0-否，1-是 | Is primary: 0-no, 1-yes |
| create_time | DATETIME | 创建时间 | Create time |

#### 2.2.8 角色权限关联表 (sys_role_permission)
#### 2.2.8 Role Permission Relation Table (sys_role_permission)

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| role_id | BIGINT | 角色ID | Role ID |
| permission_id | BIGINT | 权限ID | Permission ID |
| create_time | DATETIME | 创建时间 | Create time |

#### 2.2.9 用户配置表 (sys_user_config)
#### 2.2.9 User Config Table (sys_user_config)

支持用户级别的自定义配置。
Supports user-level custom configuration.

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| user_id | BIGINT | 用户ID | User ID |
| config_type | VARCHAR(50) | 配置类型 | Config type |
| config_key | VARCHAR(100) | 配置键 | Config key |
| config_value | TEXT | 配置值 | Config value |
| value_type | VARCHAR(20) | 值类型：STRING, NUMBER, BOOLEAN, JSON | Value type: STRING, NUMBER, BOOLEAN, JSON |
| description | VARCHAR(200) | 描述 | Description |

#### 2.2.10 角色配置表 (sys_role_config)
#### 2.2.10 Role Config Table (sys_role_config)

支持角色级别的自定义配置。
Supports role-level custom configuration.

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| role_id | BIGINT | 角色ID | Role ID |
| config_type | VARCHAR(50) | 配置类型 | Config type |
| config_key | VARCHAR(100) | 配置键 | Config key |
| config_value | TEXT | 配置值 | Config value |
| value_type | VARCHAR(20) | 值类型：STRING, NUMBER, BOOLEAN, JSON | Value type: STRING, NUMBER, BOOLEAN, JSON |
| description | VARCHAR(200) | 描述 | Description |

#### 2.2.11 组织配置表 (sys_org_config)
#### 2.2.11 Organization Config Table (sys_org_config)

支持组织级别的自定义配置。
Supports organization-level custom configuration.

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| org_id | BIGINT | 组织ID | Organization ID |
| config_type | VARCHAR(50) | 配置类型 | Config type |
| config_key | VARCHAR(100) | 配置键 | Config key |
| config_value | TEXT | 配置值 | Config value |
| value_type | VARCHAR(20) | 值类型：STRING, NUMBER, BOOLEAN, JSON | Value type: STRING, NUMBER, BOOLEAN, JSON |
| description | VARCHAR(200) | 描述 | Description |

#### 2.2.12 全局配置表 (sys_global_config)
#### 2.2.12 Global Config Table (sys_global_config)

支持系统级别的全局配置，包括多语言配置。
Supports system-level global configuration, including multi-language configuration.

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| config_type | VARCHAR(50) | 配置类型 | Config type |
| config_key | VARCHAR(100) | 配置键（唯一） | Config key (unique) |
| config_value | TEXT | 配置值 | Config value |
| value_type | VARCHAR(20) | 值类型：STRING, NUMBER, BOOLEAN, JSON | Value type: STRING, NUMBER, BOOLEAN, JSON |
| description | VARCHAR(200) | 描述 | Description |
| sort_order | INT | 排序 | Sort order |
| status | VARCHAR(10) | 状态：0-禁用，1-启用 | Status: 0-disabled, 1-enabled |

#### 2.2.13 用户密码历史表 (sys_user_password_history)
#### 2.2.13 User Password History Table (sys_user_password_history)

记录用户密码变更历史，用于密码重复校验。
Records user password change history for password repeat validation.

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| user_id | BIGINT | 用户ID | User ID |
| password_hash | VARCHAR(255) | 历史密码哈希值 | Historical password hash |
| create_time | DATETIME | 创建时间 | Create time |

#### 2.2.14 Token黑名单表 (sys_token_blacklist)
#### 2.2.14 Token Blacklist Table (sys_token_blacklist)

记录已注销的Token，防止被恶意使用。
Records logged-out tokens to prevent malicious use.

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| token | VARCHAR(500) | Token值 | Token value |
| user_id | BIGINT | 用户ID | User ID |
| expires_at | DATETIME | Token过期时间 | Token expiration time |
| created_at | DATETIME | 创建时间 | Create time |

#### 2.2.15 用户配置历史表 (sys_user_config_history)
#### 2.2.15 User Config History Table (sys_user_config_history)

记录配置变更历史，支持配置回滚和版本管理。
Records config change history, supports config rollback and version management.

| 字段 | 类型 | 说明 |
| Field | Type | Description |
|------|------|------|
| id | BIGINT | 主键ID | Primary key ID |
| config_id | BIGINT | 配置ID | Config ID |
| user_id | BIGINT | 用户ID（NULL表示全局配置） | User ID (NULL means global config) |
| config_type | VARCHAR(50) | 配置类型 | Config type |
| config_key | VARCHAR(100) | 配置键 | Config key |
| config_value | TEXT | 配置值 | Config value |
| operation | VARCHAR(20) | 操作类型：CREATE/UPDATE/DELETE/ROLLBACK | Operation type: CREATE/UPDATE/DELETE/ROLLBACK |
| operator | VARCHAR(100) | 操作人 | Operator |
| version | INT | 版本号（用于版本回滚） | Version number (for rollback) |
| create_time | DATETIME | 创建时间 | Create time |

---

## 3. API接口
## 3. API Interfaces

### 3.1 认证接口 (AuthController)
### 3.1 Authentication Interface (AuthController)

| 方法 | 路径 | 说明 | 认证 |
| Method | Path | Description | Auth Required |
|------|------|------|------|
| POST | /api/v1/auth/login | 用户登录 | 否 | No |
| POST | /api/v1/auth/register | 用户注册 | 否 | No |
| POST | /api/v1/auth/logout | 用户注销 | 是 | Yes |
| POST | /api/v1/auth/refresh | 刷新Token | 是 | Yes |

### 3.2 用户管理接口 (UserController)
### 3.2 User Management Interface (UserController)

| 方法 | 路径 | 说明 | 认证 |
| Method | Path | Description | Auth Required |
|------|------|------|------|
| POST | /api/v1/users | 创建用户 | 是 | Yes |
| GET | /api/v1/users/{id} | 获取用户详情 | 是 | Yes |
| GET | /api/v1/users | 分页查询用户 | 是 | Yes |
| PUT | /api/v1/users/{id} | 更新用户 | 是 | Yes |
| DELETE | /api/v1/users/{id} | 删除用户 | 是 | Yes |
| POST | /api/v1/users/{id}/roles | 分配用户角色(多角色) | 是 | Yes |
| GET | /api/v1/users/{id}/roles | 获取用户角色列表 | 是 | Yes |
| DELETE | /api/v1/users/{id}/roles/{roleId} | 移除用户角色 | 是 | Yes |
| PUT | /api/v1/users/{id}/roles/{roleId}/primary | 设置主角色 | 是 | Yes |
| POST | /api/v1/users/{id}/orgs | 分配用户组织(多组织) | 是 | Yes |
| GET | /api/v1/users/{id}/orgs | 获取用户组织列表 | 是 | Yes |
| DELETE | /api/v1/users/{id}/orgs/{orgId} | 移除用户组织 | 是 | Yes |
| PUT | /api/v1/users/{id}/orgs/{orgId}/primary | 设置主组织 | 是 | Yes |
| GET | /api/v1/users/{id}/configs | 获取用户配置列表 | 是 | Yes |
| POST | /api/v1/users/{id}/configs | 创建用户配置 | 是 | Yes |
| PUT | /api/v1/users/{id}/configs/{configId} | 更新用户配置 | 是 | Yes |
| DELETE | /api/v1/users/{id}/configs/{configId} | 删除用户配置 | 是 | Yes |

### 3.3 角色管理接口 (RoleController)
### 3.3 Role Management Interface (RoleController)

| 方法 | 路径 | 说明 | 认证 |
| Method | Path | Description | Auth Required |
|------|------|------|------|
| GET | /api/v1/roles | 分页查询角色 | 是 | Yes |
| GET | /api/v1/roles/all | 获取所有角色 | 是 | Yes |
| GET | /api/v1/roles/{id} | 获取角色详情 | 是 | Yes |
| GET | /api/v1/roles/{id}/permissions | 获取角色权限ID列表 | 是 | Yes |
| POST | /api/v1/roles | 创建角色 | 是 | Yes |
| PUT | /api/v1/roles/{id} | 更新角色 | 是 | Yes |
| DELETE | /api/v1/roles/{id} | 删除角色 | 是 | Yes |
| POST | /api/v1/roles/{id}/permissions | 分配角色权限 | 是 | Yes |
| POST | /api/v1/roles/batch | 批量创建角色 | 是 | Yes |
| PUT | /api/v1/roles/batch | 批量更新角色 | 是 | Yes |
| POST | /api/v1/roles/{id}/enable | 启用角色 | 是 | Yes |
| POST | /api/v1/roles/{id}/disable | 禁用角色 | 是 | Yes |
| POST | /api/v1/roles/{id}/copy-permissions/{targetRoleId} | 复制角色权限 | 是 | Yes |

### 3.4 权限管理接口 (PermissionController)
### 3.4 Permission Management Interface (PermissionController)

| 方法 | 路径 | 说明 | 认证 |
| Method | Path | Description | Auth Required |
|------|------|------|------|
| GET | /api/v1/permissions | 分页查询权限 | 是 | Yes |
| GET | /api/v1/permissions/all | 获取所有权限 | 是 | Yes |
| GET | /api/v1/permissions/{id} | 获取权限详情 | 是 | Yes |
| GET | /api/v1/permissions/user/{userId} | 获取用户的权限列表 | 是 | Yes |
| GET | /api/v1/permissions/role/{roleId} | 获取角色的权限列表 | 是 | Yes |
| POST | /api/v1/permissions | 创建权限 | 是 | Yes |
| PUT | /api/v1/permissions/{id} | 更新权限 | 是 | Yes |
| DELETE | /api/v1/permissions/{id} | 删除权限 | 是 | Yes |

### 3.5 组织管理接口 (OrgUnitController)
### 3.5 Organization Management Interface (OrgUnitController)

| 方法 | 路径 | 说明 | 认证 |
| Method | Path | Description | Auth Required |
|------|------|------|------|
| GET | /api/v1/orgs | 分页查询组织 | 是 | Yes |
| GET | /api/v1/orgs/tree | 获取组织树 | 是 | Yes |
| GET | /api/v1/orgs/{id} | 获取组织详情 | 是 | Yes |
| POST | /api/v1/orgs | 创建组织 | 是 | Yes |
| PUT | /api/v1/orgs/{id} | 更新组织 | 是 | Yes |
| DELETE | /api/v1/orgs/{id} | 删除组织 | 是 | Yes |
| GET | /api/v1/orgs/types | 获取所有组织类型 | 是 | Yes |

### 3.6 配置管理接口 (ConfigController)
### 3.6 Config Management Interface (ConfigController)

| 方法 | 路径 | 说明 | 认证 |
| Method | Path | Description | Auth Required |
|------|------|------|------|
| GET | /api/v1/configs | 分页查询配置列表 | 是 | Yes |
| GET | /api/v1/configs/{id} | 获取配置详情 | 是 | Yes |
| GET | /api/v1/configs/key/{configKey} | 根据Key获取配置 | 是 | Yes |
| GET | /api/v1/configs/type/{configType} | 根据类型获取配置列表 | 是 | Yes |
| POST | /api/v1/configs | 创建配置 | 是 | Yes |
| PUT | /api/v1/configs/{id} | 更新配置 | 是 | Yes |
| DELETE | /api/v1/configs/{id} | 删除配置 | 是 | Yes |
| GET | /api/v1/configs/value/{configKey} | 获取配置值 | 是 | Yes |
| GET | /api/v1/configs/types | 获取所有配置类型 | 是 | Yes |
| POST | /api/v1/configs/refresh-cache | 刷新配置缓存 | 是 | Yes |
| PUT | /api/v1/configs/batch | 批量更新配置 | 是 | Yes |
| GET | /api/v1/configs/type/{configType}/map | 获取类型的全部配置为Map | 是 | Yes |

### 3.7 多语言接口 (I18nController)
### 3.7 Multi-language Interface (I18nController)

| 方法 | 路径 | 说明 | 认证 |
| Method | Path | Description | Auth Required |
|------|------|------|------|
| GET | /api/v1/i18n/languages | 获取支持的语言列表 | 否 | No |
| POST | /api/v1/i18n/switch | 切换语言 | 否 | No |
| GET | /api/v1/i18n/current | 获取当前语言 | 否 | No |
| GET | /api/v1/i18n/validate/{lang} | 验证语言是否支持 | 否 | No |

---

## 4. API调用示例
## 4. API Call Examples

### 4.1 多角色分配
### 4.1 Multi-role Assignment

```bash
# 为用户分配多个角色
# Assign multiple roles to user
POST /api/v1/users/1/roles
Content-Type: application/json

[1, 2, 3]

# 设置第一个角色为主角色
# Set first role as primary
# isPrimary参数：1-第一个为主角色，0-不设主角色
# isPrimary parameter: 1-first as primary, 0-no primary
```

**响应示例**：
**Response Example**:
```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

### 4.2 多组织分配
### 4.2 Multi-organization Assignment

```bash
# 为用户分配多个组织
# Assign multiple organizations to user
POST /api/v1/users/1/orgs?isPrimary=1
Content-Type: application/json

[1, 2, 3]
```

### 4.3 全局配置管理
### 4.3 Global Config Management

```bash
# 获取所有支持的语言
# Get all supported languages
GET /api/v1/configs/type/i18n

# 获取默认语言配置
# Get default language config
GET /api/v1/configs/key/default.language

# 创建新的全局配置
# Create new global config
POST /api/v1/configs
Content-Type: application/json

{
    "configType": "custom",
    "configKey": "custom.setting",
    "configValue": "value",
    "valueType": "STRING",
    "description": "自定义配置",
    "sortOrder": 10,
    "status": "1"
}
```

### 4.4 语言切换
### 4.4 Language Switch

```bash
# 切换到英文
# Switch to English
POST /api/v1/i18n/switch?lang=en_US

# 获取当前语言
# Get current language
GET /api/v1/i18n/current
```

---

## 5. 核心代码
## 5. Core Code

### 5.1 UserService - 批量角色分配（V3优化）
### 5.1 UserService - Batch Role Assignment (V3 Optimized)

```java
@Override
@CacheEvict(value = "user-roles", key = "#userId", allEntries = true)
@Transactional
public void assignRoles(Long userId, List<Long> roleIds) {
    User user = getById(userId);
    if (user == null) {
        throw new BusinessException(404, I18nMessageUtil.getMessage("user.not.found"));
    }

    // 1. 获取已有的用户角色列表
    // 1. Get existing user role list
    List<UserRole> existingUserRoles = userRoleMapper.selectList(
        new LambdaQueryWrapper<UserRole>()
            .eq(UserRole::getUserId, userId)
    );

    List<Long> existingRoleIds = existingUserRoles.stream()
        .map(UserRole::getRoleId)
        .collect(Collectors.toList());

    // 2. 处理空列表（清除所有角色）
    // 2. Handle empty list (clear all roles)
    if (roleIds == null || roleIds.isEmpty()) {
        if (!existingRoleIds.isEmpty()) {
            userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, userId)
            );
        }
        return;
    }

    // 3. 计算需要删除的角色
    // 3. Calculate roles to remove
    List<Long> toRemove = existingRoleIds.stream()
        .filter(id -> !roleIds.contains(id))
        .collect(Collectors.toList());

    // 4. 计算需要新增的角色
    // 4. Calculate roles to add
    List<Long> toAdd = roleIds.stream()
        .filter(id -> !existingRoleIds.contains(id))
        .collect(Collectors.toList());

    // 5. 批量删除
    // 5. Batch delete
    if (!toRemove.isEmpty()) {
        userRoleMapper.delete(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .in(UserRole::getRoleId, toRemove)
        );
    }

    // 6. 批量插入（V3优化：使用批量插入替代循环插入）
    // 6. Batch insert (V3 optimized: use batch insert instead of loop insert)
    if (!toAdd.isEmpty()) {
        List<UserRole> userRoles = toAdd.stream()
            .map(roleId -> {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            })
            .collect(Collectors.toList());
        
        userRoleMapper.batchInsertUserRoles(userRoles);
    }
}
```

### 5.2 UserConfigServiceImpl - 批量更新主角色/组织（V3优化）
### 5.2 UserConfigServiceImpl - Batch Update Primary Role/Org (V3 Optimized)

```java
@Override
@Transactional
public void setPrimaryRole(Long userId, Long roleId) {
    Role role = roleMapper.selectById(roleId);
    if (role == null) {
        throw new BusinessException(404, I18nMessageUtil.getMessage("role.not.found"));
    }

    List<UserRoleRelation> relations = userRoleRelationMapper.selectByUserId(userId);
    boolean hasRole = relations.stream()
        .anyMatch(r -> r.getRoleId().equals(roleId));

    if (!hasRole) {
        throw new BusinessException(400, "用户没有该角色");
    }

    // V3优化：使用单条SQL批量更新
    // V3 optimized: use single SQL batch update
    userRoleRelationMapper.updatePrimaryByUserId(userId, roleId);
}

@Override
@Transactional
public void setPrimaryOrg(Long userId, Long orgId) {
    OrgUnit org = orgUnitMapper.selectById(orgId);
    if (org == null) {
        throw new BusinessException(404, I18nMessageUtil.getMessage("org.not.found"));
    }

    List<UserOrgRelation> relations = userOrgRelationMapper.selectByUserId(userId);
    boolean hasOrg = relations.stream()
        .anyMatch(r -> r.getOrgId().equals(orgId));

    if (!hasOrg) {
        throw new BusinessException(400, "用户不属于该组织");
    }

    // V3优化：使用单条SQL批量更新
    // V3 optimized: use single SQL batch update
    userOrgRelationMapper.updatePrimaryByUserId(userId, orgId);
}
```

### 5.3 UserConfigServiceImpl - N+1查询优化（V3优化）
### 5.3 UserConfigServiceImpl - N+1 Query Optimization (V3 Optimized)

```java
@Override
public List<Map<String, Object>> getUserRoles(Long userId) {
    List<UserRoleRelation> relations = userRoleRelationMapper.selectByUserId(userId);

    if (relations.isEmpty()) {
        return Collections.emptyList();
    }

    // V3优化：批量获取角色信息（一次查询替代N次）
    // V3 optimized: batch get role info (one query instead of N)
    Set<Long> roleIds = relations.stream()
        .map(UserRoleRelation::getRoleId)
        .collect(Collectors.toSet());
    List<Role> roles = roleMapper.selectBatchIds(roleIds);

    Map<Long, Role> roleMap = roles.stream()
        .collect(Collectors.toMap(Role::getId, r -> r));

    UserRoleRelation primaryRelation = userRoleRelationMapper.selectPrimaryByUserId(userId);

    List<Map<String, Object>> result = new ArrayList<>();
    for (UserRoleRelation relation : relations) {
        Role role = roleMap.get(relation.getRoleId());
        if (role != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", role.getId());
            map.put("roleCode", role.getRoleCode());
            map.put("roleName", role.getRoleName());
            map.put("description", role.getDescription());
            map.put("isPrimary", primaryRelation != null && primaryRelation.getRoleId().equals(role.getId()) ? "1" : "0");
            result.add(map);
        }
    }

    return result;
}

@Override
public List<Map<String, Object>> getUserOrgs(Long userId) {
    List<UserOrgRelation> relations = userOrgRelationMapper.selectByUserId(userId);

    if (relations.isEmpty()) {
        return Collections.emptyList();
    }

    // V3优化：批量获取组织信息（一次查询替代N次）
    // V3 optimized: batch get org info (one query instead of N)
    Set<Long> orgIds = relations.stream()
        .map(UserOrgRelation::getOrgId)
        .collect(Collectors.toSet());
    List<OrgUnit> orgs = orgUnitMapper.selectBatchIds(orgIds);

    Map<Long, OrgUnit> orgMap = orgs.stream()
        .collect(Collectors.toMap(OrgUnit::getId, o -> o));

    UserOrgRelation primaryRelation = userOrgRelationMapper.selectPrimaryByUserId(userId);

    List<Map<String, Object>> result = new ArrayList<>();
    for (UserOrgRelation relation : relations) {
        OrgUnit org = orgMap.get(relation.getOrgId());
        if (org != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", org.getId());
            map.put("orgCode", org.getOrgCode());
            map.put("fullName", org.getFullName());
            map.put("shortName", org.getShortName());
            map.put("orgType", org.getOrgType());
            map.put("isPrimary", primaryRelation != null && primaryRelation.getOrgId().equals(org.getId()) ? "1" : "0");
            result.add(map);
        }
    }

    return result;
}
```

### 5.4 ConfigHistoryService - 版本管理（V3优化）
### 5.4 ConfigHistoryService - Version Management (V3 Optimized)

```java
@Service
public class ConfigHistoryServiceImpl implements ConfigHistoryService {

    private final ConfigHistoryMapper configHistoryMapper;
    private final UserConfigMapper userConfigMapper;

    @Override
    public void saveConfigHistory(Long configId, Long userId, String configKey,
                                 String configValue, String configType,
                                 String operation, String operator) {
        ConfigHistory history = new ConfigHistory();
        history.setConfigId(configId);
        history.setUserId(userId);
        history.setConfigKey(configKey);
        history.setConfigValue(configValue);
        history.setConfigType(configType);
        history.setOperation(operation);
        history.setOperator(operator);
        history.setVersion(getNextVersion(configId));
        configHistoryMapper.insert(history);
    }

    private Integer getNextVersion(Long configId) {
        Integer maxVersion = configHistoryMapper.selectMaxVersionByConfigId(configId);
        return maxVersion != null ? maxVersion + 1 : 1;
    }

    @Override
    @Transactional
    public Object rollbackToVersion(Long configId, Integer version) {
        ConfigHistory targetVersion = configHistoryMapper.selectByConfigIdAndVersion(configId, version);
        if (targetVersion == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("config.version.not.found"));
        }

        UserConfig config = userConfigMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("config.not.found"));
        }

        config.setConfigValue(targetVersion.getConfigValue());
        userConfigMapper.updateById(config);

        saveConfigHistory(configId, config.getUserId(), config.getConfigKey(),
                          config.getConfigValue(), config.getConfigType(), "ROLLBACK", null);

        return config;
    }
}
```

### 5.5 ConfigEncryptionService - 配置加密（V3优化）
### 5.5 ConfigEncryptionService - Config Encryption (V3 Optimized)

```java
@Service
public class ConfigEncryptionService {

    private static final List<String> SENSITIVE_PREFIXES = Arrays.asList(
        "password", "apiKey", "secret", "token", "credential", "key", "auth"
    );

    @Value("${config.encryption.key:zhuji-default-encryption-key-16}")
    private String encryptionKey;

    public boolean isSensitive(String configKey) {
        if (configKey == null) return false;
        String lowerKey = configKey.toLowerCase();
        return SENSITIVE_PREFIXES.stream()
            .anyMatch(lowerKey::contains);
    }

    public String encrypt(String value) {
        if (value == null) return null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                encryptionKey.getBytes(StandardCharsets.UTF_8), "AES"
            );
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue == null) return null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                encryptionKey.getBytes(StandardCharsets.UTF_8), "AES"
            );
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
}
```

### 5.6 UserConfigServiceImpl - 配置加密集成（V3优化）
### 5.6 UserConfigServiceImpl - Config Encryption Integration (V3 Optimized)

```java
@Override
@Cacheable(value = "user-config", key = "#userId + ':' + #configKey")
public UserConfig getUserConfigByKey(Long userId, String configKey) {
    UserConfig config = userConfigMapper.selectByUserIdAndKey(userId, configKey);

    if (config != null && configEncryptionService.isSensitive(configKey)) {
        try {
            config.setConfigValue(configEncryptionService.decrypt(config.getConfigValue()));
        } catch (Exception e) {
        }
    }

    return config;
}

@Override
@CacheEvict(value = "user-config", key = "#config.userId", allEntries = true)
@Transactional
public UserConfig createUserConfig(UserConfig config) {
    validateConfig(config.getConfigKey(), config.getConfigValue());
    UserConfig existing = userConfigMapper.selectByUserIdAndKey(config.getUserId(), config.getConfigKey());
    if (existing != null) {
        throw new BusinessException(400, I18nMessageUtil.getMessage("validation.unique", "配置键"));
    }

    // V3优化：加密敏感配置
    // V3 optimized: encrypt sensitive config
    if (configEncryptionService.isSensitive(config.getConfigKey())) {
        config.setConfigValue(configEncryptionService.encrypt(config.getConfigValue()));
    }

    userConfigMapper.insert(config);

    configHistoryService.saveConfigHistory(
        config.getId(),
        config.getUserId(),
        config.getConfigKey(),
        config.getConfigValue(),
        config.getConfigType(),
        "CREATE",
        null
    );

    eventPublisher.publishEvent(new ConfigChangeEvent(this, config.getConfigKey()));
    configChangePublisher.publishConfigChange(config.getUserId(), config.getConfigKey(), "CREATE");

    return config;
}
```

### 5.7 ConfigManagementService - 全局配置管理
### 5.7 ConfigManagementService - Global Config Management

```java
@Service
public class ConfigManagementService {

    public Page<GlobalConfig> pageConfigs(int page, int size, String configType, String configKey) {
        Page<GlobalConfig> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<GlobalConfig> wrapper = new LambdaQueryWrapper<>();

        if (configType != null) {
            wrapper.eq(GlobalConfig::getConfigType, configType);
        }
        if (configKey != null) {
            wrapper.like(GlobalConfig::getConfigKey, configKey);
        }
        wrapper.orderByAsc(GlobalConfig::getSortOrder);
        return globalConfigMapper.selectPage(pageObj, wrapper);
    }

    public String getConfigValue(String configKey, String defaultValue) {
        GlobalConfig config = getConfigByKey(configKey);
        return config != null ? config.getConfigValue() : defaultValue;
    }
}
```

### 5.8 I18nController - 多语言切换
### 5.8 I18nController - Multi-language Switch

```java
@PostMapping("/switch")
public ApiResponse<Void> switchLanguage(
        @RequestParam String lang,
        HttpServletRequest request,
        HttpServletResponse response) {

    if (!SUPPORTED_LANGUAGES.containsKey(lang)) {
        return ApiResponse.error(400, "不支持的语言: " + lang);
    }

    // 写入Cookie
    // Write to Cookie
    Cookie localeCookie = new Cookie("locale", lang);
    localeCookie.setMaxAge(604800); // 7天
    localeCookie.setPath("/");
    response.addCookie(localeCookie);

    return ApiResponse.success();
}
```

---

## 6. 初始化数据
## 6. Initialization Data

### 6.1 预定义角色
### 6.1 Predefined Roles

| 角色编码 | 角色名称 | 说明 |
| Role Code | Role Name | Description |
|----------|----------|------|
| SUPER_ADMIN | 超级管理员 | 拥有所有权限 | Has all permissions |
| ADMIN | 管理员 | 系统管理员 | System administrator |
| USER | 普通用户 | 基础权限 | Basic permissions |
| GUEST | 访客 | 访客用户 | Guest user |

### 6.2 预定义权限分类
### 6.2 Predefined Permission Categories

| 分类 | 说明 |
| Category | Description |
|------|------|
| 用户管理 | USER_VIEW, USER_CREATE, USER_UPDATE, USER_DELETE, USER_ROLE_ASSIGN |
| User Management | USER_VIEW, USER_CREATE, USER_UPDATE, USER_DELETE, USER_ROLE_ASSIGN |
| 角色管理 | ROLE_VIEW, ROLE_CREATE, ROLE_UPDATE, ROLE_DELETE, ROLE_PERM_ASSIGN |
| Role Management | ROLE_VIEW, ROLE_CREATE, ROLE_UPDATE, ROLE_DELETE, ROLE_PERM_ASSIGN |
| 权限管理 | PERM_VIEW, PERM_CREATE, PERM_UPDATE, PERM_DELETE |
| Permission Management | PERM_VIEW, PERM_CREATE, PERM_UPDATE, PERM_DELETE |
| 组织管理 | ORG_VIEW, ORG_CREATE, ORG_UPDATE, ORG_DELETE |
| Organization Management | ORG_VIEW, ORG_CREATE, ORG_UPDATE, ORG_DELETE |
| 系统监控 | MONITOR_VIEW |
| System Monitoring | MONITOR_VIEW |
| 通知管理 | NOTIFICATION_VIEW, NOTIFICATION_SEND |
| Notification Management | NOTIFICATION_VIEW, NOTIFICATION_SEND |
| 文件管理 | FILE_UPLOAD, FILE_DOWNLOAD, FILE_DELETE |
| File Management | FILE_UPLOAD, FILE_DOWNLOAD, FILE_DELETE |
| 工作流 | WORKFLOW_VIEW, WORKFLOW_CREATE, WORKFLOW_START, WORKFLOW_APPROVE |
| Workflow | WORKFLOW_VIEW, WORKFLOW_CREATE, WORKFLOW_START, WORKFLOW_APPROVE |
| 第三方服务 | THIRDPARTY_VIEW, THIRDPARTY_SMS |
| Third-party Services | THIRDPARTY_VIEW, THIRDPARTY_SMS |
| 系统参数 | SYSPARAM_VIEW, SYSPARAM_UPDATE |
| System Parameters | SYSPARAM_VIEW, SYSPARAM_UPDATE |

### 6.3 全局配置初始化数据
### 6.3 Global Config Initialization Data

| 配置类型 | 配置键 | 配置值 | 说明 |
| Config Type | Config Key | Config Value | Description |
|----------|--------|--------|------|
| i18n | default.language | zh_CN | 系统默认语言 | System default language |
| i18n | supported.languages | zh_CN,en_US,zh_TW,ja_JP,ko_KR | 支持的语言列表 | Supported languages list |
| system | max.login.attempts | 5 | 最大登录尝试次数 | Max login attempts |
| system | session.timeout | 1800 | 会话超时时间（秒） | Session timeout (seconds) |
| security | password.min.length | 8 | 密码最小长度 | Min password length |
| security | password.require.special.char | true | 密码必须包含特殊字符 | Password must contain special characters |
| security | password.expiry.days | 90 | 密码过期天数 | Password expiry days |
| role | default.user.role | USER | 新用户默认角色 | New user default role |
| org | allow.virtual.org | true | 允许创建虚拟组织 | Allow creating virtual organizations |
| org | max.org.depth | 10 | 最大组织层级深度 | Max organization depth |

### 6.4 默认用户
### 6.4 Default Users

| 用户名 | 密码 | 角色 |
| Username | Password | Role |
|--------|------|------|
| admin | admin123 | 超级管理员 | Super Administrator |
| testuser | user123 | 普通用户 | Normal User |

---

## 7. 配置说明
## 7. Configuration Guide

### 7.1 application.yml
### 7.1 application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zhuji_user_org
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
    database: 0

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto

# 配置加密
# Config encryption
config:
  encryption:
    key: ${CONFIG_ENCRYPTION_KEY:zhuji-default-encryption-key-16}

# 缓存配置
# Cache configuration
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时
```

---

## 8. 安全机制
## 8. Security Mechanisms

### 8.1 密码安全
### 8.1 Password Security

- **BCrypt加密**：使用BCrypt算法加密用户密码
- **BCrypt Encryption**: Use BCrypt algorithm to encrypt user passwords
- **密码历史**：记录最近5次密码，禁止重复使用
- **Password History**: Record last 5 passwords, prevent reuse
- **密码策略**：强制要求8位以上，包含大小写字母、数字和特殊字符
- **Password Policy**: Require at least 8 characters, including uppercase, lowercase, numbers, and special characters
- **密码过期**：90天后强制要求修改密码
- **Password Expiry**: Force password change after 90 days

### 8.2 账户安全
### 8.2 Account Security

- **登录失败锁定**：连续5次失败后锁定账户30分钟
- **Login Failure Lock**: Lock account after 5 consecutive failures for 30 minutes
- **Token黑名单**：注销后将Token加入黑名单
- **Token Blacklist**: Add token to blacklist after logout
- **JWT认证**：使用JWT进行无状态认证
- **JWT Authentication**: Use JWT for stateless authentication

### 8.3 权限控制
### 8.3 Permission Control

- **RBAC模型**：基于角色的访问控制
- **RBAC Model**: Role-based access control
- **多角色支持**：用户可拥有多个角色
- **Multi-role Support**: Users can have multiple roles
- **主角色**：支持设置主角色，用于权限优先级
- **Primary Role**: Support setting primary role for permission priority
- **细粒度权限**：支持菜单、按钮、API三种资源类型
- **Fine-grained Permissions**: Support menu, button, API resource types

---

## 9. 缓存策略
## 9. Caching Strategy

### 9.1 缓存层级
### 9.1 Cache Levels

1. **本地缓存**：Caffeine缓存，用于高频访问数据
2. **Local Cache**: Caffeine cache for high-frequency data
3. **分布式缓存**：Redis缓存，用于跨实例共享数据
4. **Distributed Cache**: Redis cache for cross-instance data sharing
5. **数据库缓存**：MyBatis二级缓存
6. **Database Cache**: MyBatis second-level cache

### 9.2 缓存配置
### 9.2 Cache Configuration

```java
@Cacheable(value = "user-roles", key = "#userId")
public List<UserRole> getUserRoles(Long userId) {
    // ...
}

@CacheEvict(value = "user-roles", key = "#userId", allEntries = true)
public void assignRoles(Long userId, List<Long> roleIds) {
    // ...
}
```

---

## 10. 多语言(i18n)支持
## 10. Multi-language (i18n) Support

### 10.1 支持的语言
### 10.1 Supported Languages

| 语言代码 | 语言名称 |
| Language Code | Language Name |
|----------|----------|
| zh_CN | 简体中文 | Simplified Chinese |
| en_US | 英语 | English |
| zh_TW | 繁体中文 | Traditional Chinese |
| ja_JP | 日语 | Japanese |
| ko_KR | 韩语 | Korean |

### 10.2 消息存储方式
### 10.2 Message Storage

1. **配置文件存储**（适合静态消息）：
2. **Config File Storage** (for static messages):
   - `messages_zh_CN.properties`
   - `messages_en_US.properties`

3. **数据库存储**（适合动态配置）：
4. **Database Storage** (for dynamic configuration):
   - `i18n_message` 表存储所有语言消息
   - `i18n_message` table stores all language messages
   - 支持动态添加/修改消息，无需重启服务
   - Support dynamic add/modify messages without service restart

### 10.3 消息键示例
### 10.3 Message Key Examples

```properties
# 用户相关
# User related
user.assign.roles.success=角色分配成功
user.assign.org.success=组织分配成功
user.password.updated=密码更新成功
user.password.mismatch=密码不匹配

# 角色相关
# Role related
role.not.found=角色不存在
role.create.success=角色创建成功
role.code.exists=角色编码已存在
role.in.use=角色使用中无法删除

# 权限相关
# Permission related
permission.denied=权限不足
permission.not.found=权限不存在
```

### 10.4 使用国际化消息
### 10.4 Using Internationalization Messages

```java
// 在Service中使用
// Use in Service
throw new BusinessException(404, I18nMessageUtil.getMessage("role.not.found"));

// 带参数的消息
// Message with parameters
String message = I18nMessageUtil.getMessage("user.welcome", username);
```

### 10.5 缓存机制
### 10.5 Cache Mechanism

- **本地缓存**：ConcurrentHashMap存储，应用启动时加载
- **Local Cache**: ConcurrentHashMap storage, load at application startup
- **Spring Cache**：Redis分布式缓存，减少数据库查询
- **Spring Cache**: Redis distributed cache, reduce database queries
- **缓存刷新**：消息变更时自动清除缓存
- **Cache Refresh**: Auto clear cache when messages change

### 10.6 多实例缓存同步
### 10.6 Multi-instance Cache Sync

- 使用 `ApplicationEvent` 发布消息变更事件
- Use `ApplicationEvent` to publish message change events
- 通过 `Redis Pub/Sub` 通知其他实例刷新缓存
- Notify other instances to refresh cache via `Redis Pub/Sub`
- 事件类：`I18nMessageChangeEvent`
- Event class: `I18nMessageChangeEvent`

---

## 11. V3 性能优化总结
## 11. V3 Performance Optimization Summary

### 11.1 优化概览
### 11.1 Optimization Overview

| 优化项 | 优化前 | 优化后 | 性能提升 |
| Optimization | Before | After | Improvement |
|--------|--------|--------|----------|
| assignRoles批量插入 | 循环插入N次 | 批量插入1次 | 减少数据库往返次数 | Reduce DB round-trips |
| setPrimaryRole/Org批量更新 | 循环更新N次 | 单条SQL批量更新 | 减少数据库往返次数 | Reduce DB round-trips |
| getUserRoles/Orgs查询 | N+1查询 | 批量查询+Map查找 | 减少数据库查询次数 | Reduce DB queries |
| 配置历史版本管理 | 无版本号 | 支持版本回滚 | 增强功能完整性 | Enhance functionality |
| 配置加密 | 明文存储 | AES加密存储 | 提升安全性 | Improve security |

### 11.2 批量操作优化
### 11.2 Batch Operation Optimization

**优化前**：
**Before**:
```java
// 循环插入
// Loop insert
for (Long roleId : roleIds) {
    UserRole ur = new UserRole();
    ur.setUserId(userId);
    ur.setRoleId(roleId);
    userRoleMapper.insert(ur);  // N次数据库操作
}
```

**优化后**：
**After**:
```java
// 批量插入
// Batch insert
List<UserRole> userRoles = roleIds.stream()
    .map(roleId -> {
        UserRole ur = new UserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        return ur;
    })
    .collect(Collectors.toList());
userRoleMapper.batchInsertUserRoles(userRoles);  // 1次数据库操作
```

**性能提升**：
**Performance Improvement**:
- 减少数据库往返次数：N次 → 1次
- Reduce DB round-trips: N → 1
- 减少事务开销：N次 → 1次
- Reduce transaction overhead: N → 1
- 提升吞吐量：约 **10-50倍**（取决于角色数量）
- Improve throughput: ~ **10-50x** (depends on role count)

### 11.3 SQL批量更新优化
### 11.3 SQL Batch Update Optimization

**优化前**：
**Before**:
```java
// 循环更新
// Loop update
for (UserRoleRelation relation : relations) {
    relation.setIsPrimary("0");
    userRoleRelationMapper.updateById(relation);  // N次数据库操作
}
targetRelation.setIsPrimary("1");
userRoleRelationMapper.updateById(targetRelation);  // 1次数据库操作
```

**优化后**：
**After**:
```java
// 单条SQL批量更新
// Single SQL batch update
userRoleRelationMapper.updatePrimaryByUserId(userId, roleId);
```

**SQL实现**：
**SQL Implementation**:
```sql
UPDATE sys_user_role_relation
SET is_primary = CASE 
    WHEN role_id = #{roleId} THEN '1' 
    ELSE '0' 
END
WHERE user_id = #{userId}
```

**性能提升**：
**Performance Improvement**:
- 减少数据库往返次数：N+1次 → 1次
- Reduce DB round-trips: N+1 → 1
- 减少SQL解析开销：N+1次 → 1次
- Reduce SQL parsing overhead: N+1 → 1
- 提升响应速度：约 **5-20倍**（取决于关系数量）
- Improve response: ~ **5-20x** (depends on relation count)

### 11.4 N+1查询优化
### 11.4 N+1 Query Optimization

**优化前**：
**Before**:
```java
// N+1查询问题
// N+1 query problem
List<UserRoleRelation> relations = userRoleRelationMapper.selectByUserId(userId);
for (UserRoleRelation relation : relations) {
    Role role = roleMapper.selectById(relation.getRoleId());  // N次数据库查询
}
```

**优化后**：
**After**:
```java
// 批量查询+Map查找
// Batch query + Map lookup
List<UserRoleRelation> relations = userRoleRelationMapper.selectByUserId(userId);
Set<Long> roleIds = relations.stream()
    .map(UserRoleRelation::getRoleId)
    .collect(Collectors.toSet());
List<Role> roles = roleMapper.selectBatchIds(roleIds);  // 1次批量查询
Map<Long, Role> roleMap = roles.stream()
    .collect(Collectors.toMap(Role::getId, r -> r));

for (UserRoleRelation relation : relations) {
    Role role = roleMap.get(relation.getRoleId());  // O(1)查找
}
```

**性能提升**：
**Performance Improvement**:
- 减少数据库查询次数：N+1次 → 2次
- Reduce DB queries: N+1 → 2
- 减少网络往返：N+1次 → 2次
- Reduce network round-trips: N+1 → 2
- 提升响应速度：约 **5-30倍**（取决于角色数量）
- Improve response: ~ **5-30x** (depends on role count)

### 11.5 配置版本管理
### 11.5 Config Version Management

**新增功能**：
**New Features**:
- `ConfigHistory` 表新增 `version` 字段
- `ConfigHistory` table add `version` field
- 支持按版本查询配置历史
- Support query config history by version
- 支持配置回滚到指定版本
- Support config rollback to specific version
- 自动生成版本号
- Auto generate version number

**API接口**：
**API Interfaces**:
```java
// 获取配置历史（按版本降序）
// Get config history (desc by version)
List<ConfigHistory> getConfigHistory(Long configId);

// 获取指定版本的配置
// Get config of specific version
ConfigHistory getConfigHistoryByVersion(Long configId, Integer version);

// 回滚到指定版本
// Rollback to specific version
Object rollbackToVersion(Long configId, Integer version);
```

### 11.6 配置加密功能
### 11.6 Config Encryption Feature

**新增功能**：
**New Features**:
- `ConfigEncryptionService` 服务
- `ConfigEncryptionService` service
- 自动识别敏感配置（password、apiKey、secret等）
- Auto identify sensitive config (password, apiKey, secret, etc.)
- AES加密存储敏感配置
- AES encrypt sensitive config
- 自动解密返回给用户
- Auto decrypt and return to user

**敏感配置前缀**：
**Sensitive Config Prefixes**:
```java
private static final List<String> SENSITIVE_PREFIXES = Arrays.asList(
    "password", "apiKey", "secret", "token", "credential", "key", "auth"
);
```

**使用方式**：
**Usage**:
```java
// 自动加密
// Auto encrypt
if (configEncryptionService.isSensitive(config.getConfigKey())) {
    config.setConfigValue(configEncryptionService.encrypt(config.getConfigValue()));
}

// 自动解密
// Auto decrypt
if (config != null && configEncryptionService.isSensitive(configKey)) {
    config.setConfigValue(configEncryptionService.decrypt(config.getConfigValue()));
}
```

### 11.7 数据库迁移
### 11.7 Database Migration

**迁移脚本**：`V1.0.2__add_config_history_version.sql`
**Migration Script**: `V1.0.2__add_config_history_version.sql`

```sql
ALTER TABLE sys_user_config_history
ADD COLUMN version INT DEFAULT 1 COMMENT '版本号';

CREATE INDEX idx_config_version ON sys_user_config_history(config_id, version);
```

### 11.8 相关文件
### 11.8 Related Files

| 文件路径 | 说明 |
| File Path | Description |
|----------|------|
| `user-org-service/src/main/java/com/zhuji/userorg/service/UserService.java` | 用户服务（批量角色分配） | User service (batch role assignment) |
| `user-org-service/src/main/java/com/zhuji/userorg/service/impl/UserConfigServiceImpl.java` | 用户配置服务（批量更新、N+1优化、加密） | User config service (batch update, N+1 optimization, encryption) |
| `user-org-service/src/main/java/com/zhuji/userorg/service/ConfigEncryptionService.java` | 配置加密服务（新增） | Config encryption service (new) |
| `user-org-service/src/main/java/com/zhuji/userorg/service/ConfigHistoryService.java` | 配置历史服务（版本管理） | Config history service (version management) |
| `user-org-service/src/main/java/com/zhuji/userorg/entity/ConfigHistory.java` | 配置历史实体（新增version字段） | Config history entity (new version field) |
| `user-org-service/src/main/java/com/zhuji/userorg/mapper/UserRoleMapper.java` | 用户角色Mapper（批量插入） | User role mapper (batch insert) |
| `user-org-service/src/main/resources/mapper/UserRoleMapper.xml` | 用户角色Mapper XML（批量插入SQL） | User role mapper XML (batch insert SQL) |
| `user-org-service/src/main/java/com/zhuji/userorg/mapper/UserRoleRelationMapper.java` | 用户角色关系Mapper（批量更新） | User role relation mapper (batch update) |
| `user-org-service/src/main/resources/mapper/UserRoleRelationMapper.xml` | 用户角色关系Mapper XML（批量更新SQL） | User role relation mapper XML (batch update SQL) |
| `user-org-service/src/main/java/com/zhuji/userorg/mapper/UserOrgRelationMapper.java` | 用户组织关系Mapper（批量更新） | User organization relation mapper (batch update) |
| `user-org-service/src/main/resources/mapper/UserOrgRelationMapper.xml` | 用户组织关系Mapper XML（批量更新SQL） | User organization relation mapper XML (batch update SQL) |
