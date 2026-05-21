# 优化代码文件
## 用户管理与可配置化设计优化代码

**生成日期**: 2026-05-21  
**对应报告**: [源码深度分析报告 V2](./source-code-analysis-v2.md)

---

## 目录

1. [问题 1: 批量插入优化](#问题1-批量插入优化)
2. [问题 4: 密码过期策略](#问题4-密码过期策略)
3. [问题 5: 密码历史记录](#问题5-密码历史记录)
4. [问题 6: 双 Token 机制](#问题6-双token机制)
5. [问题 7: Token 黑名单](#问题7-token黑名单)
6. [问题 8: 跨实例配置同步](#问题8-跨实例配置同步)
7. [问题 9: 配置版本管理](#问题9-配置版本管理)
8. [问题 10: 配置加密](#问题10-配置加密)
9. [问题 11: 配置导入导出](#问题11-配置导入导出)

---

## 问题 1: 批量插入优化

### 优化后的 `batchAssignRoles` 方法

**位置**: `UserConfigServiceImpl.java`

```java
@Override
@Transactional
public void batchAssignRoles(Long userId, List<Long> roleIds, String isPrimary) {
    // 1. 校验用户是否存在
    if (userRoleRelationMapper.selectCount(
        new LambdaQueryWrapper<UserRoleRelation>()
            .eq(UserRoleRelation::getUserId, userId)
    ) == 0 && roleIds.isEmpty()) {
        return; // 用户没有角色且不分配新角色，直接返回
    }
    
    // 2. 查询已有的角色
    List<UserRoleRelation> existingRelations = userRoleRelationMapper.selectList(
        new LambdaQueryWrapper<UserRoleRelation>()
            .eq(UserRoleRelation::getUserId, userId)
    );
    
    Set<Long> existingRoleIds = existingRelations.stream()
        .map(UserRoleRelation::getRoleId)
        .collect(Collectors.toSet());
    
    // 3. 计算需要新增的角色
    List<Long> rolesToAdd = roleIds.stream()
        .filter(roleId -> !existingRoleIds.contains(roleId))
        .collect(Collectors.toList());
    
    // 4. 计算需要删除的角色
    List<Long> rolesToRemove = existingRoleIds.stream()
        .filter(roleId -> !roleIds.contains(roleId))
        .collect(Collectors.toList());
    
    // 5. 批量删除不需要的角色
    if (!rolesToRemove.isEmpty()) {
        userRoleRelationMapper.delete(
            new LambdaQueryWrapper<UserRoleRelation>()
                .eq(UserRoleRelation::getUserId, userId)
                .in(UserRoleRelation::getRoleId, rolesToRemove)
        );
    }
    
    // 6. 批量插入新增的角色
    if (!rolesToAdd.isEmpty()) {
        List<UserRoleRelation> newRelations = new ArrayList<>();
        for (int i = 0; i < rolesToAdd.size(); i++) {
            Long roleId = rolesToAdd.get(i);
            
            // 校验角色是否存在
            Role role = roleMapper.selectById(roleId);
            if (role == null) {
                throw new BusinessException(404, I18nMessageUtil.getMessage("role.not.found"));
            }
            
            UserRoleRelation relation = new UserRoleRelation();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            // 如果 isPrimary="1"，则第一个新增的角色设为主角色
            relation.setIsPrimary(("1".equals(isPrimary) && i == 0) ? "1" : "0");
            relation.setCreateTime(LocalDateTime.now());
            newRelations.add(relation);
        }
        
        // 批量插入
        userRoleRelationMapper.batchInsert(newRelations);
    }
}
```

### 批量插入 Mapper 方法

**位置**: `UserRoleRelationMapper.java`

```java
@Mapper
public interface UserRoleRelationMapper extends BaseMapper<UserRoleRelation> {
    
    /**
     * 批量插入用户角色关系
     * @param list 用户角色关系列表
     */
    void batchInsert(@Param("list") List<UserRoleRelation> list);
}
```

**位置**: `resources/mapper/UserRoleRelationMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.zhuji.userorg.mapper.UserRoleRelationMapper">
    
    <insert id="batchInsert">
        INSERT INTO sys_user_role_relation 
            (user_id, role_id, is_primary, create_time)
        VALUES
        <foreach collection="list" item="item" separator=",">
            (#{item.userId}, #{item.roleId}, #{item.isPrimary}, #{item.createTime})
        </foreach>
    </insert>
    
</mapper>
```

### 同样优化 `batchAssignOrgs` 方法

```java
@Override
@Transactional
public void batchAssignOrgs(Long userId, List<Long> orgIds, String isPrimary) {
    // 1. 查询已有的组织
    List<UserOrgRelation> existingRelations = userOrgRelationMapper.selectList(
        new LambdaQueryWrapper<UserOrgRelation>()
            .eq(UserOrgRelation::getUserId, userId)
    );
    
    Set<Long> existingOrgIds = existingRelations.stream()
        .map(UserOrgRelation::getOrgId)
        .collect(Collectors.toSet());
    
    // 2. 计算需要新增的组织
    List<Long> orgsToAdd = orgIds.stream()
        .filter(orgId -> !existingOrgIds.contains(orgId))
        .collect(Collectors.toList());
    
    // 3. 计算需要删除的组织
    List<Long> orgsToRemove = existingOrgIds.stream()
        .filter(orgId -> !orgIds.contains(orgId))
        .collect(Collectors.toList());
    
    // 4. 批量删除不需要的组织
    if (!orgsToRemove.isEmpty()) {
        userOrgRelationMapper.delete(
            new LambdaQueryWrapper<UserOrgRelation>()
                .eq(UserOrgRelation::getUserId, userId)
                .in(UserOrgRelation::getOrgId, orgsToRemove)
        );
    }
    
    // 5. 批量插入新增的组织
    if (!orgsToAdd.isEmpty()) {
        List<UserOrgRelation> newRelations = new ArrayList<>();
        for (int i = 0; i < orgsToAdd.size(); i++) {
            Long orgId = orgsToAdd.get(i);
            
            // 校验组织是否存在
            OrgUnit org = orgUnitMapper.selectById(orgId);
            if (org == null) {
                throw new BusinessException(404, I18nMessageUtil.getMessage("org.not.found"));
            }
            
            UserOrgRelation relation = new UserOrgRelation();
            relation.setUserId(userId);
            relation.setOrgId(orgId);
            relation.setRelationType("MEMBER");
            relation.setIsPrimary(("1".equals(isPrimary) && i == 0) ? "1" : "0");
            relation.setCreateTime(LocalDateTime.now());
            newRelations.add(relation);
        }
        
        // 批量插入
        userOrgRelationMapper.batchInsert(newRelations);
    }
}
```

---

## 问题 4: 密码过期策略

### 1. 扩展 `User` 实体

**位置**: `User.java`

```java
@TableName("sys_user")
public class User {
    // ... 已有字段
    
    /**
     * 密码更新时间
     */
    private LocalDateTime passwordUpdateTime;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    // ... getter/setter
}
```

### 2. 密码过期检查服务

**位置**: `PasswordExpiryService.java`

```java
package com.zhuji.userorg.security;

import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PasswordExpiryService {

    @Value("${security.password.expiry-days:90}")
    private int passwordExpiryDays;

    @Value("${security.password.expiry-warning-days:7}")
    private int passwordExpiryWarningDays;

    /**
     * 检查密码是否过期
     */
    public void checkPasswordExpiry(User user) {
        if (user.getPasswordUpdateTime() == null) {
            // 如果密码更新时间为空，说明是旧数据，不检查
            return;
        }

        long daysSinceUpdate = ChronoUnit.DAYS.between(
            user.getPasswordUpdateTime(), 
            LocalDateTime.now()
        );

        if (daysSinceUpdate > passwordExpiryDays) {
            throw new BusinessException(403, I18nMessageUtil.getMessage("user.password.expired"));
        }
    }

    /**
     * 检查密码是否即将过期
     * @return 剩余天数，如果不过期则返回 null
     */
    public Integer getPasswordExpiryWarning(User user) {
        if (user.getPasswordUpdateTime() == null) {
            return null;
        }

        long daysSinceUpdate = ChronoUnit.DAYS.between(
            user.getPasswordUpdateTime(), 
            LocalDateTime.now()
        );

        int remainingDays = (int) (passwordExpiryDays - daysSinceUpdate);

        if (remainingDays > 0 && remainingDays <= passwordExpiryWarningDays) {
            return remainingDays;
        }

        return null;
    }
}
```

### 3. 在登录时检查密码过期

**位置**: `UserService.java`

```java
public LoginResponse login(LoginRequest request) {
    User user = lambdaQuery()
            .eq(User::getUsername, request.getUsername())
            .one();

    if (user == null) {
        throw new BusinessException(401, I18nMessageUtil.getMessage("user.not.found"));
    }

    if (userLockService.isLocked(user.getId())) {
        throw new BusinessException(403, I18nMessageUtil.getMessage("user.locked"));
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        userLockService.incrementFailCount(user.getId());
        throw new BusinessException(401, I18nMessageUtil.getMessage("user.password.error"));
    }

    userLockService.resetFailCount(user.getId());

    if (user.getStatus() != 1) {
        throw new BusinessException(403, "用户已被禁用");
    }

    // ✅ 检查密码是否过期
    passwordExpiryService.checkPasswordExpiry(user);

    // 更新最后登录时间
    user.setLastLoginTime(LocalDateTime.now());
    updateById(user);

    // ... 生成 Token
    
    // ✅ 检查密码是否即将过期，返回警告信息
    Integer expiryWarning = passwordExpiryService.getPasswordExpiryWarning(user);
    
    LoginResponse response = LoginResponse.builder()
            .token(token)
            .userId(user.getId())
            .username(user.getUsername())
            .tokenType("Bearer")
            .expiresIn(TOKEN_EXPIRATION)
            .build();
    
    if (expiryWarning != null) {
        response.setPasswordExpiryWarning(
            I18nMessageUtil.getMessage("user.password.expiry.warning", expiryWarning)
        );
    }
    
    return response;
}
```

---

## 问题 5: 密码历史记录

### 1. 密码历史实体

**位置**: `UserPasswordHistory.java`

```java
package com.zhuji.userorg.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("sys_user_password_history")
public class UserPasswordHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String password;  // 加密后的密码
    private String passwordHash;  // 密码哈希值（用于快速比对）
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // ... getter/setter
}
```

### 2. 密码历史 Mapper

**位置**: `UserPasswordHistoryMapper.java`

```java
package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.UserPasswordHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPasswordHistoryMapper extends BaseMapper<UserPasswordHistory> {
    
    /**
     * 查询用户最近的密码历史
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 密码历史列表
     */
    List<UserPasswordHistory> selectRecentByUserId(
        @Param("userId") Long userId, 
        @Param("limit") int limit
    );
}
```

### 3. 密码历史服务

**位置**: `PasswordHistoryService.java`

```java
package com.zhuji.userorg.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.entity.UserPasswordHistory;
import com.zhuji.userorg.mapper.UserPasswordHistoryMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PasswordHistoryService {

    private final UserPasswordHistoryMapper passwordHistoryMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${security.password.history-count:5}")
    private int passwordHistoryCount;

    public PasswordHistoryService(
            UserPasswordHistoryMapper passwordHistoryMapper,
            PasswordEncoder passwordEncoder) {
        this.passwordHistoryMapper = passwordHistoryMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 验证密码是否在历史记录中
     */
    public void validatePasswordNotInHistory(Long userId, String newPassword) {
        List<UserPasswordHistory> historyList = passwordHistoryMapper.selectRecentByUserId(
            userId, passwordHistoryCount
        );

        for (UserPasswordHistory history : historyList) {
            if (passwordEncoder.matches(newPassword, history.getPassword())) {
                throw new BusinessException(400, 
                    I18nMessageUtil.getMessage("user.password.in.history", passwordHistoryCount));
            }
        }
    }

    /**
     * 保存密码历史
     */
    @Transactional
    public void savePasswordHistory(Long userId, String encodedPassword) {
        UserPasswordHistory history = new UserPasswordHistory();
        history.setUserId(userId);
        history.setPassword(encodedPassword);
        passwordHistoryMapper.insert(history);

        // 清理过期的密码历史（保留最近 N 条）
        cleanOldPasswordHistory(userId);
    }

    /**
     * 清理过期的密码历史
     */
    private void cleanOldPasswordHistory(Long userId) {
        List<UserPasswordHistory> historyList = passwordHistoryMapper.selectList(
            new LambdaQueryWrapper<UserPasswordHistory>()
                .eq(UserPasswordHistory::getUserId, userId)
                .orderByDesc(UserPasswordHistory::getCreateTime)
        );

        if (historyList.size() > passwordHistoryCount) {
            List<Long> idsToDelete = historyList.stream()
                .skip(passwordHistoryCount)
                .map(UserPasswordHistory::getId)
                .toList();
            
            passwordHistoryMapper.deleteBatchIds(idsToDelete);
        }
    }
}
```

### 4. 修改密码接口

**位置**: `UserService.java`

```java
@Transactional
public void changePassword(Long userId, String oldPassword, String newPassword) {
    User user = getById(userId);
    if (user == null) {
        throw new BusinessException(404, I18nMessageUtil.getMessage("user.not.found"));
    }

    // 校验旧密码
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
        throw new BusinessException(400, I18nMessageUtil.getMessage("user.password.old.incorrect"));
    }

    // 校验密码复杂度
    passwordPolicyValidator.validatePassword(newPassword);

    // 校验密码历史
    passwordHistoryService.validatePasswordNotInHistory(userId, newPassword);

    // 更新密码
    String encodedPassword = passwordEncoder.encode(newPassword);
    user.setPassword(encodedPassword);
    user.setPasswordUpdateTime(LocalDateTime.now());
    updateById(user);

    // 保存密码历史
    passwordHistoryService.savePasswordHistory(userId, encodedPassword);
}
```

---

## 问题 6: 双 Token 机制

### 1. 修改 `JwtService`

**位置**: `JwtService.java`

```java
package com.zhuji.userorg.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    @Value("${jwt.secret:ZhujiSecretKeyForJWTTokenGeneration2024VeryLongSecretKey}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:3600000}") // 1 小时
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 天
    private long refreshTokenExpiration;

    /**
     * 生成访问令牌（短期）
     */
    public String generateAccessToken(String username, List<String> permissions) {
        return generateToken(username, permissions, accessTokenExpiration, "access");
    }

    /**
     * 生成刷新令牌（长期）
     */
    public String generateRefreshToken(String username) {
        return generateToken(username, null, refreshTokenExpiration, "refresh");
    }

    private String generateToken(String username, List<String> permissions, 
                                  long expiration, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        var builder = Jwts.builder()
                .subject(username)
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiryDate);

        if (permissions != null) {
            builder.claim("permissions", permissions);
        }

        return builder.signWith(key).compact();
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 从令牌中获取权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("permissions", List.class);
    }

    /**
     * 从令牌中获取令牌类型
     */
    public String getTokenType(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取令牌过期时间（秒）
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration / 1000;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration / 1000;
    }
}
```

### 2. 修改 `LoginResponse`

**位置**: `LoginResponse.java`

```java
package com.zhuji.userorg.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;      // 访问令牌
    private String refreshToken;     // 刷新令牌
    private Long userId;
    private String username;
    private String tokenType;
    private Long expiresIn;          // 访问令牌过期时间（秒）
    private String passwordExpiryWarning;  // 密码过期警告
}
```

### 3. 修改登录逻辑

**位置**: `UserService.java`

```java
public LoginResponse login(LoginRequest request) {
    User user = lambdaQuery()
            .eq(User::getUsername, request.getUsername())
            .one();

    if (user == null) {
        throw new BusinessException(401, I18nMessageUtil.getMessage("user.not.found"));
    }

    if (userLockService.isLocked(user.getId())) {
        throw new BusinessException(403, I18nMessageUtil.getMessage("user.locked"));
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        userLockService.incrementFailCount(user.getId());
        throw new BusinessException(401, I18nMessageUtil.getMessage("user.password.error"));
    }

    userLockService.resetFailCount(user.getId());

    if (user.getStatus() != 1) {
        throw new BusinessException(403, "用户已被禁用");
    }

    // 检查密码是否过期
    passwordExpiryService.checkPasswordExpiry(user);

    // 更新最后登录时间
    user.setLastLoginTime(LocalDateTime.now());
    updateById(user);

    // 获取权限列表
    List<Permission> permissions = permissionService.listByUserId(user.getId());
    List<String> permissionCodes = permissions.stream()
            .map(Permission::getPermissionCode)
            .collect(Collectors.toList());

    // 生成访问令牌和刷新令牌
    String accessToken = jwtService.generateAccessToken(user.getUsername(), permissionCodes);
    String refreshToken = jwtService.generateRefreshToken(user.getUsername());

    // 存储 Token 到 Redis
    redisTemplate.opsForValue().set(
        TOKEN_PREFIX + user.getId(),
        accessToken,
        jwtService.getAccessTokenExpiration(),
        TimeUnit.SECONDS
    );

    redisTemplate.opsForValue().set(
        REFRESH_TOKEN_PREFIX + user.getId(),
        refreshToken,
        jwtService.getRefreshTokenExpiration(),
        TimeUnit.SECONDS
    );

    // 检查密码是否即将过期
    Integer expiryWarning = passwordExpiryService.getPasswordExpiryWarning(user);

    return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userId(user.getId())
            .username(user.getUsername())
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpiration())
            .passwordExpiryWarning(
                expiryWarning != null 
                    ? I18nMessageUtil.getMessage("user.password.expiry.warning", expiryWarning) 
                    : null
            )
            .build();
}
```

### 4. 刷新令牌接口

**位置**: `UserService.java`

```java
public LoginResponse refreshToken(String refreshToken) {
    // 校验 refreshToken
    if (!jwtService.validateToken(refreshToken)) {
        throw new BusinessException(401, I18nMessageUtil.getMessage("token.invalid"));
    }

    // 检查令牌类型
    String tokenType = jwtService.getTokenType(refreshToken);
    if (!"refresh".equals(tokenType)) {
        throw new BusinessException(401, I18nMessageUtil.getMessage("token.not.refresh"));
    }

    String username = jwtService.getUsernameFromToken(refreshToken);
    User user = lambdaQuery()
            .eq(User::getUsername, username)
            .one();

    if (user == null || user.getStatus() != 1) {
        throw new BusinessException(401, I18nMessageUtil.getMessage("user.not.found.or.disabled"));
    }

    // 检查 refreshToken 是否在 Redis 中
    String storedRefreshToken = (String) redisTemplate.opsForValue()
        .get(REFRESH_TOKEN_PREFIX + user.getId());
    
    if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
        throw new BusinessException(401, I18nMessageUtil.getMessage("token.expired"));
    }

    // 生成新的访问令牌
    List<Permission> permissions = permissionService.listByUserId(user.getId());
    List<String> permissionCodes = permissions.stream()
            .map(Permission::getPermissionCode)
            .collect(Collectors.toList());

    String newAccessToken = jwtService.generateAccessToken(user.getUsername(), permissionCodes);

    // 更新 Redis 中的访问令牌
    redisTemplate.opsForValue().set(
        TOKEN_PREFIX + user.getId(),
        newAccessToken,
        jwtService.getAccessTokenExpiration(),
        TimeUnit.SECONDS
    );

    return LoginResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)  // refreshToken 不变
            .userId(user.getId())
            .username(user.getUsername())
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpiration())
            .build();
}
```

### 5. Controller 接口

**位置**: `AuthController.java`

```java
package com.zhuji.userorg.controller;

import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.userorg.dto.LoginResponse;
import com.zhuji.userorg.dto.RefreshTokenRequest;
import com.zhuji.userorg.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "登录/登出/令牌刷新接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "刷新访问令牌")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(userService.refreshToken(request.getRefreshToken()));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorization) {
        // 从 Token 中获取用户 ID
        String token = authorization.replace("Bearer ", "");
        String username = jwtService.getUsernameFromToken(token);
        User user = userService.getByUsername(username);
        
        if (user != null) {
            userService.logout(user.getId());
        }
        
        return ApiResponse.success();
    }
}
```

---

## 问题 7: Token 黑名单

### 1. Token 黑名单服务

**位置**: `TokenBlacklistService.java`

```java
package com.zhuji.userorg.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtService jwtService;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate, 
                                   JwtService jwtService) {
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }

    /**
     * 将 Token 加入黑名单
     */
    public void addToBlacklist(String token) {
        long expiration = jwtService.getAccessTokenExpiration();
        String blacklistKey = BLACKLIST_PREFIX + token;
        
        redisTemplate.opsForValue().set(
            blacklistKey, 
            "1", 
            expiration, 
            TimeUnit.SECONDS
        );
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        String blacklistKey = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }

    /**
     * 用户退出登录时，将 Token 加入黑名单
     */
    public void logout(String token) {
        addToBlacklist(token);
    }
}
```

### 2. 修改退出登录逻辑

**位置**: `UserService.java`

```java
public void logout(Long userId) {
    // 获取当前 Token
    String token = (String) redisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
    
    if (token != null) {
        // 将 Token 加入黑名单
        tokenBlacklistService.addToBlacklist(token);
        
        // 删除 Redis 中的 Token
        redisTemplate.delete(TOKEN_PREFIX + userId);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }
}
```

### 3. JWT 认证过滤器

**位置**: `JwtAuthenticationFilter.java`

```java
package com.zhuji.userorg.security;

import com.zhuji.userorg.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtService jwtService, 
                                     TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      FilterChain filterChain) 
            throws ServletException, IOException {
        
        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            
            // 验证 Token
            if (jwtService.validateToken(token)) {
                // 检查是否在黑名单中
                if (tokenBlacklistService.isBlacklisted(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 已失效");
                    return;
                }
                
                String username = jwtService.getUsernameFromToken(token);
                List<String> permissions = jwtService.getPermissionsFromToken(token);
                
                List<SimpleGrantedAuthority> authorities = permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

---

## 问题 8: 跨实例配置同步

### 1. 配置变更消息

**位置**: `ConfigChangeMessage.java`

```java
package com.zhuji.userorg.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigChangeMessage implements Serializable {
    private Long userId;
    private String configKey;
    private String operation;  // CREATE, UPDATE, DELETE
    private Long timestamp;
}
```

### 2. Redis 消息发布者

**位置**: `ConfigChangePublisher.java`

```java
package com.zhuji.userorg.event;

import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConfigChangePublisher {

    private static final String CONFIG_CHANGE_CHANNEL = "config:change";
    
    private final RedisTemplate<String, Object> redisTemplate;

    public ConfigChangePublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publishConfigChange(Long userId, String configKey, String operation) {
        ConfigChangeMessage message = new ConfigChangeMessage(
            userId, configKey, operation, System.currentTimeMillis()
        );
        
        redisTemplate.convertAndSend(CONFIG_CHANGE_CHANNEL, JSON.toJSONString(message));
    }
}
```

### 3. Redis 消息监听器

**位置**: `ConfigChangeMessageListener.java`

```java
package com.zhuji.userorg.event;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfigChangeMessageListener implements MessageListener {

    private final CacheManager cacheManager;

    public ConfigChangeMessageListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody());
        ConfigChangeMessage configMessage = JSON.parseObject(body, ConfigChangeMessage.class);

        log.info("收到配置变更消息: userId={}, key={}, operation={}", 
                 configMessage.getUserId(), configMessage.getConfigKey(), configMessage.getOperation());

        // 清除本地缓存
        if (configMessage.getUserId() != null) {
            clearUserConfigCache(configMessage.getUserId(), configMessage.getConfigKey());
        } else {
            clearAllConfigCache();
        }
    }

    private void clearUserConfigCache(Long userId, String configKey) {
        var cache = cacheManager.getCache("user-config");
        if (cache != null) {
            // 清除用户所有配置缓存
            cache.evict(userId);
            
            // 如果指定了 configKey，也清除该键的缓存
            if (configKey != null) {
                cache.evict(userId + ":" + configKey);
            }
        }
    }

    private void clearAllConfigCache() {
        var cache = cacheManager.getCache("user-config");
        if (cache != null) {
            cache.clear();
        }
    }
}
```

### 4. Redis 配置

**位置**: `RedisConfig.java`

```java
package com.zhuji.userorg.config;

import com.zhuji.userorg.event.ConfigChangeMessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            ConfigChangeMessageListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, new PatternTopic("config:change"));
        return container;
    }
}
```

### 5. 修改配置服务

**位置**: `UserConfigServiceImpl.java`

```java
@Service
public class UserConfigServiceImpl implements UserConfigService {

    // ... 已有依赖
    
    private final ConfigChangePublisher configChangePublisher;

    @Override
    @CacheEvict(value = "user-config", key = "#config.userId", allEntries = true)
    public UserConfig createUserConfig(UserConfig config) {
        validateConfig(config.getConfigKey(), config.getConfigValue());
        
        UserConfig existing = userConfigMapper.selectByUserIdAndKey(config.getUserId(), config.getConfigKey());
        if (existing != null) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("validation.unique", "配置键"));
        }
        
        userConfigMapper.insert(config);
        
        // 发布配置变更事件（本地）
        eventPublisher.publishEvent(new ConfigChangeEvent(this, config.getConfigKey()));
        
        // 发布配置变更消息（Redis 跨实例）
        configChangePublisher.publishConfigChange(config.getUserId(), config.getConfigKey(), "CREATE");
        
        return config;
    }

    @Override
    @CacheEvict(value = "user-config", key = "#userId", allEntries = true)
    public UserConfig updateUserConfig(Long id, UserConfig config) {
        validateConfig(config.getConfigKey(), config.getConfigValue());
        
        UserConfig existing = userConfigMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("user.config.not.found"));
        }

        if (!existing.getConfigKey().equals(config.getConfigKey())) {
            UserConfig duplicate = userConfigMapper.selectByUserIdAndKey(existing.getUserId(), config.getConfigKey());
            if (duplicate != null) {
                throw new BusinessException(400, I18nMessageUtil.getMessage("validation.unique", "配置键"));
            }
        }

        config.setId(id);
        config.setUpdateTime(LocalDateTime.now());
        userConfigMapper.updateById(config);
        
        // 发布配置变更事件（本地）
        eventPublisher.publishEvent(new ConfigChangeEvent(this, config.getConfigKey()));
        
        // 发布配置变更消息（Redis 跨实例）
        configChangePublisher.publishConfigChange(existing.getUserId(), config.getConfigKey(), "UPDATE");
        
        return config;
    }

    @Override
    @CacheEvict(value = "user-config", allEntries = true)
    public void deleteUserConfig(Long id) {
        UserConfig config = userConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("user.config.not.found"));
        }
        
        userConfigMapper.deleteById(id);
        
        // 发布配置变更事件（本地）
        eventPublisher.publishEvent(new ConfigChangeEvent(this, config.getConfigKey()));
        
        // 发布配置变更消息（Redis 跨实例）
        configChangePublisher.publishConfigChange(config.getUserId(), config.getConfigKey(), "DELETE");
    }
}
```

---

## 问题 9: 配置版本管理

### 1. 配置历史实体

**位置**: `UserConfigHistory.java`

```java
package com.zhuji.userorg.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("sys_user_config_history")
public class UserConfigHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long configId;
    private Long userId;
    private String configKey;
    private String configValue;
    private String configType;
    private String operation;  // CREATE, UPDATE, DELETE
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime operatedTime;
    
    private String operatedBy;  // 操作人
    
    private Integer version;
    
    // ... getter/setter
}
```

### 2. 配置历史 Mapper

**位置**: `UserConfigHistoryMapper.java`

```java
package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.UserConfigHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserConfigHistoryMapper extends BaseMapper<UserConfigHistory> {
    
    /**
     * 查询配置的版本历史
     */
    List<UserConfigHistory> selectByConfigId(@Param("configId") Long configId);
    
    /**
     * 查询配置的指定版本
     */
    UserConfigHistory selectByConfigIdAndVersion(
        @Param("configId") Long configId, 
        @Param("version") Integer version
    );
    
    /**
     * 获取配置的下一个版本号
     */
    Integer getNextVersion(@Param("configId") Long configId);
}
```

### 3. 配置历史服务

**位置**: `UserConfigHistoryService.java`

```java
package com.zhuji.userorg.service;

import com.zhuji.userorg.dto.ConfigHistoryVO;
import com.zhuji.userorg.entity.UserConfig;
import com.zhuji.userorg.entity.UserConfigHistory;

import java.util.List;

public interface UserConfigHistoryService {
    
    /**
     * 保存配置历史
     */
    void saveHistory(UserConfig config, String operation);
    
    /**
     * 查询配置的版本历史
     */
    List<ConfigHistoryVO> getConfigHistory(Long configId);
    
    /**
     * 回滚配置到指定版本
     */
    UserConfig rollbackToVersion(Long configId, Integer version);
}
```

### 4. 配置历史服务实现

**位置**: `UserConfigHistoryServiceImpl.java`

```java
package com.zhuji.userorg.service.impl;

import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.dto.ConfigHistoryVO;
import com.zhuji.userorg.entity.UserConfig;
import com.zhuji.userorg.entity.UserConfigHistory;
import com.zhuji.userorg.mapper.UserConfigHistoryMapper;
import com.zhuji.userorg.mapper.UserConfigMapper;
import com.zhuji.userorg.service.UserConfigHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserConfigHistoryServiceImpl implements UserConfigHistoryService {

    private final UserConfigHistoryMapper historyMapper;
    private final UserConfigMapper configMapper;

    public UserConfigHistoryServiceImpl(
            UserConfigHistoryMapper historyMapper,
            UserConfigMapper configMapper) {
        this.historyMapper = historyMapper;
        this.configMapper = configMapper;
    }

    @Override
    public void saveHistory(UserConfig config, String operation) {
        UserConfigHistory history = new UserConfigHistory();
        history.setConfigId(config.getId());
        history.setUserId(config.getUserId());
        history.setConfigKey(config.getConfigKey());
        history.setConfigValue(config.getConfigValue());
        history.setConfigType(config.getConfigType());
        history.setOperation(operation);
        history.setOperatedTime(LocalDateTime.now());
        history.setOperatedBy(getCurrentUser());
        history.setVersion(getNextVersion(config.getId()));
        
        historyMapper.insert(history);
    }

    @Override
    public List<ConfigHistoryVO> getConfigHistory(Long configId) {
        List<UserConfigHistory> historyList = historyMapper.selectByConfigId(configId);
        
        return historyList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserConfig rollbackToVersion(Long configId, Integer version) {
        UserConfigHistory history = historyMapper.selectByConfigIdAndVersion(configId, version);
        
        if (history == null) {
            throw new BusinessException(404, 
                I18nMessageUtil.getMessage("config.version.not.found"));
        }
        
        UserConfig config = configMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException(404, 
                I18nMessageUtil.getMessage("user.config.not.found"));
        }
        
        // 保存当前版本到历史
        saveHistory(config, "ROLLBACK");
        
        // 回滚配置
        config.setConfigValue(history.getConfigValue());
        config.setUpdateTime(LocalDateTime.now());
        configMapper.updateById(config);
        
        return config;
    }

    private Integer getNextVersion(Long configId) {
        Integer nextVersion = historyMapper.getNextVersion(configId);
        return nextVersion != null ? nextVersion + 1 : 1;
    }

    private String getCurrentUser() {
        // 从 Spring Security 获取当前用户
        // return SecurityContextHolder.getContext().getAuthentication().getName();
        return "system";
    }

    private ConfigHistoryVO convertToVO(UserConfigHistory history) {
        ConfigHistoryVO vo = new ConfigHistoryVO();
        vo.setId(history.getId());
        vo.setConfigId(history.getConfigId());
        vo.setConfigKey(history.getConfigKey());
        vo.setConfigValue(history.getConfigValue());
        vo.setOperation(history.getOperation());
        vo.setOperatedTime(history.getOperatedTime());
        vo.setOperatedBy(history.getOperatedBy());
        vo.setVersion(history.getVersion());
        return vo;
    }
}
```

---

## 问题 10: 配置加密

### 1. 配置加密服务

**位置**: `ConfigEncryptionService.java`

```java
package com.zhuji.userorg.service;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ConfigEncryptionService {

    private final StringEncryptor encryptor;
    
    // 需要加密的配置键前缀
    private static final List<String> SENSITIVE_PREFIXES = Arrays.asList(
        "password", "apiKey", "secret", "token", "credential", "key"
    );

    public ConfigEncryptionService(StringEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    /**
     * 判断配置是否需要加密
     */
    public boolean isSensitive(String configKey) {
        if (configKey == null) {
            return false;
        }
        
        String lowerKey = configKey.toLowerCase();
        return SENSITIVE_PREFIXES.stream()
                .anyMatch(prefix -> lowerKey.contains(prefix.toLowerCase()));
    }

    /**
     * 加密配置值
     */
    public String encrypt(String configKey, String configValue) {
        if (!isSensitive(configKey) || configValue == null) {
            return configValue;
        }
        
        return encryptor.encrypt(configValue);
    }

    /**
     * 解密配置值
     */
    public String decrypt(String configKey, String configValue) {
        if (!isSensitive(configKey) || configValue == null) {
            return configValue;
        }
        
        try {
            return encryptor.decrypt(configValue);
        } catch (Exception e) {
            // 如果解密失败，说明可能是明文，直接返回
            return configValue;
        }
    }
}
```

### 2. 修改配置服务

**位置**: `UserConfigServiceImpl.java`

```java
@Service
public class UserConfigServiceImpl implements UserConfigService {

    // ... 已有依赖
    
    private final ConfigEncryptionService encryptionService;

    @Override
    @Cacheable(value = "user-config", key = "#userId + ':' + #configKey")
    public UserConfig getUserConfigByKey(Long userId, String configKey) {
        UserConfig config = userConfigMapper.selectByUserIdAndKey(userId, configKey);
        
        if (config != null) {
            // 解密敏感配置
            config.setConfigValue(
                encryptionService.decrypt(config.getConfigKey(), config.getConfigValue())
            );
        }
        
        return config;
    }

    @Override
    @CacheEvict(value = "user-config", key = "#config.userId", allEntries = true)
    public UserConfig createUserConfig(UserConfig config) {
        validateConfig(config.getConfigKey(), config.getConfigValue());
        
        UserConfig existing = userConfigMapper.selectByUserIdAndKey(config.getUserId(), config.getConfigKey());
        if (existing != null) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("validation.unique", "配置键"));
        }
        
        // 加密敏感配置
        config.setConfigValue(
            encryptionService.encrypt(config.getConfigKey(), config.getConfigValue())
        );
        
        userConfigMapper.insert(config);
        
        // 发布配置变更消息
        configChangePublisher.publishConfigChange(config.getUserId(), config.getConfigKey(), "CREATE");
        
        return config;
    }

    @Override
    @CacheEvict(value = "user-config", key = "#userId", allEntries = true)
    public UserConfig updateUserConfig(Long id, UserConfig config) {
        validateConfig(config.getConfigKey(), config.getConfigValue());
        
        UserConfig existing = userConfigMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("user.config.not.found"));
        }

        if (!existing.getConfigKey().equals(config.getConfigKey())) {
            UserConfig duplicate = userConfigMapper.selectByUserIdAndKey(existing.getUserId(), config.getConfigKey());
            if (duplicate != null) {
                throw new BusinessException(400, I18nMessageUtil.getMessage("validation.unique", "配置键"));
            }
        }

        config.setId(id);
        config.setUpdateTime(LocalDateTime.now());
        
        // 加密敏感配置
        config.setConfigValue(
            encryptionService.encrypt(config.getConfigKey(), config.getConfigValue())
        );
        
        userConfigMapper.updateById(config);
        
        // 发布配置变更消息
        configChangePublisher.publishConfigChange(existing.getUserId(), config.getConfigKey(), "UPDATE");
        
        return config;
    }
}
```

---

## 问题 11: 配置导入导出

### 1. 配置导出接口

**位置**: `UserConfigService.java`

```java
/**
 * 导出用户配置
 */
List<UserConfig> exportUserConfigs(Long userId, String configType);

/**
 * 导入用户配置
 */
void importUserConfigs(Long userId, List<UserConfig> configs, boolean overwrite);
```

### 2. 配置导出导入实现

**位置**: `UserConfigServiceImpl.java`

```java
@Override
public List<UserConfig> exportUserConfigs(Long userId, String configType) {
    LambdaQueryWrapper<UserConfig> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserConfig::getUserId, userId);
    
    if (configType != null && !configType.isEmpty()) {
        queryWrapper.eq(UserConfig::getConfigType, configType);
    }
    
    List<UserConfig> configs = userConfigMapper.selectList(queryWrapper);
    
    // 解密敏感配置
    for (UserConfig config : configs) {
        config.setConfigValue(
            encryptionService.decrypt(config.getConfigKey(), config.getConfigValue())
        );
    }
    
    return configs;
}

@Override
@Transactional
public void importUserConfigs(Long userId, List<UserConfig> configs, boolean overwrite) {
    for (UserConfig config : configs) {
        UserConfig existing = userConfigMapper.selectByUserIdAndKey(userId, config.getConfigKey());
        
        if (existing != null) {
            if (overwrite) {
                // 覆盖已有配置
                existing.setConfigValue(
                    encryptionService.encrypt(config.getConfigKey(), config.getConfigValue())
                );
                existing.setConfigType(config.getConfigType());
                existing.setUpdateTime(LocalDateTime.now());
                userConfigMapper.updateById(existing);
            }
            // 如果不覆盖，跳过
        } else {
            // 创建新配置
            config.setUserId(userId);
            config.setConfigValue(
                encryptionService.encrypt(config.getConfigKey(), config.getConfigValue())
            );
            userConfigMapper.insert(config);
        }
    }
    
    // 发布配置变更消息
    configChangePublisher.publishConfigChange(userId, null, "IMPORT");
}
```

### 3. Controller 接口

**位置**: `UserConfigController.java`

```java
@Operation(summary = "导出用户配置")
@GetMapping("/{id}/configs/export")
public ApiResponse<List<UserConfig>> exportConfigs(
        @PathVariable Long id,
        @RequestParam(required = false) String configType) {
    return ApiResponse.success(userConfigService.exportUserConfigs(id, configType));
}

@Operation(summary = "导入用户配置")
@PostMapping("/{id}/configs/import")
public ApiResponse<Void> importConfigs(
        @PathVariable Long id,
        @RequestBody List<UserConfig> configs,
        @RequestParam(defaultValue = "false") boolean overwrite) {
    userConfigService.importUserConfigs(id, configs, overwrite);
    return ApiResponse.success();
}
```

---

**文件生成时间**: 2026-05-21 14:30:00  
**对应报告**: [源码深度分析报告 V2](./source-code-analysis-v2.md)
