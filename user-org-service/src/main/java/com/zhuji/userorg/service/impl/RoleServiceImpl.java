package com.zhuji.userorg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.entity.Permission;
import com.zhuji.userorg.entity.Role;
import com.zhuji.userorg.entity.RolePermission;
import com.zhuji.userorg.entity.UserRole;
import com.zhuji.userorg.mapper.PermissionMapper;
import com.zhuji.userorg.mapper.RoleMapper;
import com.zhuji.userorg.mapper.RolePermissionMapper;
import com.zhuji.userorg.mapper.UserRoleMapper;
import com.zhuji.userorg.service.PermissionService;
import com.zhuji.userorg.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;

    public RoleServiceImpl(RoleMapper roleMapper,
                          RolePermissionMapper rolePermissionMapper,
                          UserRoleMapper userRoleMapper,
                          PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public Page<Role> page(int pageNum, int pageSize, String keyword) {
        Page<Role> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Role::getRoleCode, keyword)
                    .or()
                    .like(Role::getRoleName, keyword));
        }

        wrapper.orderByDesc(Role::getCreateTime);
        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Role> listAll() {
        return roleMapper.selectList(
            new LambdaQueryWrapper<Role>()
                .orderByAsc(Role::getSortOrder)
        );
    }

    @Override
    public Role getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("role.not.found"));
        }
        return role;
    }

    @Override
    public Role getByCode(String code) {
        return roleMapper.selectOne(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, code)
        );
    }

    @Override
    @Transactional
    public Role create(Role role) {
        if (roleMapper.selectOne(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, role.getRoleCode())
        ) != null) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("role.code.exists"));
        }

        if (role.getStatus() == null) {
            role.setStatus("1");
        }

        roleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional
    public Role update(Long id, Role role) {
        Role existingRole = getById(id);

        if (!existingRole.getRoleCode().equals(role.getRoleCode()) &&
            roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                    .eq(Role::getRoleCode, role.getRoleCode())
            ) != null) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("role.code.exists"));
        }

        existingRole.setRoleName(role.getRoleName());
        existingRole.setDescription(role.getDescription());
        existingRole.setSortOrder(role.getSortOrder());
        existingRole.setStatus(role.getStatus());

        roleMapper.updateById(existingRole);
        return existingRole;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = getById(id);

        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getRoleId, id);
        long userCount = userRoleMapper.selectCount(userRoleWrapper);
        if (userCount > 0) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("role.in.use"));
        }

        rolePermissionMapper.delete(
            new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, id)
        );

        userRoleMapper.delete(userRoleWrapper);

        roleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        getById(roleId);

        rolePermissionMapper.delete(
            new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId)
        );

        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermission> rolePermissions = permissionIds.stream()
                .map(permissionId -> {
                    RolePermission rp = new RolePermission();
                    rp.setRoleId(roleId);
                    rp.setPermissionId(permissionId);
                    return rp;
                })
                .collect(Collectors.toList());

            for (RolePermission rp : rolePermissions) {
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    public List<Long> getPermissionIds(Long roleId) {
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId)
        );

        return rolePermissions.stream()
            .map(RolePermission::getPermissionId)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Role> batchCreate(List<Role> roles) {
        List<Role> created = new ArrayList<>();
        for (Role role : roles) {
            created.add(create(role));
        }
        return created;
    }

    @Override
    @Transactional
    public void batchUpdate(List<Role> roles) {
        for (Role role : roles) {
            if (role.getId() == null) {
                throw new BusinessException(400, I18nMessageUtil.getMessage("common.param.invalid"));
            }
            update(role.getId(), role);
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Role role = getById(id);
        role.setStatus(status);
        roleMapper.updateById(role);
    }

    @Override
    @Transactional
    public void copyPermissions(Long sourceRoleId, Long targetRoleId) {
        getById(sourceRoleId);
        getById(targetRoleId);

        List<Long> permissionIds = getPermissionIds(sourceRoleId);
        assignPermissions(targetRoleId, permissionIds);
    }
}