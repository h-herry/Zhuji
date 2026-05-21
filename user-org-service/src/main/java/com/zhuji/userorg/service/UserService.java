package com.zhuji.userorg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuji.common.core.enums.ErrorCode;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.dto.CreateUserRequest;
import com.zhuji.userorg.dto.LoginRequest;
import com.zhuji.userorg.dto.LoginResponse;
import com.zhuji.userorg.entity.Permission;
import com.zhuji.userorg.entity.User;
import com.zhuji.userorg.entity.UserRole;
import com.zhuji.userorg.mapper.UserMapper;
import com.zhuji.userorg.mapper.UserRoleMapper;
import com.zhuji.userorg.security.PasswordExpiryService;
import com.zhuji.userorg.security.PasswordHistoryService;
import com.zhuji.userorg.security.PasswordPolicyValidator;
import com.zhuji.userorg.security.TokenBlacklistService;
import com.zhuji.userorg.security.UserLockService;
import com.zhuji.userorg.vo.UserVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;
    private final UserRoleMapper userRoleMapper;
    private final PermissionService permissionService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtService jwtService;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final UserLockService userLockService;
    private final PasswordExpiryService passwordExpiryService;
    private final PasswordHistoryService passwordHistoryService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String TOKEN_PREFIX = "auth:token:";
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";

    public UserService(PasswordEncoder passwordEncoder,
                       UserRoleMapper userRoleMapper,
                       PermissionService permissionService,
                       RedisTemplate<String, Object> redisTemplate,
                       JwtService jwtService,
                       PasswordPolicyValidator passwordPolicyValidator,
                       UserLockService userLockService,
                       PasswordExpiryService passwordExpiryService,
                       PasswordHistoryService passwordHistoryService,
                       TokenBlacklistService tokenBlacklistService) {
        this.passwordEncoder = passwordEncoder;
        this.userRoleMapper = userRoleMapper;
        this.permissionService = permissionService;
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.userLockService = userLockService;
        this.passwordExpiryService = passwordExpiryService;
        this.passwordHistoryService = passwordHistoryService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Transactional
    public UserVO register(CreateUserRequest request) {
        passwordPolicyValidator.validatePassword(request.getPassword());
        return createUserInternal(request, true);
    }

    @CacheEvict(value = "user", allEntries = true)
    @Transactional
    public UserVO createUser(CreateUserRequest request) {
        passwordPolicyValidator.validatePassword(request.getPassword());
        return createUserInternal(request, false);
    }

    private UserVO createUserInternal(CreateUserRequest request, boolean checkEmail) {
        if (lambdaQuery().eq(User::getUsername, request.getUsername()).exists()) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("user.username.exists"));
        }

        if (checkEmail && request.getEmail() != null && lambdaQuery().eq(User::getEmail, request.getEmail()).exists()) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("user.email.exists"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .orgId(request.getOrgId())
                .status(1)
                .passwordUpdateTime(LocalDateTime.now())
                .build();
        save(user);

        passwordHistoryService.savePasswordHistory(user.getId(), user.getPassword());

        return convertToVO(user);
    }

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

        passwordExpiryService.checkPasswordExpiry(user);

        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);

        List<Permission> permissions = permissionService.listByUserId(user.getId());
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());

        String accessToken = jwtService.generateAccessToken(user.getUsername(), permissionCodes);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

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

    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new BusinessException(401, I18nMessageUtil.getMessage("token.invalid"));
        }

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

        String storedRefreshToken = (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + user.getId());

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(401, I18nMessageUtil.getMessage("token.expired"));
        }

        List<Permission> permissions = permissionService.listByUserId(user.getId());
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());

        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), permissionCodes);
        String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

        redisTemplate.opsForValue().set(
            TOKEN_PREFIX + user.getId(),
            newAccessToken,
            jwtService.getAccessTokenExpiration(),
            TimeUnit.SECONDS
        );

        redisTemplate.opsForValue().set(
            REFRESH_TOKEN_PREFIX + user.getId(),
            newRefreshToken,
            jwtService.getRefreshTokenExpiration(),
            TimeUnit.SECONDS
        );

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .build();
    }

    public void logout(Long userId) {
        String token = (String) redisTemplate.opsForValue().get(TOKEN_PREFIX + userId);

        if (token != null) {
            tokenBlacklistService.addToBlacklist(token);

            redisTemplate.delete(TOKEN_PREFIX + userId);
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        }
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("user.not.found"));
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("user.password.old.incorrect"));
        }

        passwordPolicyValidator.validatePassword(newPassword);

        passwordHistoryService.validatePasswordNotInHistory(userId, newPassword);

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setPasswordUpdateTime(LocalDateTime.now());
        updateById(user);

        passwordHistoryService.savePasswordHistory(userId, encodedPassword);
    }

    @Cacheable(value = "user", key = "#id")
    public UserVO getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), I18nMessageUtil.getMessage("user.not.found"));
        }
        return convertToVO(user);
    }

    public Page<UserVO> listUsers(int page, int size, String username, Integer status) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = page(pageParam, wrapper);
        Page<UserVO> result = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        return result.setRecords(userPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
    }

    @CacheEvict(value = "user", key = "#id")
    public void updateUser(Long id, CreateUserRequest request) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), I18nMessageUtil.getMessage("user.not.found"));
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getOrgId() != null) {
            user.setOrgId(request.getOrgId());
        }
        updateById(user);
    }

    @CacheEvict(value = "user", key = "#id")
    public void deleteUser(Long id) {
        userRoleMapper.delete(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, id)
        );
        removeById(id);
    }

    @CacheEvict(value = "user-roles", key = "#userId", allEntries = true)
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("user.not.found"));
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

        List<Long> toRemove = existingRoleIds.stream()
            .filter(id -> !roleIds.contains(id))
            .collect(Collectors.toList());
        if (!toRemove.isEmpty()) {
            userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, userId)
                    .in(UserRole::getRoleId, toRemove)
            );
        }

        List<Long> toAdd = roleIds.stream()
            .filter(id -> !existingRoleIds.contains(id))
            .collect(Collectors.toList());
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

    @Cacheable(value = "user-roles", key = "#userId")
    public List<Long> getUserRoleIds(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
        );

        return userRoles.stream()
            .map(UserRole::getRoleId)
            .collect(Collectors.toList());
    }

    private UserVO convertToVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .orgId(user.getOrgId())
                .createTime(user.getCreateTime())
                .build();
    }
}
