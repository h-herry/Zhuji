package com.zhuji.userorg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.userorg.entity.GlobalConfig;
import com.zhuji.userorg.service.ConfigManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "配置管理", description = "系统配置管理接口")
@RestController
@RequestMapping("/api/v1/configs")
public class ConfigController {

    private final ConfigManagementService configManagementService;

    public ConfigController(ConfigManagementService configManagementService) {
        this.configManagementService = configManagementService;
    }

    @Operation(summary = "分页查询配置列表")
    @GetMapping
    public ApiResponse<Page<GlobalConfig>> pageConfigs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String configType,
            @RequestParam(required = false) String configKey) {
        return ApiResponse.success(configManagementService.pageConfigs(page, size, configType, configKey));
    }

    @Operation(summary = "获取配置详情")
    @GetMapping("/{id}")
    public ApiResponse<GlobalConfig> getConfig(@PathVariable Long id) {
        return ApiResponse.success(configManagementService.getConfigById(id));
    }

    @Operation(summary = "根据Key获取配置")
    @GetMapping("/key/{configKey}")
    public ApiResponse<GlobalConfig> getConfigByKey(@PathVariable String configKey) {
        GlobalConfig config = configManagementService.getConfigByKey(configKey);
        if (config == null) {
            return ApiResponse.success(null);
        }
        return ApiResponse.success(config);
    }

    @Operation(summary = "根据类型获取配置列表")
    @GetMapping("/type/{configType}")
    public ApiResponse<List<GlobalConfig>> getConfigsByType(@PathVariable String configType) {
        return ApiResponse.success(configManagementService.getConfigsByType(configType));
    }

    @Operation(summary = "创建配置")
    @PostMapping
    public ApiResponse<GlobalConfig> createConfig(@RequestBody GlobalConfig config) {
        return ApiResponse.success(configManagementService.createConfig(config));
    }

    @Operation(summary = "更新配置")
    @PutMapping("/{id}")
    public ApiResponse<GlobalConfig> updateConfig(@PathVariable Long id, @RequestBody GlobalConfig config) {
        return ApiResponse.success(configManagementService.updateConfig(id, config));
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConfig(@PathVariable Long id) {
        configManagementService.deleteConfig(id);
        return ApiResponse.success();
    }

    @Operation(summary = "获取配置值")
    @GetMapping("/value/{configKey}")
    public ApiResponse<String> getConfigValue(
            @PathVariable String configKey,
            @RequestParam(required = false, defaultValue = "") String defaultValue) {
        return ApiResponse.success(configManagementService.getConfigValue(configKey, defaultValue));
    }

    @Operation(summary = "获取所有配置类型")
    @GetMapping("/types")
    public ApiResponse<List<Map<String, Object>>> getConfigTypes() {
        return ApiResponse.success(configManagementService.getConfigTypes());
    }

    @Operation(summary = "刷新配置缓存")
    @PostMapping("/refresh-cache")
    public ApiResponse<Void> refreshCache() {
        configManagementService.refreshCache();
        return ApiResponse.success();
    }

    @Operation(summary = "批量更新配置")
    @PutMapping("/batch")
    public ApiResponse<Void> batchUpdateConfigs(@RequestBody List<GlobalConfig> configs) {
        configManagementService.batchUpdateConfigs(configs);
        return ApiResponse.success();
    }

    @Operation(summary = "获取类型的全部配置为Map")
    @GetMapping("/type/{configType}/map")
    public ApiResponse<Map<String, String>> getConfigMap(@PathVariable String configType) {
        return ApiResponse.success(configManagementService.getAllConfigsByType(configType));
    }
}