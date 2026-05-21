package com.zhuji.userorg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.entity.GlobalConfig;
import com.zhuji.userorg.mapper.GlobalConfigMapper;
import com.zhuji.userorg.service.ConfigManagementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfigManagementServiceImpl implements ConfigManagementService {

    private final GlobalConfigMapper globalConfigMapper;
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    public ConfigManagementServiceImpl(GlobalConfigMapper globalConfigMapper) {
        this.globalConfigMapper = globalConfigMapper;
        refreshCache();
    }

    @Override
    public Page<GlobalConfig> pageConfigs(int page, int size, String configType, String configKey) {
        Page<GlobalConfig> pageData = new Page<>(page, size);
        LambdaQueryWrapper<GlobalConfig> queryWrapper = new LambdaQueryWrapper<>();

        if (configType != null && !configType.isEmpty()) {
            queryWrapper.eq(GlobalConfig::getConfigType, configType);
        }
        if (configKey != null && !configKey.isEmpty()) {
            queryWrapper.like(GlobalConfig::getConfigKey, configKey);
        }

        queryWrapper.orderByAsc(GlobalConfig::getSortOrder);
        return globalConfigMapper.selectPage(pageData, queryWrapper);
    }

    @Override
    public GlobalConfig getConfigById(Long id) {
        GlobalConfig config = globalConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("config.not.found"));
        }
        return config;
    }

    @Override
    public GlobalConfig getConfigByKey(String configKey) {
        GlobalConfig config = globalConfigMapper.selectByKey(configKey);
        if (config == null) {
            return null;
        }
        return config;
    }

    @Override
    public List<GlobalConfig> getConfigsByType(String configType) {
        return globalConfigMapper.selectByType(configType);
    }

    @Override
    public GlobalConfig createConfig(GlobalConfig config) {
        GlobalConfig existing = globalConfigMapper.selectByKey(config.getConfigKey());
        if (existing != null) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("config.key.exists"));
        }

        globalConfigMapper.insert(config);
        refreshCache();
        return config;
    }

    @Override
    public GlobalConfig updateConfig(Long id, GlobalConfig config) {
        GlobalConfig existing = getConfigById(id);

        if (!existing.getConfigKey().equals(config.getConfigKey())) {
            GlobalConfig duplicate = globalConfigMapper.selectByKey(config.getConfigKey());
            if (duplicate != null) {
                throw new BusinessException(400, I18nMessageUtil.getMessage("config.key.exists"));
            }
        }

        config.setId(id);
        globalConfigMapper.updateById(config);
        refreshCache();
        return config;
    }

    @Override
    public void deleteConfig(Long id) {
        GlobalConfig config = getConfigById(id);
        globalConfigMapper.deleteById(id);
        refreshCache();
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        return configCache.getOrDefault(configKey, defaultValue);
    }

    @Override
    public Integer getConfigValueAsInt(String configKey, Integer defaultValue) {
        String value = configCache.get(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Boolean getConfigValueAsBoolean(String configKey, Boolean defaultValue) {
        String value = configCache.get(configKey);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    @Override
    public Map<String, String> getAllConfigsByType(String configType) {
        List<GlobalConfig> configs = globalConfigMapper.selectByType(configType);
        Map<String, String> result = new HashMap<>();
        for (GlobalConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    @Override
    public void refreshCache() {
        configCache.clear();
        List<GlobalConfig> configs = globalConfigMapper.selectActiveConfigs();
        for (GlobalConfig config : configs) {
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }
    }

    @Override
    public List<Map<String, Object>> getConfigTypes() {
        LambdaQueryWrapper<GlobalConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(GlobalConfig::getConfigType)
                    .groupBy(GlobalConfig::getConfigType)
                    .orderByAsc(GlobalConfig::getConfigType);

        List<GlobalConfig> configs = globalConfigMapper.selectList(queryWrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (GlobalConfig config : configs) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", config.getConfigType());
            result.add(item);
        }

        return result;
    }

    @Override
    public void batchUpdateConfigs(List<GlobalConfig> configs) {
        for (GlobalConfig config : configs) {
            GlobalConfig existing = globalConfigMapper.selectByKey(config.getConfigKey());
            if (existing != null) {
                existing.setConfigValue(config.getConfigValue());
                existing.setDescription(config.getDescription());
                globalConfigMapper.updateById(existing);
            } else {
                globalConfigMapper.insert(config);
            }
        }
        refreshCache();
    }
}