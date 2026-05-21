package com.zhuji.userorg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.config.validator.ConfigValidator;
import com.zhuji.userorg.entity.*;
import com.zhuji.userorg.event.ConfigChangeEvent;
import com.zhuji.userorg.event.ConfigChangePublisher;
import com.zhuji.userorg.mapper.*;
import com.zhuji.userorg.service.ConfigEncryptionService;
import com.zhuji.userorg.service.ConfigHistoryService;
import com.zhuji.userorg.service.UserConfigService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserConfigServiceImpl implements UserConfigService {

    private final UserConfigMapper userConfigMapper;
    private final UserRoleRelationMapper userRoleRelationMapper;
    private final UserOrgRelationMapper userOrgRelationMapper;
    private final RoleMapper roleMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final List<ConfigValidator> configValidators;
    private final ApplicationEventPublisher eventPublisher;
    private final ConfigHistoryService configHistoryService;
    private final ConfigChangePublisher configChangePublisher;
    private final ConfigEncryptionService configEncryptionService;

    public UserConfigServiceImpl(UserConfigMapper userConfigMapper,
                               UserRoleRelationMapper userRoleRelationMapper,
                               UserOrgRelationMapper userOrgRelationMapper,
                               RoleMapper roleMapper,
                               OrgUnitMapper orgUnitMapper,
                               List<ConfigValidator> configValidators,
                               ApplicationEventPublisher eventPublisher,
                               ConfigHistoryService configHistoryService,
                               ConfigChangePublisher configChangePublisher,
                               ConfigEncryptionService configEncryptionService) {
        this.userConfigMapper = userConfigMapper;
        this.userRoleRelationMapper = userRoleRelationMapper;
        this.userOrgRelationMapper = userOrgRelationMapper;
        this.roleMapper = roleMapper;
        this.orgUnitMapper = orgUnitMapper;
        this.configValidators = configValidators;
        this.eventPublisher = eventPublisher;
        this.configHistoryService = configHistoryService;
        this.configChangePublisher = configChangePublisher;
        this.configEncryptionService = configEncryptionService;
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
    @Cacheable(value = "user-config", key = "#userId")
    public List<UserConfig> getUserConfigs(Long userId) {
        return userConfigMapper.selectByUserId(userId);
    }

    @Override
    @Cacheable(value = "user-config", key = "#userId + ':' + #configType")
    public List<UserConfig> getUserConfigsByType(Long userId, String configType) {
        return userConfigMapper.selectByUserIdAndType(userId, configType);
    }

    @Override
    @Cacheable(value = "user-config", key = "#userId + ':' + #configKey")
    public UserConfig getUserConfigByKey(Long userId, String configKey) {
        UserConfig config = userConfigMapper.selectByUserIdAndKey(userId, configKey);

        if (config != null && configEncryptionService.isSensitive(configKey)) {
            try {
                config.setConfigValue(configEncryptionService.decrypt(config.getConfigValue()));
            } catch (Exception e) {
            }
        }

        return config;
    }

    @Override
    @CacheEvict(value = "user-config", key = "#config.userId", allEntries = true)
    @Transactional
    public UserConfig createUserConfig(UserConfig config) {
        validateConfig(config.getConfigKey(), config.getConfigValue());
        UserConfig existing = userConfigMapper.selectByUserIdAndKey(config.getUserId(), config.getConfigKey());
        if (existing != null) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("validation.unique", "配置键"));
        }

        if (configEncryptionService.isSensitive(config.getConfigKey())) {
            config.setConfigValue(configEncryptionService.encrypt(config.getConfigValue()));
        }

        userConfigMapper.insert(config);

        configHistoryService.saveConfigHistory(
            config.getId(),
            config.getUserId(),
            config.getConfigKey(),
            config.getConfigValue(),
            config.getConfigType(),
            "CREATE",
            null
        );

        eventPublisher.publishEvent(new ConfigChangeEvent(this, config.getConfigKey()));
        configChangePublisher.publishConfigChange(config.getUserId(), config.getConfigKey(), "CREATE");

        return config;
    }

    @Override
    @CacheEvict(value = "user-config", key = "#userId", allEntries = true)
    @Transactional
    public UserConfig updateUserConfig(Long id, UserConfig config) {
        validateConfig(config.getConfigKey(), config.getConfigValue());
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

        if (configEncryptionService.isSensitive(config.getConfigKey())) {
            config.setConfigValue(configEncryptionService.encrypt(config.getConfigValue()));
        }

        config.setId(id);
        config.setUpdateTime(LocalDateTime.now());
        userConfigMapper.updateById(config);

        configHistoryService.saveConfigHistory(
            config.getId(),
            config.getUserId(),
            config.getConfigKey(),
            config.getConfigValue(),
            config.getConfigType(),
            "UPDATE",
            null
        );

        eventPublisher.publishEvent(new ConfigChangeEvent(this, config.getConfigKey()));
        configChangePublisher.publishConfigChange(config.getUserId(), config.getConfigKey(), "UPDATE");

        return config;
    }

    @Override
    @CacheEvict(value = "user-config", allEntries = true)
    @Transactional
    public void deleteUserConfig(Long id) {
        UserConfig config = userConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("user.config.not.found"));
        }
        userConfigMapper.deleteById(id);

        configHistoryService.saveConfigHistory(
            config.getId(),
            config.getUserId(),
            config.getConfigKey(),
            config.getConfigValue(),
            config.getConfigType(),
            "DELETE",
            null
        );

        eventPublisher.publishEvent(new ConfigChangeEvent(this, config.getConfigKey()));
        configChangePublisher.publishConfigChange(config.getUserId(), config.getConfigKey(), "DELETE");
    }

    private void validateConfig(String configKey, String configValue) {
        for (ConfigValidator validator : configValidators) {
            if (validator.supports(configKey)) {
                validator.validate(configKey, configValue);
            }
        }
    }

    @Override
    @Transactional
    public void batchAssignRoles(Long userId, List<Long> roleIds, String isPrimary) {
        List<UserRoleRelation> existingRelations = userRoleRelationMapper.selectList(
            new LambdaQueryWrapper<UserRoleRelation>()
                .eq(UserRoleRelation::getUserId, userId)
        );

        Set<Long> existingRoleIds = existingRelations.stream()
            .map(UserRoleRelation::getRoleId)
            .collect(Collectors.toSet());

        List<Long> rolesToAdd = roleIds.stream()
            .filter(roleId -> !existingRoleIds.contains(roleId))
            .collect(Collectors.toList());

        List<Long> rolesToRemove = existingRoleIds.stream()
            .filter(roleId -> !roleIds.contains(roleId))
            .collect(Collectors.toList());

        if (!rolesToRemove.isEmpty()) {
            userRoleRelationMapper.delete(
                new LambdaQueryWrapper<UserRoleRelation>()
                    .eq(UserRoleRelation::getUserId, userId)
                    .in(UserRoleRelation::getRoleId, rolesToRemove)
            );
        }

        if (!rolesToAdd.isEmpty()) {
            List<UserRoleRelation> newRelations = new ArrayList<>();
            for (int i = 0; i < rolesToAdd.size(); i++) {
                Long roleId = rolesToAdd.get(i);

                Role role = roleMapper.selectById(roleId);
                if (role == null) {
                    throw new BusinessException(404, I18nMessageUtil.getMessage("role.not.found"));
                }

                UserRoleRelation relation = new UserRoleRelation();
                relation.setUserId(userId);
                relation.setRoleId(roleId);
                relation.setIsPrimary(("1".equals(isPrimary) && i == 0) ? "1" : "0");
                relation.setCreateTime(LocalDateTime.now());
                newRelations.add(relation);
            }

            userRoleRelationMapper.batchInsert(newRelations);
        }
    }

    @Override
    @Transactional
    public void batchAssignOrgs(Long userId, List<Long> orgIds, String isPrimary) {
        List<UserOrgRelation> existingRelations = userOrgRelationMapper.selectList(
            new LambdaQueryWrapper<UserOrgRelation>()
                .eq(UserOrgRelation::getUserId, userId)
        );

        Set<Long> existingOrgIds = existingRelations.stream()
            .map(UserOrgRelation::getOrgId)
            .collect(Collectors.toSet());

        List<Long> orgsToAdd = orgIds.stream()
            .filter(orgId -> !existingOrgIds.contains(orgId))
            .collect(Collectors.toList());

        List<Long> orgsToRemove = existingOrgIds.stream()
            .filter(orgId -> !orgIds.contains(orgId))
            .collect(Collectors.toList());

        if (!orgsToRemove.isEmpty()) {
            userOrgRelationMapper.delete(
                new LambdaQueryWrapper<UserOrgRelation>()
                    .eq(UserOrgRelation::getUserId, userId)
                    .in(UserOrgRelation::getOrgId, orgsToRemove)
            );
        }

        if (!orgsToAdd.isEmpty()) {
            List<UserOrgRelation> newRelations = new ArrayList<>();
            for (int i = 0; i < orgsToAdd.size(); i++) {
                Long orgId = orgsToAdd.get(i);

                OrgUnit org = orgUnitMapper.selectById(orgId);
                if (org == null) {
                    throw new BusinessException(404, I18nMessageUtil.getMessage("org.not.found"));
                }

                UserOrgRelation relation = new UserOrgRelation();
                relation.setUserId(userId);
                relation.setOrgId(orgId);
                relation.setRelationType("MEMBER");
                relation.setIsPrimary(("1".equals(isPrimary) && i == 0) ? "1" : "0");
                relation.setCreateTime(LocalDateTime.now());
                newRelations.add(relation);
            }

            userOrgRelationMapper.batchInsert(newRelations);
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
        List<UserRoleRelation> relations = userRoleRelationMapper.selectByUserId(userId);

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> roleIds = relations.stream()
            .map(UserRoleRelation::getRoleId)
            .collect(Collectors.toSet());
        List<Role> roles = roleMapper.selectBatchIds(roleIds);

        Map<Long, Role> roleMap = roles.stream()
            .collect(Collectors.toMap(Role::getId, r -> r));

        UserRoleRelation primaryRelation = userRoleRelationMapper.selectPrimaryByUserId(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserRoleRelation relation : relations) {
            Role role = roleMap.get(relation.getRoleId());
            if (role != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", role.getId());
                map.put("roleCode", role.getRoleCode());
                map.put("roleName", role.getRoleName());
                map.put("description", role.getDescription());
                map.put("isPrimary", primaryRelation != null && primaryRelation.getRoleId().equals(role.getId()) ? "1" : "0");
                result.add(map);
            }
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getUserOrgs(Long userId) {
        List<UserOrgRelation> relations = userOrgRelationMapper.selectByUserId(userId);

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> orgIds = relations.stream()
            .map(UserOrgRelation::getOrgId)
            .collect(Collectors.toSet());
        List<OrgUnit> orgs = orgUnitMapper.selectBatchIds(orgIds);

        Map<Long, OrgUnit> orgMap = orgs.stream()
            .collect(Collectors.toMap(OrgUnit::getId, o -> o));

        UserOrgRelation primaryRelation = userOrgRelationMapper.selectPrimaryByUserId(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserOrgRelation relation : relations) {
            OrgUnit org = orgMap.get(relation.getOrgId());
            if (org != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", org.getId());
                map.put("orgCode", org.getOrgCode());
                map.put("fullName", org.getFullName());
                map.put("shortName", org.getShortName());
                map.put("orgType", org.getOrgType());
                map.put("isPrimary", primaryRelation != null && primaryRelation.getOrgId().equals(org.getId()) ? "1" : "0");
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
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("role.not.found"));
        }

        List<UserRoleRelation> relations = userRoleRelationMapper.selectByUserId(userId);
        boolean hasRole = relations.stream()
            .anyMatch(r -> r.getRoleId().equals(roleId));

        if (!hasRole) {
            throw new BusinessException(400, "用户没有该角色");
        }

        userRoleRelationMapper.updatePrimaryByUserId(userId, roleId);
    }

    @Override
    @Transactional
    public void setPrimaryOrg(Long userId, Long orgId) {
        OrgUnit org = orgUnitMapper.selectById(orgId);
        if (org == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("org.not.found"));
        }

        List<UserOrgRelation> relations = userOrgRelationMapper.selectByUserId(userId);
        boolean hasOrg = relations.stream()
            .anyMatch(r -> r.getOrgId().equals(orgId));

        if (!hasOrg) {
            throw new BusinessException(400, "用户不属于该组织");
        }

        userOrgRelationMapper.updatePrimaryByUserId(userId, orgId);
    }
}
