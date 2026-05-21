package com.zhuji.userorg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.entity.*;
import com.zhuji.userorg.mapper.*;
import com.zhuji.userorg.service.UserConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserConfigServiceImpl implements UserConfigService {

    private final UserConfigMapper userConfigMapper;
    private final UserRoleRelationMapper userRoleRelationMapper;
    private final UserOrgRelationMapper userOrgRelationMapper;
    private final RoleMapper roleMapper;
    private final OrgUnitMapper orgUnitMapper;

    public UserConfigServiceImpl(UserConfigMapper userConfigMapper,
                               UserRoleRelationMapper userRoleRelationMapper,
                               UserOrgRelationMapper userOrgRelationMapper,
                               RoleMapper roleMapper,
                               OrgUnitMapper orgUnitMapper) {
        this.userConfigMapper = userConfigMapper;
        this.userRoleRelationMapper = userRoleRelationMapper;
        this.userOrgRelationMapper = userOrgRelationMapper;
        this.roleMapper = roleMapper;
        this.orgUnitMapper = orgUnitMapper;
    }

    @Override
    public Page<UserConfig> pageUserConfigs(int page, int size, Long userId, String configType) {
        Page<UserConfig> pageData = new Page<>(page, size);
        LambdaQueryWrapper<UserConfig> queryWrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            queryWrapper.eq(UserConfig::getUserId, userId);
        }
        if (configType != null && !configType.isEmpty()) {
            queryWrapper.eq(UserConfig::getConfigType, configType);
        }

        queryWrapper.orderByDesc(UserConfig::getCreateTime);
        return userConfigMapper.selectPage(pageData, queryWrapper);
    }

    @Override
    public List<UserConfig> getUserConfigs(Long userId) {
        return userConfigMapper.selectByUserId(userId);
    }

    @Override
    public List<UserConfig> getUserConfigsByType(Long userId, String configType) {
        return userConfigMapper.selectByUserIdAndType(userId, configType);
    }

    @Override
    public UserConfig getUserConfigByKey(Long userId, String configKey) {
        return userConfigMapper.selectByUserIdAndKey(userId, configKey);
    }

    @Override
    public UserConfig createUserConfig(UserConfig config) {
        UserConfig existing = userConfigMapper.selectByUserIdAndKey(config.getUserId(), config.getConfigKey());
        if (existing != null) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("validation.unique", "配置键"));
        }
        userConfigMapper.insert(config);
        return config;
    }

    @Override
    public UserConfig updateUserConfig(Long id, UserConfig config) {
        UserConfig existing = userConfigMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("user.config.not.found"));
        }

        if (!existing.getConfigKey().equals(config.getConfigKey())) {
            UserConfig duplicate = userConfigMapper.selectByUserIdAndKey(existing.getUserId(), config.getConfigKey());
            if (duplicate != null) {
                throw new BusinessException(400, I18nMessageUtil.getMessage("validation.unique", "配置键"));
            }
        }

        config.setId(id);
        config.setUpdateTime(LocalDateTime.now());
        userConfigMapper.updateById(config);
        return config;
    }

    @Override
    public void deleteUserConfig(Long id) {
        UserConfig config = userConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("user.config.not.found"));
        }
        userConfigMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchAssignRoles(Long userId, List<Long> roleIds, String isPrimary) {
        LambdaQueryWrapper<UserRoleRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRoleRelation::getUserId, userId);
        userRoleRelationMapper.delete(queryWrapper);

        for (int i = 0; i < roleIds.size(); i++) {
            Long roleId = roleIds.get(i);
            Role role = roleMapper.selectById(roleId);
            if (role == null) {
                throw new BusinessException(404, I18nMessageUtil.getMessage("role.not.found"));
            }

            UserRoleRelation relation = new UserRoleRelation();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relation.setIsPrimary((i == 0 && "1".equals(isPrimary)) ? "1" : "0");
            relation.setCreateTime(LocalDateTime.now());
            userRoleRelationMapper.insert(relation);
        }
    }

    @Override
    @Transactional
    public void batchAssignOrgs(Long userId, List<Long> orgIds, String isPrimary) {
        LambdaQueryWrapper<UserOrgRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserOrgRelation::getUserId, userId);
        userOrgRelationMapper.delete(queryWrapper);

        for (int i = 0; i < orgIds.size(); i++) {
            Long orgId = orgIds.get(i);
            OrgUnit org = orgUnitMapper.selectById(orgId);
            if (org == null) {
                throw new BusinessException(404, I18nMessageUtil.getMessage("org.not.found"));
            }

            UserOrgRelation relation = new UserOrgRelation();
            relation.setUserId(userId);
            relation.setOrgId(orgId);
            relation.setIsPrimary((i == 0 && "1".equals(isPrimary)) ? "1" : "0");
            relation.setCreateTime(LocalDateTime.now());
            userOrgRelationMapper.insert(relation);
        }
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return userRoleRelationMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public List<Long> getUserOrgIds(Long userId) {
        return userOrgRelationMapper.selectOrgIdsByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getUserRoles(Long userId) {
        List<Long> roleIds = userRoleRelationMapper.selectRoleIdsByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Long roleId : roleIds) {
            Role role = roleMapper.selectById(roleId);
            if (role != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", role.getId());
                map.put("roleCode", role.getRoleCode());
                map.put("roleName", role.getRoleName());
                map.put("description", role.getDescription());

                UserRoleRelation relation = userRoleRelationMapper.selectPrimaryByUserId(userId);
                map.put("isPrimary", relation != null && relation.getRoleId().equals(roleId) ? "1" : "0");

                result.add(map);
            }
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getUserOrgs(Long userId) {
        List<Long> orgIds = userOrgRelationMapper.selectOrgIdsByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Long orgId : orgIds) {
            OrgUnit org = orgUnitMapper.selectById(orgId);
            if (org != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", org.getId());
                map.put("orgCode", org.getOrgCode());
                map.put("fullName", org.getFullName());
                map.put("shortName", org.getShortName());
                map.put("orgType", org.getOrgType());

                UserOrgRelation relation = userOrgRelationMapper.selectPrimaryByUserId(userId);
                map.put("isPrimary", relation != null && relation.getOrgId().equals(orgId) ? "1" : "0");

                result.add(map);
            }
        }

        return result;
    }

    @Override
    @Transactional
    public void removeUserRole(Long userId, Long roleId) {
        LambdaQueryWrapper<UserRoleRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRoleRelation::getUserId, userId)
                    .eq(UserRoleRelation::getRoleId, roleId);
        userRoleRelationMapper.delete(queryWrapper);
    }

    @Override
    @Transactional
    public void removeUserOrg(Long userId, Long orgId) {
        LambdaQueryWrapper<UserOrgRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserOrgRelation::getUserId, userId)
                    .eq(UserOrgRelation::getOrgId, orgId);
        userOrgRelationMapper.delete(queryWrapper);
    }

    @Override
    @Transactional
    public void setPrimaryRole(Long userId, Long roleId) {
        List<UserRoleRelation> relations = userRoleRelationMapper.selectByUserId(userId);
        for (UserRoleRelation relation : relations) {
            if (relation.getRoleId().equals(roleId)) {
                relation.setIsPrimary("1");
            } else {
                relation.setIsPrimary("0");
            }
            userRoleRelationMapper.updateById(relation);
        }
    }

    @Override
    @Transactional
    public void setPrimaryOrg(Long userId, Long orgId) {
        List<UserOrgRelation> relations = userOrgRelationMapper.selectByUserId(userId);
        for (UserOrgRelation relation : relations) {
            if (relation.getOrgId().equals(orgId)) {
                relation.setIsPrimary("1");
            } else {
                relation.setIsPrimary("0");
            }
            userOrgRelationMapper.updateById(relation);
        }
    }
}