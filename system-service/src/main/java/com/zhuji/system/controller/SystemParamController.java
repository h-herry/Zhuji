package com.zhuji.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.system.dto.SystemParamDTO;
import com.zhuji.system.entity.SystemParam;
import com.zhuji.system.service.SystemParamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统参数管理")
@RestController
@RequestMapping("/api/v1/system-params")
public class SystemParamController {

    private final SystemParamService systemParamService;

    public SystemParamController(SystemParamService systemParamService) {
        this.systemParamService = systemParamService;
    }

    @Operation(summary = "分页查询系统参数")
    @GetMapping("/page")
    public ApiResponse<Page<SystemParam>> page(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Long categoryId) {
        return ApiResponse.success(systemParamService.page(pageNum, pageSize, keyword, categoryId));
    }

    @Operation(summary = "获取所有参数")
    @GetMapping
    public ApiResponse<List<SystemParam>> listAll() {
        return ApiResponse.success(systemParamService.listAll());
    }

    @Operation(summary = "获取参数详情")
    @GetMapping("/{id}")
    public ApiResponse<SystemParam> getById(@PathVariable Long id) {
        return ApiResponse.success(systemParamService.getById(id));
    }

    @Operation(summary = "根据键获取值")
    @GetMapping("/key/{paramKey}")
    public ApiResponse<String> getValue(@PathVariable String paramKey) {
        return ApiResponse.success(systemParamService.getValue(paramKey));
    }

    @Operation(summary = "根据分类获取所有参数")
    @GetMapping("/category/{categoryId}")
    public ApiResponse<Map<String, String>> getByCategory(@PathVariable Long categoryId) {
        return ApiResponse.success(systemParamService.getParamsByCategory(categoryId));
    }

    @Operation(summary = "创建参数")
    @PostMapping
    public ApiResponse<SystemParam> create(@Valid @RequestBody SystemParamDTO dto) {
        return ApiResponse.success(systemParamService.create(dto));
    }

    @Operation(summary = "更新参数")
    @PutMapping("/{id}")
    public ApiResponse<SystemParam> update(@PathVariable Long id,
                                           @Valid @RequestBody SystemParamDTO dto) {
        return ApiResponse.success(systemParamService.update(id, dto));
    }

    @Operation(summary = "删除参数")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        systemParamService.delete(id);
        return ApiResponse.success(null);
    }
}