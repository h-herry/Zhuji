package com.zhuji.userorg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.userorg.entity.GlobalConfig;

import java.util.List;
import java.util.Map;

public interface ConfigManagementService {

    Page<GlobalConfig> pageConfigs(int page, int size, String configType, String configKey);

    GlobalConfig getConfigById(Long id);

    GlobalConfig getConfigByKey(String configKey);

    List<GlobalConfig> getConfigsByType(String configType);

    GlobalConfig createConfig(GlobalConfig config);

    GlobalConfig updateConfig(Long id, GlobalConfig config);

    void deleteConfig(Long id);

    String getConfigValue(String configKey, String defaultValue);

    Integer getConfigValueAsInt(String configKey, Integer defaultValue);

    Boolean getConfigValueAsBoolean(String configKey, Boolean defaultValue);

    Map<String, String> getAllConfigsByType(String configType);

    void refreshCache();

    List<Map<String, Object>> getConfigTypes();

    void batchUpdateConfigs(List<GlobalConfig> configs);
}