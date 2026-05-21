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
import com.zhuji.userorg.service.PermissionService;
import com.zhuji.userorg.vo.UserVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final String TOKEN_PREFIX = "auth:token:";
    private static final long TOKEN_EXPIRATION = 86400;

    public UserService(PasswordEncoder passwordEncoder,
                      UserRoleMapper userRoleMapper,
                      PermissionService permissionService,
                      RedisTemplate<String, Object> redisTemplate,
                      JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.userRoleMapper = userRoleMapper;
        this.permissionService = permissionService;
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserVO register(CreateUserRequest request) {
        if (lambdaQuery().eq(User::getUsername, request.getUsername()).exists()) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("user.username.exists"));
        }

        if (request.getEmail() != null && lambdaQuery().eq(User::getEmail, request.getEmail()).exists()) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("user.email.exists"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .orgId(request.getOrgId())
                .status(1)
                .build();
        save(user);

        return convertToVO(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = lambdaQuery()
                .eq(User::getUsername, request.getUsername())
                .one();

        if (user == null) {
            throw new BusinessException(401, I18nMessageUtil.getMessage("user.not.found"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, I18nMessageUtil.getMessage("user.password.error"));
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(403, "用户已被禁用");
        }

        List<Permission> permissions = permissionService.listByUserId(user.getId());
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());

        String token = jwtService.generateToken(user.getUsername(), permissionCodes);

        redisTemplate.opsForValue().set(
            TOKEN_PREFIX + user.getId(),
            token,
            TOKEN_EXPIRATION,
            TimeUnit.SECONDS
        );

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .tokenType("Bearer")
                .expiresIn(TOKEN_EXPIRATION)
                .build();
    }

    public void logout(Long userId) {
        redisTemplate.delete(TOKEN_PREFIX + userId);
    }

    @CacheEvict(value = "user", allEntries = true)
    public UserVO createUser(CreateUserRequest request) {
        if (lambdaQuery().eq(User::getUsername, request.getUsername()).exists()) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS.getCode(), I18nMessageUtil.getMessage("user.username.exists"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .orgId(request.getOrgId())
                .status(1)
                .build();
        save(user);

        return convertToVO(user);
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

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("user.not.found"));
        }

        userRoleMapper.delete(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
        );

        if (roleIds != null && !roleIds.isEmpty()) {
            List<UserRole> userRoles = roleIds.stream()
                .map(roleId -> {
                    UserRole ur = new UserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(roleId);
                    return ur;
                })
                .collect(Collectors.toList());

            for (UserRole ur : userRoles) {
                userRoleMapper.insert(ur);
            }
        }
    }

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