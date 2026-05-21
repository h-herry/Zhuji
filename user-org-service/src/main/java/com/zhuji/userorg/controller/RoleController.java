package com.zhuji.userorg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.userorg.entity.Role;
import com.zhuji.userorg.entity.RoleConfig;
import com.zhuji.userorg.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理", description = "角色CRUD及配置管理接口")
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "分页查询角色")
    @GetMapping
    public ResponseEntity<Page<Role>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(roleService.page(pageNum, pageSize, keyword));
    }

    @Operation(summary = "获取所有角色")
    @GetMapping("/all")
    public ResponseEntity<List<Role>> listAll() {
        return ResponseEntity.ok(roleService.listAll());
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public ResponseEntity<Role> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getById(id));
    }

    @Operation(summary = "获取角色权限ID列表")
    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<Long>> getPermissionIds(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getPermissionIds(id));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public ResponseEntity<Role> create(@RequestBody Role role) {
        return ResponseEntity.ok(roleService.create(role));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public ResponseEntity<Role> update(@PathVariable Long id, @RequestBody Role role) {
        return ResponseEntity.ok(roleService.update(id, role));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "分配角色权限")
    @PostMapping("/{id}/permissions")
    public ResponseEntity<Void> assignPermissions(
            @PathVariable Long id,
            @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "批量创建角色")
    @PostMapping("/batch")
    public ResponseEntity<List<Role>> batchCreate(@RequestBody List<Role> roles) {
        List<Role> created = roleService.batchCreate(roles);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "批量更新角色")
    @PutMapping("/batch")
    public ResponseEntity<Void> batchUpdate(@RequestBody List<Role> roles) {
        roleService.batchUpdate(roles);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "启用角色")
    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        roleService.updateStatus(id, "1");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "禁用角色")
    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        roleService.updateStatus(id, "0");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "复制角色权限")
    @PostMapping("/{id}/copy-permissions/{targetRoleId}")
    public ResponseEntity<Void> copyPermissions(
            @PathVariable Long id,
            @PathVariable Long targetRoleId) {
        roleService.copyPermissions(id, targetRoleId);
        return ResponseEntity.ok().build();
    }
}