package com.zhuji.userorg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.userorg.entity.Permission;
import com.zhuji.userorg.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<Page<Permission>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(permissionService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Permission>> listAll() {
        return ResponseEntity.ok(permissionService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> getById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Permission>> listByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(permissionService.listByUserId(userId));
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<Permission>> listByRoleId(@PathVariable Long roleId) {
        return ResponseEntity.ok(permissionService.listByRoleId(roleId));
    }

    @PostMapping
    public ResponseEntity<Permission> create(@RequestBody Permission permission) {
        return ResponseEntity.ok(permissionService.create(permission));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Permission> update(@PathVariable Long id, @RequestBody Permission permission) {
        return ResponseEntity.ok(permissionService.update(id, permission));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}