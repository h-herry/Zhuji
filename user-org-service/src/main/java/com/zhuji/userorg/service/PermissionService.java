package com.zhuji.userorg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.userorg.entity.Permission;

import java.util.List;

public interface PermissionService {

    Page<Permission> page(int pageNum, int pageSize, String keyword);

    List<Permission> listAll();

    List<Permission> listByUserId(Long userId);

    List<Permission> listByRoleId(Long roleId);

    Permission getById(Long id);

    Permission create(Permission permission);

    Permission update(Long id, Permission permission);

    void delete(Long id);
}