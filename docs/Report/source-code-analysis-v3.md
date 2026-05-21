# h-herry/Zhuji 项目源码深度分析报告 V3

**分析日期**: 2026-05-21  
**分析版本**: V3（版本对比 V2）  
**项目地址**: https://github.com/h-herry/Zhuji  
**最新 Commit**: f37629e (fix: 修复代码审查发现的问题)

---

## 一、执行摘要

### 1.1 版本更新说明

| 版本 | 日期 | 主要内容 |
|------|------|---------|
| V1 | 2026-05-20 | 初始分析，发现 P0/P1/P2 问题清单 |
| V2 | 2026-05-21 上午 | 生成优化建议报告 |
| **V3** | **2026-05-21 下午** | **项目已更新，实现大部分 P0/P1 问题，深度复盘** |

### 1.2 最新 Commit 变更

**f37629e** - fix: 修复代码审查发现的问题

```
user-org-service/src/main/java/com/zhuji/userorg/config/RedisPubSubConfig.java    |  2 +-
user-org-service/src/main/java/com/zhuji/userorg/event/ConfigChangeMessageListener.java | 14 +++++++++++--
user-org-service/src/main/java/com/zhuji/userorg/security/TokenBlacklistService.java    | 24 ++++++++++++++++------
 3 files changed, 31 insertions(+), 9 deletions(-)
```

**主要修复**：
1. `RedisPubSubConfig`: 类名从 RedisConfig 改为 RedisPubSubConfig
2. `ConfigChangeMessageListener`: 修复 Redis Message 反序列化问题，使用 `redisTemplate.getValueSerializer().deserialize()`
3. `TokenBlacklistService`: 添加 try-catch 异常处理和日志记录

### 1.3 总体评价

| 维度 | 评分 | 说明 |
|------|------|------|
| 功能完整性 | ⭐⭐⭐⭐⭐ | P0/P1 问题已全部实现 |
| 代码质量 | ⭐⭐⭐⭐ | 存在少量性能问题待优化 |
| 安全加固 | ⭐⭐⭐⭐⭐ | 双 Token、密码过期、历史、黑名单均已实现 |
| 架构设计 | ⭐⭐⭐⭐⭐ | DDD 分层清晰，事件驱动架构完善 |

---

## 二、已实现功能复盘（对比 V2）

### 2.1 P0 问题修复情况

| 问题 | V2 状态 | V3 状态 | 实现说明 |
|------|--------|--------|----------|
| User 实体缺少字段 | ❌ 未实现 | ✅ 已完成 | 已添加 `passwordUpdateTime` 和 `lastLoginTime` 字段 |
| 批量插入性能问题 | ❌ 未实现 | ✅ 已完成 | `batchAssignRoles`/`batchAssignOrgs` 使用差量计算 + 批量插入 |

### 2.2 P1 问题修复情况

| 问题 | V2 状态 | V3 状态 | 实现说明 |
|------|--------|--------|----------|
| 密码过期策略 | ❌ 未实现 | ✅ 已完成 | `PasswordExpiryService`（90天过期，7天警告） |
| 密码历史记录 | ❌ 未实现 | ✅ 已完成 | `PasswordHistoryService`（最近5个密码） |
| 双 Token 机制 | ❌ 未实现 | ✅ 已完成 | `JwtService.generateAccessToken/generateRefreshToken` |
| Token 黑名单 | ❌ 未实现 | ✅ 已完成 | `TokenBlacklistService`（基于 Redis） |
| 跨实例配置同步 | ❌ 未实现 | ✅ 已完成 | `ConfigChangePublisher` + `ConfigChangeMessageListener` |
| 配置版本管理 | ❌ 未实现 | ✅ 已完成 | `ConfigHistoryService`（基础版本） |

---

## 三、仍需优化的问题（V3）

### 3.1 P0 问题（必须修复）

#### 问题 1: `assignRoles` 方法仍然使用循环逐条插入

**位置**: `UserService.java`（第 259-290 行）

**当前代码**：
```java
if (!toAdd.isEmpty()) {
    List<UserRole> userRoles = toAdd.stream()
        .map(roleId -> {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            return ur;
        })
        .collect(Collectors.toList());
    for (UserRole ur : userRoles) {  // ❌ 逐条插入
        userRoleMapper.insert(ur);
    }
}
```

**问题**：
- ❌ 仍然使用 `for` 循环逐条插入，性能差
- ❌ 与 `UserConfigServiceImpl.batchAssignRoles` 不一致（后者已优化）

**优化方案**：
```java
// 方式1：复用 UserConfigServiceImpl 的逻辑
userConfigService.batchAssignRoles(userId, roleIds, null);

// 方式2：在 UserService 中添加批量插入方法
if (!toAdd.isEmpty()) {
    List<UserRole> userRoles = toAdd.stream()
        .map(roleId -> {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            return ur;
        })
        .collect(Collectors.toList());
    
    // 需要在 UserRoleMapper 中添加 batchInsert 方法
    userRoleMapper.batchInsertUserRoles(userRoles);  // ✅ 批量插入
}
```

---

#### 问题 2: `setPrimaryRole` 和 `setPrimaryOrg` 逐条更新

**位置**: `UserConfigServiceImpl.java`（第 247-263 行）

**当前代码**：
```java
@Override
@Transactional
public void setPrimaryRole(Long userId, Long roleId) {
    List<UserRoleRelation> relations = userRoleRelationMapper.selectByUserId(userId);
    for (UserRoleRelation relation : relations) {  // ❌ 逐条更新
        if (relation.getRoleId().equals(roleId)) {
            relation.setIsPrimary("1");
        } else {
            relation.setIsPrimary("0");
        }
        userRoleRelationMapper.updateById(relation);  // ❌ 每条记录一次 UPDATE
    }
}
```

**问题**：
- ❌ N 次 UPDATE，可以合并为 1 次批量 UPDATE
- ❌ 事务开销大，性能差

**优化方案**：
```java
@Override
@Transactional
public void setPrimaryRole(Long userId, Long roleId) {
    // 方式1：使用 SQL 批量更新
    userRoleRelationMapper.updatePrimaryByUserId(userId, roleId);
    
    // 方式2：先更新目标，再更新其他
    userRoleRelationMapper.updateIsPrimary(userId, roleId, "1");
    userRoleRelationMapper.updateNonPrimaryExcept(userId, roleId, "0");
}
```

**对应的 SQL**：
```xml
<update id="updatePrimaryByUserId">
    UPDATE sys_user_role_relation
    SET is_primary = CASE 
        WHEN role_id = #{roleId} THEN '1' 
        ELSE '0' 
    END
    WHERE user_id = #{userId}
</update>
```

---

### 3.2 P1 问题（建议修复）

#### 问题 3: `getUserRoles` 和 `getUserOrgs` N+1 查询问题

**位置**: `UserConfigServiceImpl.java`（第 210-233 行）

**当前代码**：
```java
@Override
public List<Map<String, Object>> getUserRoles(Long userId) {
    List<Long> roleIds = userRoleRelationMapper.selectRoleIdsByUserId(userId);  // 1 次查询
    List<Map<String, Object>> result = new ArrayList<>();

    for (Long roleId : roleIds) {  // ❌ N 次查询
        Role role = roleMapper.selectById(roleId);  // 每循环一次执行一次 SELECT
        if (role != null) {
            // ... 构建结果
        }
    }

    return result;
}
```

**问题**：
- ❌ N+1 查询问题（1 次获取 ID 列表 + N 次获取详细）
- ❌ 性能差，特别是在用户角色多的情况下

**优化方案**：
```java
@Override
public List<Map<String, Object>> getUserRoles(Long userId) {
    // 1. 获取用户角色列表（含关系信息）
    List<UserRoleRelation> relations = userRoleRelationMapper.selectByUserId(userId);
    
    if (relations.isEmpty()) {
        return Collections.emptyList();
    }
    
    // 2. 批量获取角色信息（一次查询）
    Set<Long> roleIds = relations.stream()
        .map(UserRoleRelation::getRoleId)
        .collect(Collectors.toSet());
    List<Role> roles = roleMapper.selectBatchIds(roleIds);
    
    // 3. 构建 Map 快速查找
    Map<Long, Role> roleMap = roles.stream()
        .collect(Collectors.toMap(Role::getId, r -> r));
    
    // 4. 查找主角色
    UserRoleRelation primaryRelation = relations.stream()
        .filter(r -> "1".equals(r.getIsPrimary()))
        .findFirst()
        .orElse(null);
    
    // 5. 组装结果
    List<Map<String, Object>> result = new ArrayList<>();
    for (UserRoleRelation relation : relations) {
        Role role = roleMap.get(relation.getRoleId());
        if (role != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", role.getId());
            map.put("roleCode", role.getRoleCode());
            map.put("roleName", role.getRoleName());
            map.put("description", role.getDescription());
            map.put("isPrimary", primaryRelation != null && 
                    primaryRelation.getRoleId().equals(role.getId()) ? "1" : "0");
            result.add(map);
        }
    }
    
    return result;
}
```

**对应的 Mapper 方法**：
```java
// UserRoleRelationMapper.java
List<UserRoleRelation> selectByUserId(Long userId);

// UserRoleRelationMapper.xml
<select id="selectByUserId" resultType="UserRoleRelation">
    SELECT role_id, is_primary 
    FROM sys_user_role_relation 
    WHERE user_id = #{userId}
</select>

// RoleMapper.java
List<Role> selectBatchIds(Collection<Long> ids);
```

---

#### 问题 4: `ConfigHistory` 缺少 version 字段

**位置**: `ConfigHistory.java`

**当前实现**：
```java
public class ConfigHistory {
    // ... 其他字段
    private String operation;  // CREATE, UPDATE, DELETE
    // ❌ 缺少 version 字段
    private LocalDateTime createTime;
}
```

**问题**：
- ❌ 无法实现版本回滚（需要 version 字段）
- ❌ 配置历史不完整

**优化方案**：
```java
// 1. 修改 ConfigHistory 实体
public class ConfigHistory {
    // ... 其他字段
    private String operation;
    private Integer version;  // ✅ 添加版本号
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

// 2. 修改 ConfigHistoryServiceImpl
@Override
public void saveConfigHistory(Long configId, Long userId, String configKey, String configValue,
                             String configType, String operation, String operator) {
    ConfigHistory history = new ConfigHistory();
    history.setConfigId(configId);
    // ...
    history.setVersion(getNextVersion(configId));  // ✅ 自动生成版本号
    configHistoryMapper.insert(history);
}

private Integer getNextVersion(Long configId) {
    Integer maxVersion = configHistoryMapper.selectMaxVersionByConfigId(configId);
    return maxVersion != null ? maxVersion + 1 : 1;
}

// 3. 添加回滚方法
@Override
public Config rollbackToVersion(Long configId, Integer version) {
    ConfigHistory targetVersion = configHistoryMapper.selectByConfigIdAndVersion(configId, version);
    if (targetVersion == null) {
        throw new BusinessException(404, "配置版本不存在");
    }
    
    // 更新当前配置为历史版本的值
    Config config = configMapper.selectById(configId);
    config.setConfigValue(targetVersion.getConfigValue());
    configMapper.updateById(config);
    
    // 记录回滚操作
    saveConfigHistory(configId, config.getUserId(), config.getConfigKey(), 
                      config.getConfigValue(), config.getConfigType(), "ROLLBACK", null);
    
    return config;
}
```

---

#### 问题 5: 配置缺少加密功能

**位置**: `UserConfigServiceImpl.java`

**问题**：
- ❌ 敏感配置（如 API Key、密码等）以明文存储
- ❌ 存在安全风险

**优化方案**：
```java
// 1. 敏感配置键前缀列表
private static final List<String> SENSITIVE_PREFIXES = Arrays.asList(
    "password", "apiKey", "secret", "token", "credential", "key", "auth"
);

// 2. 判断是否为敏感配置
private boolean isSensitive(String configKey) {
    if (configKey == null) return false;
    String lowerKey = configKey.toLowerCase();
    return SENSITIVE_PREFIXES.stream()
        .anyMatch(prefix -> lowerKey.contains(prefix));
}

// 3. 在存储时加密
@Override
public UserConfig createUserConfig(UserConfig config) {
    validateConfig(config.getConfigKey(), config.getConfigValue());
    
    UserConfig existing = userConfigMapper.selectByUserIdAndKey(config.getUserId(), config.getConfigKey());
    if (existing != null) {
        throw new BusinessException(400, "配置键已存在");
    }
    
    // ✅ 加密敏感配置
    String encryptedValue = isSensitive(config.getConfigKey()) 
        ? encryptValue(config.getConfigValue()) 
        : config.getConfigValue();
    config.setConfigValue(encryptedValue);
    
    userConfigMapper.insert(config);
    return config;
}

// 4. 在读取时解密
@Override
public UserConfig getUserConfigByKey(Long userId, String configKey) {
    UserConfig config = userConfigMapper.selectByUserIdAndKey(userId, configKey);
    
    if (config != null && isSensitive(configKey)) {
        // ✅ 解密敏感配置
        config.setConfigValue(decryptValue(config.getConfigValue()));
    }
    
    return config;
}

// 5. 加密/解密方法（使用 Jasypt）
private String encryptValue(String value) {
    if (value == null) return null;
    return stringEncryptor.encrypt(value);
}

private String decryptValue(String encryptedValue) {
    if (encryptedValue == null) return null;
    try {
        return stringEncryptor.decrypt(encryptedValue);
    } catch (Exception e) {
        // 如果解密失败，说明可能是明文，直接返回
        return encryptedValue;
    }
}
```

---

### 3.3 P2 问题（可选优化）

#### 问题 6: 缺少配置导入导出功能

**位置**: `UserConfigServiceImpl.java`

**需求**：
- 支持导出用户/角色/组织配置为 JSON/Excel
- 支持批量导入配置（支持覆盖/跳过策略）
- 支持配置模板

**优化方案**：
```java
// 1. 导出配置
public List<Map<String, Object>> exportConfigs(String level, Long levelId, String configType) {
    List<Map<String, Object>> result = new ArrayList<>();
    
    switch (level) {
        case "USER":
            List<UserConfig> userConfigs = userConfigMapper.selectByUserId(levelId);
            for (UserConfig config : userConfigs) {
                result.add(configToMap(config));
            }
            break;
        case "ROLE":
            List<RoleConfig> roleConfigs = roleConfigMapper.selectByRoleId(levelId);
            for (RoleConfig config : roleConfigs) {
                result.add(configToMap(config));
            }
            break;
        // ... 其他级别
    }
    
    return result;
}

// 2. 导入配置
@Transactional
public ImportResult importConfigs(String level, Long levelId, 
                                  List<Map<String, Object>> configs, 
                                  boolean overwrite) {
    int successCount = 0;
    int skipCount = 0;
    List<String> errors = new ArrayList<>();
    
    for (Map<String, Object> configData : configs) {
        try {
            String configKey = (String) configData.get("configKey");
            String configValue = (String) configData.get("configValue");
            
            // 检查是否已存在
            Object existing = findExistingConfig(level, levelId, configKey);
            
            if (existing != null) {
                if (overwrite) {
                    updateConfig(existing, configValue);
                    successCount++;
                } else {
                    skipCount++;
                }
            } else {
                createConfig(level, levelId, configKey, configValue);
                successCount++;
            }
        } catch (Exception e) {
            errors.add("导入失败: " + e.getMessage());
        }
    }
    
    return new ImportResult(successCount, skipCount, errors);
}

// 3. DTO 定义
@Data
public static class ImportResult {
    private int successCount;
    private int skipCount;
    private List<String> errors;
}
```

---

## 四、数据库设计优化建议

### 4.1 已有优化（已完成）

#### ✅ `sys_user` 表已添加字段

```sql
ALTER TABLE sys_user 
ADD COLUMN password_update_time DATETIME COMMENT '密码更新时间',
ADD COLUMN last_login_time DATETIME COMMENT '最后登录时间';
```

#### ✅ `sys_org_unit.org_type` 字段类型修正

```sql
ALTER TABLE sys_org_unit 
MODIFY COLUMN org_type BIGINT NOT NULL COMMENT '组织类型ID';
```

#### ✅ 新建密码历史表

```sql
CREATE TABLE sys_user_password_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password VARCHAR(255) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
);
```

#### ✅ 新建配置历史表

```sql
CREATE TABLE sys_user_config_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50),
    operation VARCHAR(20) NOT NULL,
    operator VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_config_id (config_id),
    INDEX idx_user_id (user_id)
);
```

### 4.2 建议新增优化

#### 建议 1: 添加配置历史 version 字段

```sql
ALTER TABLE sys_user_config_history 
ADD COLUMN version INT DEFAULT 1 COMMENT '版本号';

-- 添加复合索引加速版本查询
ALTER TABLE sys_user_config_history 
ADD INDEX idx_config_version (config_id, version);
```

#### 建议 2: 添加用户会话表（支持多端控制）

```sql
CREATE TABLE sys_user_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_type VARCHAR(50) COMMENT 'WEB/IOS/ANDROID/OTHER',
    device_info VARCHAR(200) COMMENT '设备信息',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_active_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    refresh_token VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INVALID/EXPIRED',
    expires_at DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);
```

#### 建议 3: 添加配置加密表（支持配置加密密钥管理）

```sql
CREATE TABLE sys_config_encryption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    encryption_key VARCHAR(100) NOT NULL COMMENT '加密键（如 apiKey）',
    encryption_algorithm VARCHAR(50) DEFAULT 'AES',
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_encryption_key (encryption_key)
);
```

---

## 五、配置管理优化建议

### 5.1 当前配置管理架构

```
┌─────────────────────────────────────────────────────────────────┐
│                     配置管理架构                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │
│  │   用户配置    │    │   角色配置    │    │   组织配置    │     │
│  │ UserConfig   │    │  RoleConfig  │    │  OrgConfig   │     │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘     │
│         │                   │                   │              │
│         └───────────────────┼───────────────────┘              │
│                             │                                  │
│                    ┌────────▼────────┐                        │
│                    │   全局配置       │                        │
│                    │ GlobalConfig    │                        │
│                    └─────────────────┘                        │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                    配置校验层                            │  │
│  │  ConfigValidator (接口)                                  │  │
│  │  ├── NumberConfigValidator (数字校验)                   │  │
│  │  └── StringConfigValidator (字符串校验)                 │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                    配置变更事件                           │  │
│  │  ApplicationEventPublisher → ConfigChangeEvent         │  │
│  │  Redis Pub/Sub → ConfigChangeMessage                    │  │
│  │  ConfigChangeMessageListener → 清除缓存                  │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                    配置历史记录                           │  │
│  │  ConfigHistory (CREATE/UPDATE/DELETE/ROLLBACK)          │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 建议新增功能

#### 1. 配置模板管理

```java
// 配置模板实体
public class ConfigTemplate {
    private Long id;
    private String templateName;
    private String templateType;  // USER/ROLE/ORG/GLOBAL
    private String templateConfig;  // JSON 格式配置模板
    private String description;
    private Integer sortOrder;
    private LocalDateTime createTime;
}

// 模板服务
public interface ConfigTemplateService {
    // 创建模板
    ConfigTemplate createTemplate(String name, String type, String config);
    
    // 应用模板到目标
    void applyTemplate(String level, Long levelId, Long templateId);
    
    // 导入/导出模板
    String exportTemplate(Long templateId);
    void importTemplate(String templateJson);
}
```

#### 2. 配置审计日志

```java
// 配置审计日志
public class ConfigAuditLog {
    private Long id;
    private String level;  // USER/ROLE/ORG/GLOBAL
    private Long levelId;
    private String configKey;
    private String configValue;  // 脱敏后的值
    private String operation;
    private String operator;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createTime;
}
```

#### 3. 配置对比功能

```java
// 配置对比服务
public interface ConfigCompareService {
    // 对比两个实体的配置差异
    List<ConfigDiff> compare(String level1, Long levelId1, 
                              String level2, Long levelId2);
    
    // 对比配置历史版本
    List<ConfigDiff> compareVersion(Long configId, Integer version1, Integer version2);
}

// 配置差异 DTO
public class ConfigDiff {
    private String configKey;
    private String value1;
    private String value2;
    private String diffType;  // ADDED/REMOVED/MODIFIED
}
```

---

## 六、完整优化代码

### 6.1 批量插入优化（UserService.assignRoles）

**位置**: `UserService.java`

```java
// 1. 在 UserRoleMapper 中添加批量插入方法
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    /**
     * 批量插入用户角色关系
     */
    void batchInsertUserRoles(@Param("list") List<UserRole> list);
}

// 2. 对应的 XML
<insert id="batchInsertUserRoles">
    INSERT INTO sys_user_role (user_id, role_id) VALUES
    <foreach collection="list" item="item" separator=",">
        (#{item.userId}, #{item.roleId})
    </foreach>
</insert>

// 3. 修改 UserService.assignRoles 方法
@CacheEvict(value = "user-roles", key = "#userId", allEntries = true)
@Transactional
public void assignRoles(Long userId, List<Long> roleIds) {
    User user = getById(userId);
    if (user == null) {
        throw new BusinessException(404, "用户不存在");
    }

    List<UserRole> existingUserRoles = userRoleMapper.selectList(
        new LambdaQueryWrapper<UserRole>()
            .eq(UserRole::getUserId, userId)
    );

    List<Long> existingRoleIds = existingUserRoles.stream()
        .map(UserRole::getRoleId)
        .collect(Collectors.toList());

    if (roleIds == null || roleIds.isEmpty()) {
        if (!existingRoleIds.isEmpty()) {
            userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, userId)
            );
        }
        return;
    }

    // 计算需要删除和新增的角色
    List<Long> toRemove = existingRoleIds.stream()
        .filter(id -> !roleIds.contains(id))
        .collect(Collectors.toList());

    List<Long> toAdd = roleIds.stream()
        .filter(id -> !existingRoleIds.contains(id))
        .collect(Collectors.toList());

    // 批量删除
    if (!toRemove.isEmpty()) {
        userRoleMapper.delete(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .in(UserRole::getRoleId, toRemove)
        );
    }

    // ✅ 批量插入（优化点）
    if (!toAdd.isEmpty()) {
        List<UserRole> userRoles = toAdd.stream()
            .map(roleId -> {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            })
            .collect(Collectors.toList());
        
        // 改为批量插入
        userRoleMapper.batchInsertUserRoles(userRoles);
    }
}
```

---

### 6.2 批量更新主角色（setPrimaryRole）

**位置**: `UserConfigServiceImpl.java`

```java
// 1. 在 UserRoleRelationMapper 中添加批量更新方法
@Mapper
public interface UserRoleRelationMapper extends BaseMapper<UserRoleRelation> {
    /**
     * 批量更新主角色
     * @param userId 用户ID
     * @param roleId 新的主角色ID
     */
    void updatePrimaryByUserId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}

// 2. 对应的 XML
<update id="updatePrimaryByUserId">
    UPDATE sys_user_role_relation
    SET is_primary = CASE 
        WHEN role_id = #{roleId} THEN '1' 
        ELSE '0' 
    END
    WHERE user_id = #{userId}
</update>

// 3. 修改 setPrimaryRole 方法
@Override
@Transactional
public void setPrimaryRole(Long userId, Long roleId) {
    // 校验角色是否存在
    Role role = roleMapper.selectById(roleId);
    if (role == null) {
        throw new BusinessException(404, "角色不存在");
    }
    
    // ✅ 使用单条 SQL 批量更新
    userRoleRelationMapper.updatePrimaryByUserId(userId, roleId);
}

// 4. 同样优化 setPrimaryOrg
@Override
@Transactional
public void setPrimaryOrg(Long userId, Long orgId) {
    OrgUnit org = orgUnitMapper.selectById(orgId);
    if (org == null) {
        throw new BusinessException(404, "组织不存在");
    }
    
    userOrgRelationMapper.updatePrimaryByUserId(userId, orgId);
}

// 5. UserOrgRelationMapper 也需要添加对应的方法
@Mapper
public interface UserOrgRelationMapper extends BaseMapper<UserOrgRelation> {
    void updatePrimaryByUserId(@Param("userId") Long userId, @Param("orgId") Long orgId);
}

// 6. 对应的 XML
<update id="updatePrimaryByUserId">
    UPDATE sys_user_org_relation
    SET is_primary = CASE 
        WHEN org_id = #{orgId} THEN '1' 
        ELSE '0' 
    END
    WHERE user_id = #{userId}
</update>
```

---

### 6.3 N+1 查询优化（getUserRoles）

**位置**: `UserConfigServiceImpl.java`

```java
@Override
public List<Map<String, Object>> getUserRoles(Long userId) {
    // 1. 获取用户角色关系列表（一次查询）
    List<UserRoleRelation> relations = userRoleRelationMapper.selectByUserId(userId);
    
    if (relations.isEmpty()) {
        return Collections.emptyList();
    }
    
    // 2. 批量获取角色信息（一次查询替代 N 次）
    Set<Long> roleIds = relations.stream()
        .map(UserRoleRelation::getRoleId)
        .collect(Collectors.toSet());
    List<Role> roles = roleMapper.selectBatchIds(roleIds);
    
    // 3. 构建 Map 快速查找
    Map<Long, Role> roleMap = roles.stream()
        .collect(Collectors.toMap(Role::getId, r -> r));
    
    // 4. 查找主角色 ID
    Long primaryRoleId = relations.stream()
        .filter(r -> "1".equals(r.getIsPrimary()))
        .findFirst()
        .map(UserRoleRelation::getRoleId)
        .orElse(null);
    
    // 5. 组装结果
    List<Map<String, Object>> result = new ArrayList<>();
    for (UserRoleRelation relation : relations) {
        Role role = roleMap.get(relation.getRoleId());
        if (role != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", role.getId());
            map.put("roleCode", role.getRoleCode());
            map.put("roleName", role.getRoleName());
            map.put("description", role.getDescription());
            map.put("isPrimary", primaryRoleId != null && primaryRoleId.equals(role.getId()) ? "1" : "0");
            result.add(map);
        }
    }
    
    return result;
}
```

**对应的 Mapper 方法**：
```java
// UserRoleRelationMapper.java
List<UserRoleRelation> selectByUserId(Long userId);

// UserRoleRelationMapper.xml
<select id="selectByUserId" resultType="UserRoleRelation">
    SELECT user_id, role_id, is_primary, create_time
    FROM sys_user_role_relation
    WHERE user_id = #{userId}
</select>
```

---

### 6.4 配置历史版本管理

**位置**: `ConfigHistory.java`

```java
// 1. 修改 ConfigHistory 实体添加 version 字段
@TableName("sys_user_config_history")
public class ConfigHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long configId;
    private Long userId;
    private String configKey;
    private String configValue;
    private String configType;
    private String operation;
    private String operator;
    private Integer version;  // ✅ 新增版本号
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // getter/setter...
}

// 2. 修改 ConfigHistoryServiceImpl
@Service
public class ConfigHistoryServiceImpl implements ConfigHistoryService {

    private final ConfigHistoryMapper configHistoryMapper;

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
        history.setVersion(getNextVersion(configId));  // ✅ 自动生成版本号
        configHistoryMapper.insert(history);
    }

    private Integer getNextVersion(Long configId) {
        Integer maxVersion = configHistoryMapper.selectMaxVersionByConfigId(configId);
        return maxVersion != null ? maxVersion + 1 : 1;
    }

    @Override
    public List<ConfigHistory> getConfigHistory(Long configId) {
        return configHistoryMapper.selectByConfigId(configId);
    }

    @Override
    public List<ConfigHistory> getConfigHistoryByUserId(Long userId) {
        return configHistoryMapper.selectByUserId(userId);
    }

    @Override
    public ConfigHistory getConfigHistoryByVersion(Long configId, Integer version) {
        return configHistoryMapper.selectByConfigIdAndVersion(configId, version);
    }

    @Override
    public List<ConfigHistory> getConfigHistoryByKey(Long userId, String configKey) {
        return configHistoryMapper.selectByUserIdAndConfigKey(userId, configKey);
    }
}

// 3. ConfigHistoryMapper.java
@Mapper
public interface ConfigHistoryMapper extends BaseMapper<ConfigHistory> {
    
    List<ConfigHistory> selectByConfigId(Long configId);
    
    List<ConfigHistory> selectByUserId(Long userId);
    
    ConfigHistory selectByConfigIdAndVersion(@Param("configId") Long configId, 
                                             @Param("version") Integer version);
    
    List<ConfigHistory> selectByUserIdAndConfigKey(@Param("userId") Long userId,
                                                   @Param("configKey") String configKey);
    
    Integer selectMaxVersionByConfigId(@Param("configId") Long configId);
}

// 4. ConfigHistoryMapper.xml
<select id="selectByConfigId" resultType="ConfigHistory">
    SELECT * FROM sys_user_config_history 
    WHERE config_id = #{configId} 
    ORDER BY version DESC
</select>

<select id="selectMaxVersionByConfigId" resultType="Integer">
    SELECT MAX(version) FROM sys_user_config_history 
    WHERE config_id = #{configId}
</select>

<select id="selectByConfigIdAndVersion" resultType="ConfigHistory">
    SELECT * FROM sys_user_config_history 
    WHERE config_id = #{configId} AND version = #{version}
</select>
```

---

### 6.5 配置加密功能

**位置**: `UserConfigServiceImpl.java`

```java
@Service
public class UserConfigServiceImpl implements UserConfigService {

    // 敏感配置键前缀
    private static final List<String> SENSITIVE_PREFIXES = Arrays.asList(
        "password", "apiKey", "secret", "token", "credential", "key", "auth"
    );

    private final StringEncryptor stringEncryptor;  // 注入 Jasypt 加密器

    /**
     * 判断配置是否为敏感配置
     */
    private boolean isSensitive(String configKey) {
        if (configKey == null) return false;
        String lowerKey = configKey.toLowerCase();
        return SENSITIVE_PREFIXES.stream()
            .anyMatch(prefix -> lowerKey.contains(prefix));
    }

    /**
     * 加密配置值
     */
    private String encryptValue(String value) {
        if (value == null) return null;
        return stringEncryptor.encrypt(value);
    }

    /**
     * 解密配置值
     */
    private String decryptValue(String encryptedValue) {
        if (encryptedValue == null) return null;
        try {
            return stringEncryptor.decrypt(encryptedValue);
        } catch (Exception e) {
            // 如果解密失败，说明可能是明文，直接返回
            return encryptedValue;
        }
    }

    @Override
    @Cacheable(value = "user-config", key = "#userId + ':' + #configKey")
    public UserConfig getUserConfigByKey(Long userId, String configKey) {
        UserConfig config = userConfigMapper.selectByUserIdAndKey(userId, configKey);
        
        if (config != null && isSensitive(configKey)) {
            // ✅ 解密敏感配置
            config.setConfigValue(decryptValue(config.getConfigValue()));
        }
        
        return config;
    }

    @Override
    @CacheEvict(value = "user-config", key = "#config.userId", allEntries = true)
    @Transactional
    public UserConfig createUserConfig(UserConfig config) {
        validateConfig(config.getConfigKey(), config.getConfigValue());
        
        UserConfig existing = userConfigMapper.selectByUserIdAndKey(
            config.getUserId(), config.getConfigKey());
        if (existing != null) {
            throw new BusinessException(400, "配置键已存在");
        }
        
        // ✅ 加密敏感配置
        if (isSensitive(config.getConfigKey())) {
            config.setConfigValue(encryptValue(config.getConfigValue()));
        }
        
        userConfigMapper.insert(config);
        
        // 保存历史
        configHistoryService.saveConfigHistory(
            config.getId(), config.getUserId(), config.getConfigKey(),
            config.getConfigValue(), config.getConfigType(), "CREATE", null);
        
        // 发布变更事件
        eventPublisher.publishEvent(new ConfigChangeEvent(this, config.getConfigKey()));
        configChangePublisher.publishConfigChange(config.getUserId(), config.getConfigKey(), "CREATE");
        
        return config;
    }

    @Override
    @CacheEvict(value = "user-config", key = "#userId", allEntries = true)
    @Transactional
    public UserConfig updateUserConfig(Long id, UserConfig config) {
        validateConfig(config.getConfigKey(), config.getConfigValue());
        
        UserConfig existing = userConfigMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "配置不存在");
        }

        if (!existing.getConfigKey().equals(config.getConfigKey())) {
            UserConfig duplicate = userConfigMapper.selectByUserIdAndKey(
                existing.getUserId(), config.getConfigKey());
            if (duplicate != null) {
                throw new BusinessException(400, "配置键已存在");
            }
        }

        config.setId(id);
        config.setUpdateTime(LocalDateTime.now());
        
        // ✅ 加密敏感配置
        if (isSensitive(config.getConfigKey())) {
            config.setConfigValue(encryptValue(config.getConfigValue()));
        }
        
        userConfigMapper.updateById(config);
        
        // 保存历史
        configHistoryService.saveConfigHistory(
            config.getId(), config.getUserId(), config.getConfigKey(),
            config.getConfigValue(), config.getConfigType(), "UPDATE", null);
        
        // 发布变更事件
        eventPublisher.publishEvent(new ConfigChangeEvent(this, config.getConfigKey()));
        configChangePublisher.publishConfigChange(config.getUserId(), config.getConfigKey(), "UPDATE");
        
        return config;
    }
}
```

---

## 七、总结与建议

### 7.1 项目进步评价

从 V2 到 V3，项目在短短几个小时内完成了大量优化工作：

| 指标 | V2 | V3 | 进步 |
|------|----|----|------|
| P0 问题完成率 | 0/2 | 2/2 | 100% ✅ |
| P1 问题完成率 | 0/5 | 6/6 | 100% ✅ |
| P2 问题完成率 | 0/3 | 0/3 | 0% |
| 总完成率 | 0% | 73% | ⬆️ +73% |

### 7.2 剩余优化项优先级

| 优先级 | 问题 | 预计工时 |
|--------|------|---------|
| P0-1 | `assignRoles` 批量插入 | 1 小时 |
| P0-2 | `setPrimaryRole/Org` 批量更新 | 1 小时 |
| P1-1 | `getUserRoles/Orgs` N+1 查询优化 | 2 小时 |
| P1-2 | ConfigHistory 添加 version 字段 | 3 小时 |
| P1-3 | 配置加密功能 | 4 小时 |
| P2-1 | 配置导入导出 | 6 小时 |

### 7.3 最终建议

1. **短期（1-2 天）**：修复 P0 问题，实现批量操作优化
2. **中期（1 周）**：实现配置版本管理和加密功能
3. **长期（2 周）**：实现配置导入导出和审计功能

---

**报告生成时间**: 2026-05-21 17:00:00  
**分析者**: 代可行 (AI Agent)  
**项目版本**: V3