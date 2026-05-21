package com.zhuji.userorg.service.impl;

import com.zhuji.userorg.entity.ConfigHistory;
import com.zhuji.userorg.mapper.ConfigHistoryMapper;
import com.zhuji.userorg.service.ConfigHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigHistoryServiceImpl implements ConfigHistoryService {

    private final ConfigHistoryMapper configHistoryMapper;

    public ConfigHistoryServiceImpl(ConfigHistoryMapper configHistoryMapper) {
        this.configHistoryMapper = configHistoryMapper;
    }

    @Override
    public void saveConfigHistory(Long configId, Long userId, String configKey, String configValue,
                                 String configType, String operation, String operator) {
        ConfigHistory history = new ConfigHistory();
        history.setConfigId(configId);
        history.setUserId(userId);
        history.setConfigKey(configKey);
        history.setConfigValue(configValue);
        history.setConfigType(configType);
        history.setOperation(operation);
        history.setOperator(operator);
        configHistoryMapper.insert(history);
    }

    @Override
    public List<ConfigHistory> getConfigHistory(Long configId) {
        return configHistoryMapper.selectByConfigId(configId);
    }

    @Override
    public List<ConfigHistory> getConfigHistoryByUserId(Long userId) {
        return configHistoryMapper.selectByUserId(userId);
    }
}
