package com.zhuji.userorg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.userorg.entity.Role;

import java.util.List;

public interface RoleService {

    Page<Role> page(int pageNum, int pageSize, String keyword);

    List<Role> listAll();

    Role getById(Long id);

    Role getByCode(String code);

    Role create(Role role);

    Role update(Long id, Role role);

    void delete(Long id);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    List<Long> getPermissionIds(Long roleId);

    List<Role> batchCreate(List<Role> roles);

    void batchUpdate(List<Role> roles);

    void updateStatus(Long id, String status);

    void copyPermissions(Long sourceRoleId, Long targetRoleId);
}