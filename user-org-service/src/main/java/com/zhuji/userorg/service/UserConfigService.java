package com.zhuji.userorg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.userorg.entity.UserConfig;
import com.zhuji.userorg.entity.UserOrgRelation;

import java.util.List;
import java.util.Map;

public interface UserConfigService {

    Page<UserConfig> pageUserConfigs(int page, int size, Long userId, String configType);

    List<UserConfig> getUserConfigs(Long userId);

    List<UserConfig> getUserConfigsByType(Long userId, String configType);

    UserConfig getUserConfigByKey(Long userId, String configKey);

    UserConfig createUserConfig(UserConfig config);

    UserConfig updateUserConfig(Long id, UserConfig config);

    void deleteUserConfig(Long id);

    void batchAssignRoles(Long userId, List<Long> roleIds, String isPrimary);

    void batchAssignOrgs(Long userId, List<Long> orgIds, String isPrimary);

    List<Long> getUserRoleIds(Long userId);

    List<Long> getUserOrgIds(Long userId);

    List<Map<String, Object>> getUserRoles(Long userId);

    List<Map<String, Object>> getUserOrgs(Long userId);

    void removeUserRole(Long userId, Long roleId);

    void removeUserOrg(Long userId, Long orgId);

    void setPrimaryRole(Long userId, Long roleId);

    void setPrimaryOrg(Long userId, Long orgId);
}