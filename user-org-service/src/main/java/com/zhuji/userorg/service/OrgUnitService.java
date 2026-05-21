package com.zhuji.userorg.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuji.common.core.enums.ErrorCode;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.dto.CreateOrgRequest;
import com.zhuji.userorg.entity.OrgType;
import com.zhuji.userorg.entity.OrgUnit;
import com.zhuji.userorg.mapper.OrgUnitMapper;
import com.zhuji.userorg.vo.OrgTreeVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrgUnitService extends ServiceImpl<OrgUnitMapper, OrgUnit> {

    private final OrgTypeService orgTypeService;

    public OrgUnitService(OrgTypeService orgTypeService) {
        this.orgTypeService = orgTypeService;
    }

    @CacheEvict(value = "org", allEntries = true)
    public OrgTreeVO createOrg(CreateOrgRequest request) {
        if (lambdaQuery().eq(OrgUnit::getOrgCode, request.getOrgCode()).exists()) {
            throw new BusinessException(409, I18nMessageUtil.getMessage("org.code.exists"));
        }

        OrgType orgType = orgTypeService.getByTypeCode(request.getOrgType());
        if (orgType == null) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("org.type.not.found"));
        }

        int level = 1;
        String path = "/" + System.currentTimeMillis();
        Long parentId = request.getParentId();

        if (parentId != null && parentId > 0) {
            OrgUnit parent = getById(parentId);
            if (parent == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), I18nMessageUtil.getMessage("org.not.found"));
            }
            level = parent.getLevel() + 1;
            path = parent.getPath() + "/" + System.currentTimeMillis();
        }

        OrgUnit orgUnit = OrgUnit.builder()
                .orgCode(request.getOrgCode())
                .fullName(request.getFullName())
                .shortName(request.getShortName() != null ? request.getShortName() : request.getFullName())
                .orgType(orgType.getTypeCode())
                .parentId(parentId != null ? parentId : 0L)
                .level(level)
                .path(path)
                .leaderId(request.getLeaderId())
                .leaderName(request.getLeaderName())
                .areaCode(request.getAreaCode())
                .costCenter(request.getCostCenter())
                .sort(request.getSort() != null ? request.getSort() : 0)
                .status(1)
                .isVirtual(request.getIsVirtual() != null ? request.getIsVirtual() : 0)
                .description(request.getDescription())
                .build();

        save(orgUnit);
        return convertToTreeVO(orgUnit);
    }

    @Cacheable(value = "org", key = "'tree'")
    public List<OrgTreeVO> getOrgTree() {
        List<OrgUnit> allOrgs = lambdaQuery()
                .orderByAsc(OrgUnit::getLevel)
                .orderByAsc(OrgUnit::getSort)
                .list();

        if (CollUtil.isEmpty(allOrgs)) {
            return new ArrayList<>();
        }

        Map<Long, List<OrgUnit>> parentMap = allOrgs.stream()
                .collect(Collectors.groupingBy(OrgUnit::getParentId));

        List<OrgTreeVO> rootList = allOrgs.stream()
                .filter(org -> org.getParentId() == null || org.getParentId() == 0)
                .map(this::convertToTreeVO)
                .collect(Collectors.toList());

        buildTree(rootList, parentMap);

        return rootList;
    }

    @Cacheable(value = "org", key = "'children:' + #parentId")
    public List<OrgTreeVO> getChildren(Long parentId) {
        List<OrgUnit> children = lambdaQuery()
                .eq(OrgUnit::getParentId, parentId)
                .orderByAsc(OrgUnit::getSort)
                .list();

        return children.stream()
                .map(this::convertToTreeVO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "org", key = "#id")
    public OrgTreeVO getOrgById(Long id) {
        OrgUnit orgUnit = getById(id);
        if (orgUnit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), I18nMessageUtil.getMessage("org.not.found"));
        }
        return convertToTreeVO(orgUnit);
    }

    @CacheEvict(value = "org", allEntries = true)
    public void updateOrg(Long id, CreateOrgRequest request) {
        OrgUnit orgUnit = getById(id);
        if (orgUnit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), I18nMessageUtil.getMessage("org.not.found"));
        }

        if (request.getFullName() != null) {
            orgUnit.setFullName(request.getFullName());
        }
        if (request.getShortName() != null) {
            orgUnit.setShortName(request.getShortName());
        }
        if (request.getLeaderId() != null) {
            orgUnit.setLeaderId(request.getLeaderId());
        }
        if (request.getLeaderName() != null) {
            orgUnit.setLeaderName(request.getLeaderName());
        }
        if (request.getAreaCode() != null) {
            orgUnit.setAreaCode(request.getAreaCode());
        }
        if (request.getCostCenter() != null) {
            orgUnit.setCostCenter(request.getCostCenter());
        }
        if (request.getSort() != null) {
            orgUnit.setSort(request.getSort());
        }
        if (request.getDescription() != null) {
            orgUnit.setDescription(request.getDescription());
        }

        updateById(orgUnit);
    }

    @CacheEvict(value = "org", allEntries = true)
    public void deleteOrg(Long id) {
        long childCount = lambdaQuery()
                .eq(OrgUnit::getParentId, id)
                .count();

        if (childCount > 0) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("org.has.children"));
        }

        removeById(id);
    }

    private void buildTree(List<OrgTreeVO> parentList, Map<Long, List<OrgUnit>> parentMap) {
        if (CollUtil.isEmpty(parentList)) {
            return;
        }

        for (OrgTreeVO parent : parentList) {
            List<OrgUnit> children = parentMap.get(parent.getId());
            if (CollUtil.isNotEmpty(children)) {
                List<OrgTreeVO> childVOList = children.stream()
                        .map(this::convertToTreeVO)
                        .collect(Collectors.toList());
                parent.setChildren(childVOList);
                buildTree(childVOList, parentMap);
            }
        }
    }

    private OrgTreeVO convertToTreeVO(OrgUnit orgUnit) {
        OrgType orgType = orgTypeService.getByTypeCode(orgUnit.getOrgType());
        String orgTypeName = orgType != null ? orgType.getTypeName() : I18nMessageUtil.getMessage("common.not.found");

        return OrgTreeVO.builder()
                .id(orgUnit.getId())
                .orgCode(orgUnit.getOrgCode())
                .fullName(orgUnit.getFullName())
                .shortName(orgUnit.getShortName())
                .orgType(orgUnit.getOrgType())
                .orgTypeName(orgTypeName)
                .parentId(orgUnit.getParentId())
                .level(orgUnit.getLevel())
                .path(orgUnit.getPath())
                .leaderId(orgUnit.getLeaderId())
                .leaderName(orgUnit.getLeaderName())
                .areaCode(orgUnit.getAreaCode())
                .costCenter(orgUnit.getCostCenter())
                .sort(orgUnit.getSort())
                .status(orgUnit.getStatus())
                .isVirtual(orgUnit.getIsVirtual())
                .description(orgUnit.getDescription())
                .createTime(orgUnit.getCreateTime())
                .children(new ArrayList<>())
                .build();
    }
}