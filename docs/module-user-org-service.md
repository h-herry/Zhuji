# 用户组织服务 (user-org-service)

## 1. 模块概述

用户组织服务是整个系统的核心模块，负责管理系统用户、组织架构、角色和权限。采用RBAC（基于角色的访问控制）模型，支持集团公司多层级组织架构。

### 1.1 主要功能

- **用户管理**：用户注册、登录、注销、信息管理
- **组织架构管理**：支持集团-公司-部门-团队-岗位多层级组织架构
- **角色管理**：角色CRUD、角色权限分配、批量操作
- **权限管理**：细粒度权限控制，支持菜单、按钮、API三种资源类型
- **认证授权**：JWT Token认证、基于权限的访问控制
- **多角色支持**：用户可同时拥有多个角色，支持设置主角色
- **多组织支持**：用户可同时属于多个组织，支持设置主组织
- **可配置化**：用户、角色、组织级别的自定义配置管理
- **多语言支持**：完整的国际化(i18n)支持

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 基础框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| Redis | 7.x | 缓存、Session存储 |
| JWT | 0.12.x | Token认证 |
| Spring Cache | - | 缓存抽象 |
| Flowable | 7.0.0 | 工作流引擎 |

---

## 2. 数据库设计

### 2.1 ER关系图

```
┌─────────────┐     ┌──────────────────────────┐     ┌─────────────┐
│  sys_user   │────<│ sys_user_role_relation │>────│  sys_role   │
└─────────────┘     └──────────────────────────┘     └─────────────┘
       │                                              │
       │                                              │
       ▼                                              ▼
┌─────────────┐     ┌────────────────────────┐     ┌─────────────────┐
│sys_user_config│    │sys_role_permission     │<────│sys_permission   │
└─────────────┘     └────────────────────────┘     └─────────────────┘
       │                     │
       │                     │
       ▼                     ▼
┌─────────────┐     ┌────────────────────────┐
│sys_global_config│   │sys_role_config        │
└─────────────┘     └────────────────────────┘
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

#### 2.2.1 组织类型表 (org_type)

原来代码中的枚举改为数据库配置，支持动态修改。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| type_code | VARCHAR(50) | 类型编码（GROUP/COMPANY/DEPARTMENT/TEAM/POSITION） |
| type_name | VARCHAR(100) | 类型名称 |
| description | VARCHAR(255) | 描述 |
| sort_order | INT | 排序 |
| status | CHAR(1) | 状态：1-启用，0-禁用 |

#### 2.2.2 组织单位表 (org_unit)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| org_code | VARCHAR(50) | 组织编码（唯一） |
| org_name | VARCHAR(100) | 组织名称 |
| org_type_id | BIGINT | 组织类型ID |
| parent_id | BIGINT | 父组织ID |
| level_code | VARCHAR(100) | 层级编码（如：00001.00001.00001） |
| sort_order | INT | 排序 |
| status | CHAR(1) | 状态 |

#### 2.2.3 用户表 (sys_user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| org_id | BIGINT | 所属组织ID（主组织） |
| status | TINYINT | 状态：1-正常，0-禁用 |
| password_update_time | DATETIME | 密码更新时间 |
| login_fail_count | INT | 登录失败次数 |
| lock_until | DATETIME | 账户锁定到期时间 |

#### 2.2.4 角色表 (sys_role)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| role_code | VARCHAR(50) | 角色编码（唯一） |
| role_name | VARCHAR(100) | 角色名称 |
| description | VARCHAR(255) | 描述 |
| sort_order | INT | 排序 |
| status | CHAR(1) | 状态 |

#### 2.2.5 权限表 (sys_permission)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| permission_code | VARCHAR(100) | 权限编码（唯一） |
| permission_name | VARCHAR(100) | 权限名称 |
| resource_type | VARCHAR(20) | 资源类型：menu/button/api |
| path | VARCHAR(255) | 路径 |
| http_method | VARCHAR(10) | HTTP方法：GET/POST/PUT/DELETE |
| parent_id | BIGINT | 父权限ID |
| sort_order | INT | 排序 |
| status | CHAR(1) | 状态 |

#### 2.2.6 用户角色关联表 (sys_user_role_relation)

支持多角色，is_primary标识主角色。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| user_id | BIGINT | 用户ID |
| role_id | BIGINT | 角色ID |
| is_primary | VARCHAR(10) | 是否主角色：0-否，1-是 |
| create_time | DATETIME | 创建时间 |

#### 2.2.7 用户组织关联表 (sys_user_org_relation)

支持多组织，relation_type区分成员和负责人。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| user_id | BIGINT | 用户ID |
| org_id | BIGINT | 组织ID |
| relation_type | VARCHAR(20) | 关系类型：MEMBER-成员，LEADER-负责人 |
| is_primary | VARCHAR(10) | 是否主组织：0-否，1-是 |
| create_time | DATETIME | 创建时间 |

#### 2.2.8 角色权限关联表 (sys_role_permission)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| role_id | BIGINT | 角色ID |
| permission_id | BIGINT | 权限ID |
| create_time | DATETIME | 创建时间 |

#### 2.2.9 用户配置表 (sys_user_config)

支持用户级别的自定义配置。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| user_id | BIGINT | 用户ID |
| config_type | VARCHAR(50) | 配置类型 |
| config_key | VARCHAR(100) | 配置键 |
| config_value | TEXT | 配置值 |
| value_type | VARCHAR(20) | 值类型：STRING, NUMBER, BOOLEAN, JSON |
| description | VARCHAR(200) | 描述 |

#### 2.2.10 角色配置表 (sys_role_config)

支持角色级别的自定义配置。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| role_id | BIGINT | 角色ID |
| config_type | VARCHAR(50) | 配置类型 |
| config_key | VARCHAR(100) | 配置键 |
| config_value | TEXT | 配置值 |
| value_type | VARCHAR(20) | 值类型：STRING, NUMBER, BOOLEAN, JSON |
| description | VARCHAR(200) | 描述 |

#### 2.2.11 组织配置表 (sys_org_config)

支持组织级别的自定义配置。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| org_id | BIGINT | 组织ID |
| config_type | VARCHAR(50) | 配置类型 |
| config_key | VARCHAR(100) | 配置键 |
| config_value | TEXT | 配置值 |
| value_type | VARCHAR(20) | 值类型：STRING, NUMBER, BOOLEAN, JSON |
| description | VARCHAR(200) | 描述 |

#### 2.2.12 全局配置表 (sys_global_config)

支持系统级别的全局配置，包括多语言配置。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| config_type | VARCHAR(50) | 配置类型 |
| config_key | VARCHAR(100) | 配置键（唯一） |
| config_value | TEXT | 配置值 |
| value_type | VARCHAR(20) | 值类型：STRING, NUMBER, BOOLEAN, JSON |
| description | VARCHAR(200) | 描述 |
| sort_order | INT | 排序 |
| status | VARCHAR(10) | 状态：0-禁用，1-启用 |

#### 2.2.13 用户密码历史表 (sys_user_password_history)

记录用户密码变更历史，用于密码重复校验。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| user_id | BIGINT | 用户ID |
| password_hash | VARCHAR(255) | 历史密码哈希值 |
| create_time | DATETIME | 创建时间 |

#### 2.2.14 Token黑名单表 (sys_token_blacklist)

记录已注销的Token，防止被恶意使用。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| token | VARCHAR(500) | Token值 |
| user_id | BIGINT | 用户ID |
| expires_at | DATETIME | Token过期时间 |
| created_at | DATETIME | 创建时间 |

#### 2.2.15 用户配置历史表 (sys_user_config_history)

记录配置变更历史，支持配置回滚。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| user_id | BIGINT | 用户ID（NULL表示全局配置） |
| config_type | VARCHAR(50) | 配置类型 |
| config_key | VARCHAR(100) | 配置键 |
| config_value | TEXT | 配置值 |
| operation_type | VARCHAR(20) | 操作类型：CREATE/UPDATE/DELETE |
| operator_id | BIGINT | 操作人ID |
| create_time | DATETIME | 创建时间 |

---

## 3. API接口

### 3.1 认证接口 (AuthController)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/v1/auth/login | 用户登录 | 否 |
| POST | /api/v1/auth/register | 用户注册 | 否 |
| POST | /api/v1/auth/logout | 用户注销 | 是 |
| POST | /api/v1/auth/refresh | 刷新Token | 是 |

### 3.2 用户管理接口 (UserController)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/v1/users | 创建用户 | 是 |
| GET | /api/v1/users/{id} | 获取用户详情 | 是 |
| GET | /api/v1/users | 分页查询用户 | 是 |
| PUT | /api/v1/users/{id} | 更新用户 | 是 |
| DELETE | /api/v1/users/{id} | 删除用户 | 是 |
| POST | /api/v1/users/{id}/roles | 分配用户角色(多角色) | 是 |
| GET | /api/v1/users/{id}/roles | 获取用户角色列表 | 是 |
| DELETE | /api/v1/users/{id}/roles/{roleId} | 移除用户角色 | 是 |
| PUT | /api/v1/users/{id}/roles/{roleId}/primary | 设置主角色 | 是 |
| POST | /api/v1/users/{id}/orgs | 分配用户组织(多组织) | 是 |
| GET | /api/v1/users/{id}/orgs | 获取用户组织列表 | 是 |
| DELETE | /api/v1/users/{id}/orgs/{orgId} | 移除用户组织 | 是 |
| PUT | /api/v1/users/{id}/orgs/{orgId}/primary | 设置主组织 | 是 |
| GET | /api/v1/users/{id}/configs | 获取用户配置列表 | 是 |
| POST | /api/v1/users/{id}/configs | 创建用户配置 | 是 |
| PUT | /api/v1/users/{id}/configs/{configId} | 更新用户配置 | 是 |
| DELETE | /api/v1/users/{id}/configs/{configId} | 删除用户配置 | 是 |

### 3.3 角色管理接口 (RoleController)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/v1/roles | 分页查询角色 | 是 |
| GET | /api/v1/roles/all | 获取所有角色 | 是 |
| GET | /api/v1/roles/{id} | 获取角色详情 | 是 |
| GET | /api/v1/roles/{id}/permissions | 获取角色权限ID列表 | 是 |
| POST | /api/v1/roles | 创建角色 | 是 |
| PUT | /api/v1/roles/{id} | 更新角色 | 是 |
| DELETE | /api/v1/roles/{id} | 删除角色 | 是 |
| POST | /api/v1/roles/{id}/permissions | 分配角色权限 | 是 |
| POST | /api/v1/roles/batch | 批量创建角色 | 是 |
| PUT | /api/v1/roles/batch | 批量更新角色 | 是 |
| POST | /api/v1/roles/{id}/enable | 启用角色 | 是 |
| POST | /api/v1/roles/{id}/disable | 禁用角色 | 是 |
| POST | /api/v1/roles/{id}/copy-permissions/{targetRoleId} | 复制角色权限 | 是 |

### 3.4 权限管理接口 (PermissionController)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/v1/permissions | 分页查询权限 | 是 |
| GET | /api/v1/permissions/all | 获取所有权限 | 是 |
| GET | /api/v1/permissions/{id} | 获取权限详情 | 是 |
| GET | /api/v1/permissions/user/{userId} | 获取用户的权限列表 | 是 |
| GET | /api/v1/permissions/role/{roleId} | 获取角色的权限列表 | 是 |
| POST | /api/v1/permissions | 创建权限 | 是 |
| PUT | /api/v1/permissions/{id} | 更新权限 | 是 |
| DELETE | /api/v1/permissions/{id} | 删除权限 | 是 |

### 3.5 组织管理接口 (OrgUnitController)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/v1/orgs | 分页查询组织 | 是 |
| GET | /api/v1/orgs/tree | 获取组织树 | 是 |
| GET | /api/v1/orgs/{id} | 获取组织详情 | 是 |
| POST | /api/v1/orgs | 创建组织 | 是 |
| PUT | /api/v1/orgs/{id} | 更新组织 | 是 |
| DELETE | /api/v1/orgs/{id} | 删除组织 | 是 |
| GET | /api/v1/orgs/types | 获取所有组织类型 | 是 |

### 3.6 配置管理接口 (ConfigController)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/v1/configs | 分页查询配置列表 | 是 |
| GET | /api/v1/configs/{id} | 获取配置详情 | 是 |
| GET | /api/v1/configs/key/{configKey} | 根据Key获取配置 | 是 |
| GET | /api/v1/configs/type/{configType} | 根据类型获取配置列表 | 是 |
| POST | /api/v1/configs | 创建配置 | 是 |
| PUT | /api/v1/configs/{id} | 更新配置 | 是 |
| DELETE | /api/v1/configs/{id} | 删除配置 | 是 |
| GET | /api/v1/configs/value/{configKey} | 获取配置值 | 是 |
| GET | /api/v1/configs/types | 获取所有配置类型 | 是 |
| POST | /api/v1/configs/refresh-cache | 刷新配置缓存 | 是 |
| PUT | /api/v1/configs/batch | 批量更新配置 | 是 |
| GET | /api/v1/configs/type/{configType}/map | 获取类型的全部配置为Map | 是 |

### 3.7 多语言接口 (I18nController)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/v1/i18n/languages | 获取支持的语言列表 | 否 |
| POST | /api/v1/i18n/switch | 切换语言 | 否 |
| GET | /api/v1/i18n/current | 获取当前语言 | 否 |
| GET | /api/v1/i18n/validate/{lang} | 验证语言是否支持 | 否 |

---

## 4. API调用示例

### 4.1 多角色分配

```bash
# 为用户分配多个角色
POST /api/v1/users/1/roles
Content-Type: application/json

[1, 2, 3]

# 设置第一个角色为主角色
# isPrimary参数：1-第一个为主角色，0-不设主角色
```

**响应示例**：
```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

### 4.2 多组织分配

```bash
# 为用户分配多个组织
POST /api/v1/users/1/orgs?isPrimary=1
Content-Type: application/json

[1, 2, 3]
```

### 4.3 全局配置管理

```bash
# 获取所有支持的语言
GET /api/v1/configs/type/i18n

# 获取默认语言配置
GET /api/v1/configs/key/default.language

# 创建新的全局配置
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

```bash
# 切换到英文
POST /api/v1/i18n/switch?lang=en_US

# 获取当前语言
GET /api/v1/i18n/current
```

---

## 5. 核心代码

### 5.1 UserConfigServiceImpl - 多角色分配

```java
@Override
@Transactional
public void batchAssignRoles(Long userId, List<Long> roleIds, String isPrimary) {
    // 先删除旧的角色关联
    LambdaQueryWrapper<UserRoleRelation> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserRoleRelation::getUserId, userId);
    userRoleRelationMapper.delete(queryWrapper);

    // 批量插入新的角色关联
    for (int i = 0; i < roleIds.size(); i++) {
        Long roleId = roleIds.get(i);
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("role.not.found"));
        }

        UserRoleRelation relation = new UserRoleRelation();
        relation.setUserId(userId);
        relation.setRoleId(roleId);
        // 第一个角色设为主角色
        relation.setIsPrimary((i == 0 && "1".equals(isPrimary)) ? "1" : "0");
        relation.setCreateTime(LocalDateTime.now());
        userRoleRelationMapper.insert(relation);
    }
}
```

### 5.2 ConfigManagementService - 全局配置管理

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

### 5.3 I18nController - 多语言切换

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
    Cookie localeCookie = new Cookie("locale", lang);
    localeCookie.setMaxAge(604800); // 7天
    localeCookie.setPath("/");
    response.addCookie(localeCookie);

    return ApiResponse.success();
}
```

---

## 6. 初始化数据

### 6.1 预定义角色

| 角色编码 | 角色名称 | 说明 |
|----------|----------|------|
| SUPER_ADMIN | 超级管理员 | 拥有所有权限 |
| ADMIN | 管理员 | 系统管理员 |
| USER | 普通用户 | 基础权限 |
| GUEST | 访客 | 访客用户 |

### 6.2 预定义权限分类

| 分类 | 说明 |
|------|------|
| 用户管理 | USER_VIEW, USER_CREATE, USER_UPDATE, USER_DELETE, USER_ROLE_ASSIGN |
| 角色管理 | ROLE_VIEW, ROLE_CREATE, ROLE_UPDATE, ROLE_DELETE, ROLE_PERM_ASSIGN |
| 权限管理 | PERM_VIEW, PERM_CREATE, PERM_UPDATE, PERM_DELETE |
| 组织管理 | ORG_VIEW, ORG_CREATE, ORG_UPDATE, ORG_DELETE |
| 系统监控 | MONITOR_VIEW |
| 通知管理 | NOTIFICATION_VIEW, NOTIFICATION_SEND |
| 文件管理 | FILE_UPLOAD, FILE_DOWNLOAD, FILE_DELETE |
| 工作流 | WORKFLOW_VIEW, WORKFLOW_CREATE, WORKFLOW_START, WORKFLOW_APPROVE |
| 第三方服务 | THIRDPARTY_VIEW, THIRDPARTY_SMS |
| 系统参数 | SYSPARAM_VIEW, SYSPARAM_UPDATE |

### 6.3 全局配置初始化数据

| 配置类型 | 配置键 | 配置值 | 说明 |
|----------|--------|--------|------|
| i18n | default.language | zh_CN | 系统默认语言 |
| i18n | supported.languages | zh_CN,en_US,zh_TW,ja_JP,ko_KR | 支持的语言列表 |
| system | max.login.attempts | 5 | 最大登录尝试次数 |
| system | session.timeout | 1800 | 会话超时时间（秒） |
| security | password.min.length | 8 | 密码最小长度 |
| security | password.require.special.char | true | 密码必须包含特殊字符 |
| security | password.expiry.days | 90 | 密码过期天数 |
| role | default.user.role | USER | 新用户默认角色 |
| org | allow.virtual.org | true | 允许创建虚拟组织 |
| org | max.org.depth | 10 | 最大组织层级深度 |

### 6.4 默认用户

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |
| testuser | user123 | 普通用户 |

---

## 7. 配置说明

### 7.1 application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zhuji_user_org
    username: root
    password: root

  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: ZhujiSecretKeyForJWTTokenGeneration2024VeryLongSecretKey
  expiration: 86400000  # 24小时
```

---

## 8. 安全机制

### 8.1 密码安全

- 使用BCrypt加密存储
- 登录时验证密码
- 支持密码最小长度配置（默认8位）
- 支持密码最大长度配置（默认32位）
- 支持密码特殊字符要求配置
- **密码策略校验**：密码必须包含大写字母、小写字母、数字、特殊字符

### 8.2 用户锁定机制

- 登录失败次数限制（默认5次）
- 失败达到上限后账户自动锁定（默认30分钟）
- 使用Redis记录登录失败次数和锁定状态
- 登录成功后自动重置失败计数

### 8.3 Token安全

- JWT Token包含用户信息和权限列表
- Token存储在Redis中，支持主动注销
- Token过期时间24小时
- **Token黑名单机制**：注销后Token加入黑名单，防止恶意使用
- **双Token机制**：支持accessToken和refreshToken

### 8.4 权限校验

- 用户访问API时，从Token中提取权限信息
- 通过Spring Security进行权限校验
- 支持方法级别的权限注解（@PreAuthorize）

### 8.5 密码历史管理

- 记录最近N次密码（默认5次）
- 修改密码时校验不能与历史密码重复
- 支持密码过期策略（默认90天）

---

## 9. 缓存策略

| 缓存Key | 说明 | 过期时间 |
|---------|------|----------|
| user::{id} | 用户信息缓存 | 1小时 |
| user-roles::{userId} | 用户角色列表缓存 | 30分钟 |
| user-permissions::{userId} | 用户权限列表缓存 | 30分钟 |
| auth:token::{userId} | 用户Token缓存 | 24小时 |
| config:{configKey} | 全局配置缓存 | 30分钟 |
| config:type:{configType} | 按类型配置缓存 | 30分钟 |
| i18n-messages:{messageKey}:{locale} | 多语言消息缓存 | 1小时 |
| i18n-messages:locale:{locale} | 按语言缓存消息 | 1小时 |
| i18n-messages:module:{locale}:{module} | 按模块缓存消息 | 1小时 |

---

## 10. 多语言(i18n)支持

### 10.1 支持的语言

| 语言代码 | 语言名称 |
|----------|----------|
| zh_CN | 简体中文 |
| en_US | English |
| zh_TW | 繁體中文 |
| ja_JP | 日本語 |
| ko_KR | 한국어 |

### 10.2 消息存储

消息支持两种存储方式：

1. **静态资源文件**（适合固定消息）：
   - `common-i18n/src/main/resources/i18n/messages_zh_CN.properties` - 中文消息
   - `common-i18n/src/main/resources/i18n/messages_en_US.properties` - 英文消息

2. **数据库存储**（适合动态配置）：
   - `i18n_message` 表存储所有语言消息
   - 支持动态添加/修改消息，无需重启服务

### 10.3 消息键示例

```properties
# 用户相关
user.assign.roles.success=角色分配成功
user.assign.org.success=组织分配成功
user.password.updated=密码更新成功
user.password.mismatch=密码不匹配

# 角色相关
role.not.found=角色不存在
role.create.success=角色创建成功
role.code.exists=角色编码已存在
role.in.use=角色使用中无法删除

# 权限相关
permission.denied=权限不足
permission.not.found=权限不存在
```

### 10.4 使用国际化消息

```java
// 在Service中使用
throw new BusinessException(404, I18nMessageUtil.getMessage("role.not.found"));

// 带参数的消息
String message = I18nMessageUtil.getMessage("user.welcome", username);
```

### 10.5 缓存机制

- **本地缓存**：ConcurrentHashMap存储，应用启动时加载
- **Spring Cache**：Redis分布式缓存，减少数据库查询
- **缓存刷新**：消息变更时自动清除缓存

### 10.6 多实例缓存同步

- 使用 `ApplicationEvent` 发布消息变更事件
- 通过 `Redis Pub/Sub` 通知其他实例刷新缓存
- 事件类：`I18nMessageChangeEvent`
