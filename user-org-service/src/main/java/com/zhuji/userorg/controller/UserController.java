package com.zhuji.userorg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.userorg.dto.CreateUserRequest;
import com.zhuji.userorg.entity.UserConfig;
import com.zhuji.userorg.service.UserConfigService;
import com.zhuji.userorg.service.UserService;
import com.zhuji.userorg.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理", description = "用户CRUD及配置管理接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserConfigService userConfigService;

    public UserController(UserService userService, UserConfigService userConfigService) {
        this.userService = userService;
        this.userConfigService = userConfigService;
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public ApiResponse<UserVO> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @Operation(summary = "分页查询用户")
    @GetMapping
    public ApiResponse<Page<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(userService.listUsers(page, size, username, status));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateUser(@PathVariable Long id, @Valid @RequestBody CreateUserRequest request) {
        userService.updateUser(id, request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    @Operation(summary = "分配用户角色(多角色)")
    @PostMapping("/{id}/roles")
    public ApiResponse<Void> assignRoles(
            @PathVariable Long id,
            @RequestBody List<Long> roleIds,
            @RequestParam(defaultValue = "1") String isPrimary) {
        userConfigService.batchAssignRoles(id, roleIds, isPrimary);
        return ApiResponse.success();
    }

    @Operation(summary = "获取用户角色列表")
    @GetMapping("/{id}/roles")
    public ApiResponse<List<Map<String, Object>>> getUserRoles(@PathVariable Long id) {
        return ApiResponse.success(userConfigService.getUserRoles(id));
    }

    @Operation(summary = "移除用户角色")
    @DeleteMapping("/{id}/roles/{roleId}")
    public ApiResponse<Void> removeUserRole(@PathVariable Long id, @PathVariable Long roleId) {
        userConfigService.removeUserRole(id, roleId);
        return ApiResponse.success();
    }

    @Operation(summary = "设置主角色")
    @PutMapping("/{id}/roles/{roleId}/primary")
    public ApiResponse<Void> setPrimaryRole(@PathVariable Long id, @PathVariable Long roleId) {
        userConfigService.setPrimaryRole(id, roleId);
        return ApiResponse.success();
    }

    @Operation(summary = "分配用户组织(多组织)")
    @PostMapping("/{id}/orgs")
    public ApiResponse<Void> assignOrgs(
            @PathVariable Long id,
            @RequestBody List<Long> orgIds,
            @RequestParam(defaultValue = "1") String isPrimary) {
        userConfigService.batchAssignOrgs(id, orgIds, isPrimary);
        return ApiResponse.success();
    }

    @Operation(summary = "获取用户组织列表")
    @GetMapping("/{id}/orgs")
    public ApiResponse<List<Map<String, Object>>> getUserOrgs(@PathVariable Long id) {
        return ApiResponse.success(userConfigService.getUserOrgs(id));
    }

    @Operation(summary = "移除用户组织")
    @DeleteMapping("/{id}/orgs/{orgId}")
    public ApiResponse<Void> removeUserOrg(@PathVariable Long id, @PathVariable Long orgId) {
        userConfigService.removeUserOrg(id, orgId);
        return ApiResponse.success();
    }

    @Operation(summary = "设置主组织")
    @PutMapping("/{id}/orgs/{orgId}/primary")
    public ApiResponse<Void> setPrimaryOrg(@PathVariable Long id, @PathVariable Long orgId) {
        userConfigService.setPrimaryOrg(id, orgId);
        return ApiResponse.success();
    }

    @Operation(summary = "获取用户配置列表")
    @GetMapping("/{id}/configs")
    public ApiResponse<Page<UserConfig>> getUserConfigs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String configType) {
        return ApiResponse.success(userConfigService.pageUserConfigs(page, size, id, configType));
    }

    @Operation(summary = "创建用户配置")
    @PostMapping("/{id}/configs")
    public ApiResponse<UserConfig> createUserConfig(@PathVariable Long id, @RequestBody UserConfig config) {
        config.setUserId(id);
        return ApiResponse.success(userConfigService.createUserConfig(config));
    }

    @Operation(summary = "更新用户配置")
    @PutMapping("/{id}/configs/{configId}")
    public ApiResponse<UserConfig> updateUserConfig(
            @PathVariable Long id,
            @PathVariable Long configId,
            @RequestBody UserConfig config) {
        return ApiResponse.success(userConfigService.updateUserConfig(configId, config));
    }

    @Operation(summary = "删除用户配置")
    @DeleteMapping("/{id}/configs/{configId}")
    public ApiResponse<Void> deleteUserConfig(@PathVariable Long configId) {
        userConfigService.deleteUserConfig(configId);
        return ApiResponse.success();
    }
}