package com.zhuji.system.controller;

import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.system.dto.ParamCategoryDTO;
import com.zhuji.system.entity.ParamCategory;
import com.zhuji.system.service.ParamCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "参数分类管理")
@RestController
@RequestMapping("/api/v1/param-categories")
public class ParamCategoryController {

    private final ParamCategoryService paramCategoryService;

    public ParamCategoryController(ParamCategoryService paramCategoryService) {
        this.paramCategoryService = paramCategoryService;
    }

    @Operation(summary = "获取所有分类")
    @GetMapping
    public ApiResponse<List<ParamCategory>> listAll() {
        return ApiResponse.success(paramCategoryService.listAll());
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public ApiResponse<ParamCategory> getById(@PathVariable Long id) {
        return ApiResponse.success(paramCategoryService.getById(id));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public ApiResponse<ParamCategory> create(@Valid @RequestBody ParamCategoryDTO dto) {
        return ApiResponse.success(paramCategoryService.create(dto));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public ApiResponse<ParamCategory> update(@PathVariable Long id,
                                            @Valid @RequestBody ParamCategoryDTO dto) {
        return ApiResponse.success(paramCategoryService.update(id, dto));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        paramCategoryService.delete(id);
        return ApiResponse.success(null);
    }
}