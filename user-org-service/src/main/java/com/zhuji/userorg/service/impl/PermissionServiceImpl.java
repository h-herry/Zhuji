package com.zhuji.userorg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.entity.Permission;
import com.zhuji.userorg.entity.RolePermission;
import com.zhuji.userorg.entity.UserRole;
import com.zhuji.userorg.mapper.PermissionMapper;
import com.zhuji.userorg.mapper.RolePermissionMapper;
import com.zhuji.userorg.mapper.UserRoleMapper;
import com.zhuji.userorg.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    public PermissionServiceImpl(PermissionMapper permissionMapper,
                               RolePermissionMapper rolePermissionMapper,
                               UserRoleMapper userRoleMapper) {
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public Page<Permission> page(int pageNum, int pageSize, String keyword) {
        Page<Permission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Permission::getPermissionCode, keyword)
                    .or()
                    .like(Permission::getPermissionName, keyword));
        }

        wrapper.orderByAsc(Permission::getSortOrder);
        return permissionMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Permission> listAll() {
        return permissionMapper.selectList(
            new LambdaQueryWrapper<Permission>()
                .orderByAsc(Permission::getSortOrder)
        );
    }

    @Override
    public List<Permission> listByUserId(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
        );

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> roleIds = userRoles.stream()
            .map(UserRole::getRoleId)
            .collect(Collectors.toList());

        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<RolePermission>()
                .in(RolePermission::getRoleId, roleIds)
        );

        if (rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> permissionIds = rolePermissions.stream()
            .map(RolePermission::getPermissionId)
            .distinct()
            .collect(Collectors.toList());

        return permissionMapper.selectList(
            new LambdaQueryWrapper<Permission>()
                .in(Permission::getId, permissionIds)
                .eq(Permission::getStatus, "1")
        );
    }

    @Override
    public List<Permission> listByRoleId(Long roleId) {
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId)
        );

        if (rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> permissionIds = rolePermissions.stream()
            .map(RolePermission::getPermissionId)
            .collect(Collectors.toList());

        return permissionMapper.selectList(
            new LambdaQueryWrapper<Permission>()
                .in(Permission::getId, permissionIds)
                .orderByAsc(Permission::getSortOrder)
        );
    }

    @Override
    public Permission getById(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("permission.not.found"));
        }
        return permission;
    }

    @Override
    public Permission create(Permission permission) {
        if (permissionMapper.selectOne(
            new LambdaQueryWrapper<Permission>()
                .eq(Permission::getPermissionCode, permission.getPermissionCode())
        ) != null) {
            throw new BusinessException(400, "权限编码已存在");
        }

        if (permission.getStatus() == null) {
            permission.setStatus("1");
        }

        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    public Permission update(Long id, Permission permission) {
        Permission existingPermission = getById(id);

        if (!existingPermission.getPermissionCode().equals(permission.getPermissionCode()) &&
            permissionMapper.selectOne(
                new LambdaQueryWrapper<Permission>()
                    .eq(Permission::getPermissionCode, permission.getPermissionCode())
            ) != null) {
            throw new BusinessException(400, "权限编码已存在");
        }

        existingPermission.setPermissionName(permission.getPermissionName());
        existingPermission.setResourceType(permission.getResourceType());
        existingPermission.setPath(permission.getPath());
        existingPermission.setHttpMethod(permission.getHttpMethod());
        existingPermission.setParentId(permission.getParentId());
        existingPermission.setSortOrder(permission.getSortOrder());
        existingPermission.setStatus(permission.getStatus());

        permissionMapper.updateById(existingPermission);
        return existingPermission;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getById(id);

        rolePermissionMapper.delete(
            new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getPermissionId, id)
        );

        permissionMapper.deleteById(id);
    }
}