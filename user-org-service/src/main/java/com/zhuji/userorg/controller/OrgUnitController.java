package com.zhuji.userorg.controller;

import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.userorg.dto.CreateOrgRequest;
import com.zhuji.userorg.entity.OrgType;
import com.zhuji.userorg.service.OrgTypeService;
import com.zhuji.userorg.service.OrgUnitService;
import com.zhuji.userorg.vo.OrgTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "组织架构管理", description = "组织架构CRUD接口")
@RestController
@RequestMapping("/api/v1/orgs")
public class OrgUnitController {

    private final OrgUnitService orgUnitService;
    private final OrgTypeService orgTypeService;

    public OrgUnitController(OrgUnitService orgUnitService, OrgTypeService orgTypeService) {
        this.orgUnitService = orgUnitService;
        this.orgTypeService = orgTypeService;
    }

    @Operation(summary = "创建组织")
    @PostMapping
    public ApiResponse<OrgTreeVO> createOrg(@Valid @RequestBody CreateOrgRequest request) {
        return ApiResponse.success(orgUnitService.createOrg(request));
    }

    @Operation(summary = "获取组织详情")
    @GetMapping("/{id}")
    public ApiResponse<OrgTreeVO> getOrg(@PathVariable Long id) {
        return ApiResponse.success(orgUnitService.getOrgById(id));
    }

    @Operation(summary = "获取组织架构树")
    @GetMapping("/tree")
    public ApiResponse<List<OrgTreeVO>> getOrgTree() {
        return ApiResponse.success(orgUnitService.getOrgTree());
    }

    @Operation(summary = "获取子组织列表")
    @GetMapping("/{parentId}/children")
    public ApiResponse<List<OrgTreeVO>> getChildren(@PathVariable Long parentId) {
        return ApiResponse.success(orgUnitService.getChildren(parentId));
    }

    @Operation(summary = "更新组织")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateOrg(@PathVariable Long id, @Valid @RequestBody CreateOrgRequest request) {
        orgUnitService.updateOrg(id, request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除组织")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrg(@PathVariable Long id) {
        orgUnitService.deleteOrg(id);
        return ApiResponse.success();
    }

    @Operation(summary = "获取组织类型列表")
    @GetMapping("/types")
    public ApiResponse<List<OrgTypeVO>> getOrgTypes() {
        List<OrgType> types = orgTypeService.getAllOrgTypes();
        List<OrgTypeVO> typeVOs = types.stream()
                .map(type -> OrgTypeVO.builder()
                        .code(type.getTypeCode())
                        .name(type.getTypeName())
                        .key(type.getTypeKey())
                        .description(type.getDescription())
                        .sort(type.getSort())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.success(typeVOs);
    }

    public static class OrgTypeVO {
        private Integer code;
        private String name;
        private String key;
        private String description;
        private Integer sort;

        public OrgTypeVO() {
        }

        public OrgTypeVO(Integer code, String name, String key, String description, Integer sort) {
            this.code = code;
            this.name = name;
            this.key = key;
            this.description = description;
            this.sort = sort;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Integer code;
            private String name;
            private String key;
            private String description;
            private Integer sort;

            public Builder code(Integer code) {
                this.code = code;
                return this;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder key(String key) {
                this.key = key;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder sort(Integer sort) {
                this.sort = sort;
                return this;
            }

            public OrgTypeVO build() {
                return new OrgTypeVO(code, name, key, description, sort);
            }
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getSort() {
            return sort;
        }

        public void setSort(Integer sort) {
            this.sort = sort;
        }
    }
}