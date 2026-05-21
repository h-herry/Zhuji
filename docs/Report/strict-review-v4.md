# 筑基项目源码严谨复审 V4 - 真实问题清单

**复审日期**: 2026-05-21  
**复审依据**: 逐文件阅读全部核心源码（共 20+ 文件）  
**项目版本**: commit 6659144

---

## 🔴 严重 Bug（必须修复，否则运行时崩溃）

### Bug 1: V1.0.1 创建 sys_user_config_history 表缺少列

**文件**: `V1.0.1__add_user_security_fields.sql`

**问题**: V1.0.1 创建表时只有 `config_key` 列，但 V1.0.2 和 ConfigHistoryMapper.xml 都引用了 `config_id`、`user_id`、`config_type` 列。

**V1.0.1 实际创建的表**:
```sql
CREATE TABLE IF NOT EXISTS sys_user_config_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    operation VARCHAR(20) NOT NULL,
    operator VARCHAR(64),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key),
    INDEX idx_create_time (create_time)
)
```

**V1.0.2 引用的不存在的列**:
```sql
ALTER TABLE sys_user_config_history
ADD COLUMN version INT DEFAULT 1 COMMENT '版本号';

CREATE INDEX idx_config_version ON sys_user_config_history(config_id, version);  -- ❌ config_id 不存在
```

**ConfigHistoryMapper.xml 引用的不存在的列**:
```xml
<select id="selectByConfigId">
    WHERE config_id = #{configId}   -- ❌ 列不存在
    WHERE user_id = #{userId}      -- ❌ 列不存在
    WHERE config_type = #{configType} -- ❌ 列不存在
</select>
```

**ConfigHistoryServiceImpl.java 会崩溃**:
```java
history.setConfigId(configId);    // ❌ 无此列，SQL 报错
history.setUserId(userId);      // ❌ 无此列，SQL 报错
history.setVersion(...);        // V1.0.2 ADD COLUMN 后存在，但初始版本没有
```

**正确方案**: V1.0.1 创建表时应该包含所有列：
```sql
CREATE TABLE IF NOT EXISTS sys_user_config_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_id BIGINT NOT NULL,           -- 缺失
    user_id BIGINT NOT NULL,             -- 缺失
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50),             -- 缺失
    operation VARCHAR(20) NOT NULL,
    operator VARCHAR(64),
    version INT DEFAULT 1,               -- 缺失
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key),
    INDEX idx_config_id (config_id),
    INDEX idx_user_id (user_id),
    INDEX idx_config_version (config_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### Bug 2: sys_user_password_history 表列名与实体不匹配

**文件**: `V1.0.1__add_user_security_fields.sql`

**V1.0.1 创建的表**:
```sql
CREATE TABLE sys_user_password_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- 列名是 password_hash
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
)
```

**UserPasswordHistory.java 实体**:
```java
private String password;  // 字段名是 password，不是 password_hash
```

MyBatis-Plus 会将驼峰 `password` 映射到下划线 `password`，但 SQL 列名是 `password_hash`，映射失败。

**正确方案**:
```sql
ALTER TABLE sys_user_password_history 
CHANGE COLUMN password_hash password VARCHAR(255) NOT NULL COMMENT '密码哈希';
```

---

## 🟡 安全性问题

### Issue 3: AES 使用 ECB 模式（不推荐）

**文件**: `ConfigEncryptionService.java`

```java
Cipher cipher = Cipher.getInstance("AES");  // 等同于 AES/ECB/PKCS5Padding
```

**问题**: ECB 模式相同明文块产生相同密文块，会泄露数据模式。加密图片或重复内容时尤为明显。

**建议改为 CBC/GCM 模式**:
```java
// CBC 模式（推荐）
Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
IvParameterSpec iv = new IvParameterSpec(encryptionKey.substring(0, 16).getBytes());
cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), iv);
```

---

### Issue 4: 加密密钥硬编码默认值

```java
@Value("${config.encryption.key:zhuji-default-encryption-key-16}")
```

**问题**: 如果生产环境配置缺失，使用可预测的默认值，存在安全风险。

**建议**: 强制要求配置，不提供默认值：
```java
@Value("${config.encryption.key}")
private String encryptionKey;  // 无默认值，必须配置
```

---

## 🟠 业务逻辑缺陷

### Issue 5: 配置回滚未正确处理加密状态

**文件**: `ConfigHistoryServiceImpl.java`

```java
public Object rollbackToVersion(Long configId, Integer version) {
    // 获取目标版本历史（可能是加密的）
    ConfigHistory targetVersion = configHistoryMapper.selectByConfigIdAndVersion(configId, version);
    
    // 当前配置（如果是加密的，值已经是密文）
    UserConfig config = userConfigMapper.selectById(configId);
    
    // 保存当前值到历史（当前配置值是密文 → 历史中存密文）
    saveConfigHistory(config.getId(), config.getUserId(), ...);
    
    // 直接恢复目标版本的值
    config.setConfigValue(targetVersion.getConfigValue());  // ⚠️ 可能仍是加密字符串
    userConfigMapper.updateById(config);  // 写入数据库的是密文
}
```

**场景**: 当前 `theme=light` → 用户改加密配置 `apiKey=abc123` → 回滚到 `apiKey=abc123`（数据库中已是加密字符串）→ 读取时解密得到明文。但如果 API 在某处直接读取数据库（绕过解密逻辑），会得到密文。

**建议**: 历史中存储解密后的原始值，或在回滚时判断加密状态。

---

### Issue 6: 刷新 Token 后未重新生成 refreshToken

**文件**: `UserService.java`

```java
public LoginResponse refreshToken(String refreshToken) {
    // ... 验证逻辑 ...
    
    // 只更新了 accessToken
    String newAccessToken = jwtService.generateAccessToken(...);
    redisTemplate.opsForValue().set(
        TOKEN_PREFIX + user.getId(),   // ✅ accessToken 刷新了
        newAccessToken,
        jwtService.getAccessTokenExpiration(),
        TimeUnit.SECONDS
    );
    // ⚠️ refreshToken 没有重新写入 Redis
    // ⚠️ 旧的 refreshToken 仍然有效
    
    return LoginResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(refreshToken)  // 返回的是旧 refreshToken
        .build();
}
```

**问题**: refreshToken 泄露后，攻击者可以一直用它刷新新的 accessToken，直到 refreshToken 本身过期。

**建议**: 刷新 accessToken 时同时刷新 refreshToken，并使旧 refreshToken 失效。

---

### Issue 7: batchAssignRoles 中存在 N+1 查询

**文件**: `UserConfigServiceImpl.java`

```java
for (int i = 0; i < rolesToAdd.size(); i++) {
    Long roleId = rolesToAdd.get(i);
    Role role = roleMapper.selectById(roleId);  // ❌ N 次 SELECT
    if (role == null) {
        throw new BusinessException(404, ...);
    }
    // ...
}
```

**建议**: 批量查询验证所有角色：
```java
List<Role> roles = roleMapper.selectBatchIds(rolesToAdd);
if (roles.size() != rolesToAdd.size()) {
    // 找出不存在的角色
}
```

---

### Issue 8: login 每次都查数据库加载权限

**文件**: `UserService.java`

```java
public LoginResponse login(LoginRequest request) {
    // ...
    List<Permission> permissions = permissionService.listByUserId(user.getId());  // 每次登录都查 DB
    // ...
}
```

**问题**: 登录操作本身已有密码验证的 DB 查询，再加上权限查询，性能损耗明显。

**建议**: 权限缓存复用，或将登录时的权限查询纳入事务中合并。

---

### Issue 9: incrementFailCount 存在竞态条件

**文件**: `UserLockService.java`

```java
public void incrementFailCount(Long userId) {
    String failKey = FAIL_COUNT_PREFIX + userId;
    Object current = redisTemplate.opsForValue().get(failKey);  // ❌ read
    int count = (current != null) ? (int) current : 0;
    count++;                                                     // ❌ compute
    redisTemplate.opsForValue().set(failKey, count, ...);       // ❌ write
    // 非原子操作，高并发下可能漏计数
}
```

**建议**: 使用 Redis INCR：
```java
Long count = redisTemplate.opsForValue().increment(FAIL_COUNT_PREFIX + userId);
redisTemplate.expire(FAIL_COUNT_PREFIX + userId, 1, TimeUnit.HOURS);
if (count != null && count >= MAX_FAIL_COUNT) {
    lockUser(userId);
}
```

---

### Issue 10: UserLockService MAX_FAIL_COUNT 硬编码

```java
private static final int MAX_FAIL_COUNT = 5;  // ❌ 硬编码
```

如果需要调整次数，需要改代码并重新部署。应该从配置文件读取。

---

## 总体评价修正

| 维度 | 之前评分 | 实际评分 | 说明 |
|------|---------|---------|------|
| 功能完整性 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 功能齐全 |
| 代码质量 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 有严重 Bug |
| 安全加固 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | AES ECB + 密钥默认值 |
| 架构设计 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 架构清晰但有细节缺陷 |
| **生产就绪** | ✅ | **❌ 暂不可用** | **Bug 1 导致运行时崩溃** |

---

## 必须修复的清单

| 优先级 | 问题 | 修复工时 | 严重程度 |
|--------|------|---------|---------|
| **P0** | V1.0.1 表结构缺少列 | 10 分钟 | 运行时崩溃 |
| **P0** | password_history 列名不匹配 | 5 分钟 | 查询失败 |
| **P1** | 刷新 Token 未重置 refreshToken | 30 分钟 | 安全漏洞 |
| **P1** | AES 使用 ECB 模式 | 20 分钟 | 安全性 |
| **P2** | 配置回滚加密状态处理 | 2 小时 | 潜在数据损坏 |
| **P2** | batchAssignRoles N+1 | 30 分钟 | 性能 |
| **P2** | login 权限查询 | 1 小时 | 性能 |
| **P2** | incrementFailCount 竞态条件 | 30 分钟 | 高并发安全 |
| **P3** | MAX_FAIL_COUNT 硬编码 | 10 分钟 | 灵活性 |
| **P3** | 加密密钥无默认值 | 10 分钟 | 安全 |

---

**复审结论**: 项目整体架构设计良好，功能完整，但 V1.0.1 的数据库迁移脚本存在严重缺陷，导致配置历史功能完全无法使用。必须先修复 Bug 1 和 Bug 2 后才能部署到生产环境。