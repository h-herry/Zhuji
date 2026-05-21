# h-herry/Zhuji 项目优化代码 V3

**生成日期**: 2026-05-21  
**对应报告**: [源码深度分析报告 V3](./source-code-analysis-v3.md)

---

## 目录

1. [问题 1: UserService.assignRoles 批量插入](#问题1-userserviceassignroles-批量插入)
2. [问题 2: setPrimaryRole/Org 批量更新](#问题2-setprimaryroleorg-批量更新)
3. [问题 3: getUserRoles/Orgs N+1 查询优化](#问题3-getuserrolesorgs-n1-查询优化)
4. [问题 4: ConfigHistory 版本管理增强](#问题4-confighistory-版本管理增强)
5. [问题 5: 配置加密功能](#问题5-配置加密功能)

---

## 问题 1: UserService.assignRoles 批量插入

### 1.1 UserRoleMapper.java

**位置**: `user-org-service/src/main/java/com/zhuji/userorg/mapper/UserRoleMapper.java`

```java
package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    /**
     * 批量插入用户角色关系
     * @param list 用户角色关系列表
     */
    void batchInsertUserRoles(@Param("list") List<UserRole> list);
}
```

### 1.2 UserRoleMapper.xml

**位置**: `user-org-service/src/main/resources/mapper/UserRoleMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.zhuji.userorg.mapper.UserRoleMapper">
    
    <!-- 批量插入用户角色关系 -->
    <insert id="batchInsertUserRoles">
        INSERT INTO sys_user_role (user_id, role_id)
        VALUES
        <foreach collection="list" item="item" separator=",">
            (#{item.userId}, #{item.roleId})
        </foreach>
    </insert>
    
</mapper>
```

### 1.3 UserService.assignRoles 优化

**位置**: `UserService.java`

```java
@CacheEvict(value = "user-roles", key = "#userId", allEntries = true)
@Transactional
public void assignRoles(Long userId, List<Long> roleIds) {
    User user = getById(userId);
    if (user == null) {
        throw new BusinessException(404, I18nMessageUtil.getMessage("user.not.found"));
    }

    // 1. 获取已有的用户角色列表
    List<UserRole> existingUserRoles = userRoleMapper.selectList(
        new LambdaQueryWrapper<UserRole>()
            .eq(UserRole::getUserId, userId)
    );

    List<Long> existingRoleIds = existingUserRoles.stream()
        .map(UserRole::getRoleId)
        .collect(Collectors.toList());

    // 2. 处理空列表（清除所有角色）
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
    List<Long> toRemove = existingRoleIds.stream()
        .filter(id -> !roleIds.contains(id))
        .collect(Collectors.toList());

    // 4. 计算需要新增的角色
    List<Long> toAdd = roleIds.stream()
        .filter(id -> !existingRoleIds.contains(id))
        .collect(Collectors.toList());

    // 5. 批量删除
    if (!toRemove.isEmpty()) {
        userRoleMapper.delete(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .in(UserRole::getRoleId, toRemove)
        );
    }

    // 6. 批量插入（优化点）
    if (!toAdd.isEmpty()) {
        List<UserRole> userRoles = toAdd.stream()
            .map(roleId -> {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            })
            .collect(Collectors.toList());
        
        // ✅ 使用批量插入方法
        userRoleMapper.batchInsertUserRoles(userRoles);
    }
}
```

---

## 问题 2: setPrimaryRole/Org 批量更新

### 2.1 UserRoleRelationMapper.java

**位置**: `user-org-service/src/main/java/com/zhuji/userorg/mapper/UserRoleRelationMapper.java`

```java
package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.UserRoleRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleRelationMapper extends BaseMapper<UserRoleRelation> {
    
    /**
     * 根据用户ID查询所有角色关系
     */
    List<UserRoleRelation> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 批量更新主角色（使用单条 SQL）
     * @param userId 用户ID
     * @param roleId 新的主角色ID
     */
    void updatePrimaryByUserId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
```

### 2.2 UserRoleRelationMapper.xml

**位置**: `user-org-service/src/main/resources/mapper/UserRoleRelationMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.zhuji.userorg.mapper.UserRoleRelationMapper">
    
    <!-- 根据用户ID查询所有角色关系 -->
    <select id="selectByUserId" resultType="UserRoleRelation">
        SELECT id, user_id, role_id, is_primary, create_time
        FROM sys_user_role_relation
        WHERE user_id = #{userId}
    </select>
    
    <!-- 批量更新主角色（单条 SQL） -->
    <update id="updatePrimaryByUserId">
        UPDATE sys_user_role_relation
        SET is_primary = CASE 
            WHEN role_id = #{roleId} THEN '1' 
            ELSE '0' 
        END
        WHERE user_id = #{userId}
    </update>
    
</mapper>
```

### 2.3 UserOrgRelationMapper.java

**位置**: `user-org-service/src/main/java/com/zhuji/userorg/mapper/UserOrgRelationMapper.java`

```java
package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.UserOrgRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserOrgRelationMapper extends BaseMapper<UserOrgRelation> {
    
    /**
     * 根据用户ID查询所有组织关系
     */
    List<UserOrgRelation> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 批量更新主组织（使用单条 SQL）
     * @param userId 用户ID
     * @param orgId 新的主组织ID
     */
    void updatePrimaryByUserId(@Param("userId") Long userId, @Param("orgId") Long orgId);
    
    /**
     * 批量插入用户组织关系
     */
    void batchInsert(@Param("list") List<UserOrgRelation> list);
}
```

### 2.4 UserOrgRelationMapper.xml

**位置**: `user-org-service/src/main/resources/mapper/UserOrgRelationMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.zhuji.userorg.mapper.UserOrgRelationMapper">
    
    <!-- 根据用户ID查询所有组织关系 -->
    <select id="selectByUserId" resultType="UserOrgRelation">
        SELECT id, user_id, org_id, relation_type, is_primary, create_time
        FROM sys_user_org_relation
        WHERE user_id = #{userId}
    </select>
    
    <!-- 批量更新主组织（单条 SQL） -->
    <update id="updatePrimaryByUserId">
        UPDATE sys_user_org_relation
        SET is_primary = CASE 
            WHEN org_id = #{orgId} THEN '1' 
            ELSE '0' 
        END
        WHERE user_id = #{userId}
    </update>
    
    <!-- 批量插入用户组织关系 -->
    <insert id="batchInsert">
        INSERT INTO sys_user_org_relation (user_id, org_id, relation_type, is_primary, create_time)
        VALUES
        <foreach collection="list" item="item" separator=",">
            (#{item.userId}, #{item.orgId}, #{item.relationType}, #{item.isPrimary}, #{item.createTime})
        </foreach>
    </insert>
    
</mapper>
```

### 2.5 UserConfigServiceImpl 优化

**位置**: `UserConfigServiceImpl.java`

```java
@Override
@Transactional
public void setPrimaryRole(Long userId, Long roleId) {
    // 1. 校验角色是否存在
    Role role = roleMapper.selectById(roleId);
    if (role == null) {
        throw new BusinessException(404, I18nMessageUtil.getMessage("role.not.found"));
    }
    
    // 2. 校验用户是否拥有该角色
    List<UserRoleRelation> relations = userRoleRelationMapper.selectByUserId(userId);
    boolean hasRole = relations.stream()
        .anyMatch(r -> r.getRoleId().equals(roleId));
    
    if (!hasRole) {
        throw new BusinessException(400, "用户没有该角色");
    }
    
    // 3. ✅ 使用单条 SQL 批量更新
    userRoleRelationMapper.updatePrimaryByUserId(userId, roleId);
}

@Override
@Transactional
public void setPrimaryOrg(Long userId, Long orgId) {
    // 1. 校验组织是否存在
    OrgUnit org = orgUnitMapper.selectById(orgId);
    if (org == null) {
        throw new BusinessException(404, I18nMessageUtil.getMessage("org.not.found"));
    }
    
    // 2. 校验用户是否属于该组织
    List<UserOrgRelation> relations = userOrgRelationMapper.selectByUserId(userId);
    boolean hasOrg = relations.stream()
        .anyMatch(r -> r.getOrgId().equals(orgId));
    
    if (!hasOrg) {
        throw new BusinessException(400, "用户不属于该组织");
    }
    
    // 3. ✅ 使用单条 SQL 批量更新
    userOrgRelationMapper.updatePrimaryByUserId(userId, orgId);
}
```

---

## 问题 3: getUserRoles/Orgs N+1 查询优化

### 3.1 UserConfigServiceImpl 优化

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

@Override
public List<Map<String, Object>> getUserOrgs(Long userId) {
    // 1. 获取用户组织关系列表（一次查询）
    List<UserOrgRelation> relations = userOrgRelationMapper.selectByUserId(userId);
    
    if (relations.isEmpty()) {
        return Collections.emptyList();
    }
    
    // 2. 批量获取组织信息（一次查询替代 N 次）
    Set<Long> orgIds = relations.stream()
        .map(UserOrgRelation::getOrgId)
        .collect(Collectors.toSet());
    List<OrgUnit> orgs = orgUnitMapper.selectBatchIds(orgIds);
    
    // 3. 构建 Map 快速查找
    Map<Long, OrgUnit> orgMap = orgs.stream()
        .collect(Collectors.toMap(OrgUnit::getId, o -> o));
    
    // 4. 查找主组织 ID
    Long primaryOrgId = relations.stream()
        .filter(r -> "1".equals(r.getIsPrimary()))
        .findFirst()
        .map(UserOrgRelation::getOrgId)
        .orElse(null);
    
    // 5. 组装结果
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
            map.put("isPrimary", primaryOrgId != null && primaryOrgId.equals(org.getId()) ? "1" : "0");
            result.add(map);
        }
    }
    
    return result;
}
```

---

## 问题 4: ConfigHistory 版本管理增强

### 4.1 ConfigHistory.java 修改

**位置**: `ConfigHistory.java`

```java
package com.zhuji.userorg.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

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
    private Integer version;  // ✅ 新增版本号字段
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    
    public String getConfigType() { return configType; }
    public void setConfigType(String configType) { this.configType = configType; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
```

### 4.2 ConfigHistoryMapper.java

**位置**: `ConfigHistoryMapper.java`

```java
package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.ConfigHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConfigHistoryMapper extends BaseMapper<ConfigHistory> {
    
    /**
     * 根据配置ID查询历史（按版本降序）
     */
    List<ConfigHistory> selectByConfigId(@Param("configId") Long configId);
    
    /**
     * 根据用户ID查询配置历史
     */
    List<ConfigHistory> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据配置ID和版本查询历史
     */
    ConfigHistory selectByConfigIdAndVersion(@Param("configId") Long configId, 
                                              @Param("version") Integer version);
    
    /**
     * 根据用户ID和配置键查询历史
     */
    List<ConfigHistory> selectByUserIdAndConfigKey(@Param("userId") Long userId,
                                                    @Param("configKey") String configKey);
    
    /**
     * 获取配置的下一个版本号
     */
    Integer selectMaxVersionByConfigId(@Param("configId") Long configId);
}
```

### 4.3 ConfigHistoryMapper.xml

**位置**: `ConfigHistoryMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.zhuji.userorg.mapper.ConfigHistoryMapper">
    
    <!-- 根据配置ID查询历史（按版本降序） -->
    <select id="selectByConfigId" resultType="ConfigHistory">
        SELECT * FROM sys_user_config_history 
        WHERE config_id = #{configId} 
        ORDER BY version DESC
    </select>
    
    <!-- 根据用户ID查询配置历史 -->
    <select id="selectByUserId" resultType="ConfigHistory">
        SELECT * FROM sys_user_config_history 
        WHERE user_id = #{userId} 
        ORDER BY create_time DESC
    </select>
    
    <!-- 根据配置ID和版本查询历史 -->
    <select id="selectByConfigIdAndVersion" resultType="ConfigHistory">
        SELECT * FROM sys_user_config_history 
        WHERE config_id = #{configId} AND version = #{version}
    </select>
    
    <!-- 根据用户ID和配置键查询历史 -->
    <select id="selectByUserIdAndConfigKey" resultType="ConfigHistory">
        SELECT * FROM sys_user_config_history 
        WHERE user_id = #{userId} AND config_key = #{configKey} 
        ORDER BY version DESC
    </select>
    
    <!-- 获取配置的下一个版本号 -->
    <select id="selectMaxVersionByConfigId" resultType="Integer">
        SELECT MAX(version) FROM sys_user_config_history 
        WHERE config_id = #{configId}
    </select>
    
</mapper>
```

### 4.4 ConfigHistoryService.java

**位置**: `ConfigHistoryService.java`

```java
package com.zhuji.userorg.service;

import com.zhuji.userorg.entity.ConfigHistory;

import java.util.List;

public interface ConfigHistoryService {
    
    /**
     * 保存配置历史（自动生成版本号）
     */
    void saveConfigHistory(Long configId, Long userId, String configKey, 
                          String configValue, String configType, 
                          String operation, String operator);
    
    /**
     * 获取配置的历史版本列表
     */
    List<ConfigHistory> getConfigHistory(Long configId);
    
    /**
     * 根据用户ID获取配置历史
     */
    List<ConfigHistory> getConfigHistoryByUserId(Long userId);
    
    /**
     * 获取指定版本的配置历史
     */
    ConfigHistory getConfigHistoryByVersion(Long configId, Integer version);
    
    /**
     * 根据用户ID和配置键获取配置历史
     */
    List<ConfigHistory> getConfigHistoryByKey(Long userId, String configKey);
    
    /**
     * 回滚配置到指定版本
     */
    Object rollbackToVersion(Long configId, Integer version);
}
```

### 4.5 ConfigHistoryServiceImpl.java

**位置**: `ConfigHistoryServiceImpl.java`

```java
package com.zhuji.userorg.service.impl;

import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.entity.ConfigHistory;
import com.zhuji.userorg.entity.UserConfig;
import com.zhuji.userorg.mapper.ConfigHistoryMapper;
import com.zhuji.userorg.mapper.UserConfigMapper;
import com.zhuji.userorg.service.ConfigHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfigHistoryServiceImpl implements ConfigHistoryService {

    private final ConfigHistoryMapper configHistoryMapper;
    private final UserConfigMapper userConfigMapper;

    public ConfigHistoryServiceImpl(ConfigHistoryMapper configHistoryMapper,
                                    UserConfigMapper userConfigMapper) {
        this.configHistoryMapper = configHistoryMapper;
        this.userConfigMapper = userConfigMapper;
    }

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

    @Override
    @Transactional
    public Object rollbackToVersion(Long configId, Integer version) {
        // 1. 获取目标版本的历史
        ConfigHistory targetVersion = configHistoryMapper.selectByConfigIdAndVersion(configId, version);
        if (targetVersion == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("config.version.not.found"));
        }

        // 2. 获取当前配置
        UserConfig config = userConfigMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("user.config.not.found"));
        }

        // 3. 保存当前版本到历史
        saveConfigHistory(config.getId(), config.getUserId(), config.getConfigKey(),
                          config.getConfigValue(), config.getConfigType(),
                          "ROLLBACK", null);

        // 4. 更新配置为历史版本的值
        config.setConfigValue(targetVersion.getConfigValue());
        config.setUpdateTime(java.time.LocalDateTime.now());
        userConfigMapper.updateById(config);

        // 5. 保存回滚操作到历史
        saveConfigHistory(config.getId(), config.getUserId(), config.getConfigKey(),
                          config.getConfigValue(), config.getConfigType(),
                          "ROLLBACK", null);

        return config;
    }
}
```

---

## 问题 5: 配置加密功能

### 5.1 配置加密服务

**位置**: `ConfigEncryptionService.java`

```java
package com.zhuji.userorg.service;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ConfigEncryptionService {

    private final StringEncryptor stringEncryptor;
    
    // 敏感配置键前缀
    private static final List<String> SENSITIVE_PREFIXES = Arrays.asList(
        "password", "apiKey", "secret", "token", "credential", "key", "auth"
    );

    public ConfigEncryptionService(StringEncryptor stringEncryptor) {
        this.stringEncryptor = stringEncryptor;
    }

    /**
     * 判断配置是否为敏感配置
     */
    public boolean isSensitive(String configKey) {
        if (configKey == null) return false;
        String lowerKey = configKey.toLowerCase();
        return SENSITIVE_PREFIXES.stream()
            .anyMatch(prefix -> lowerKey.contains(prefix));
    }

    /**
     * 加密配置值
     */
    public String encrypt(String configKey, String configValue) {
        if (!isSensitive(configKey) || configValue == null) {
            return configValue;
        }
        return stringEncryptor.encrypt(configValue);
    }

    /**
     * 解密配置值
     */
    public String decrypt(String configKey, String configValue) {
        if (!isSensitive(configKey) || configValue == null) {
            return configValue;
        }
        try {
            return stringEncryptor.decrypt(configValue);
        } catch (Exception e) {
            // 如果解密失败，说明可能是明文，直接返回
            return configValue;
        }
    }
}
```

### 5.2 修改 UserConfigServiceImpl

**位置**: `UserConfigServiceImpl.java`

```java
@Service
public class UserConfigServiceImpl implements UserConfigService {

    private final UserConfigMapper userConfigMapper;
    private final ConfigEncryptionService encryptionService;
    
    // 敏感配置键前缀
    private static final List<String> SENSITIVE_PREFIXES = Arrays.asList(
        "password", "apiKey", "secret", "token", "credential", "key", "auth"
    );

    public UserConfigServiceImpl(UserConfigMapper userConfigMapper,
                                 ConfigEncryptionService encryptionService) {
        this.userConfigMapper = userConfigMapper;
        this.encryptionService = encryptionService;
    }

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
        return encryptionService.encrypt(null, value);
    }

    /**
     * 解密配置值
     */
    private String decryptValue(String encryptedValue) {
        if (encryptedValue == null) return null;
        return encryptionService.decrypt(null, encryptedValue);
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
        config.setUpdateTime(java.time.LocalDateTime.now());
        
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
    
    // ... 其他方法
}
```

### 5.3 Jasypt 配置

**位置**: `application.yml`

```yaml
# Jasypt 加密配置
jasypt:
  encryptor:
    algorithm: AES
    password: ${JASYPT_ENCRYPTOR_PASSWORD:ZhujiDefaultEncryptorPassword}
    iv-generator-classname: org.jasypt.iv.RandomIvGenerator
```

---

## 八、相关配置文件

### 8.1 pom.xml 依赖

```xml
<!-- Jasypt 加密 -->
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

### 8.2 环境变量配置

```bash
# 设置加密密钥（生产环境必须修改）
export JASYPT_ENCRYPTOR_PASSWORD=your-secret-password
```

---

**文件生成时间**: 2026-05-21 17:10:00  
**对应报告**: [源码深度分析报告 V3](./source-code-analysis-v3.md)