package com.zhuji.userorg.service.impl;

import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.entity.ConfigHistory;
import com.zhuji.userorg.entity.UserConfig;
import com.zhuji.userorg.mapper.ConfigHistoryMapper;
import com.zhuji.userorg.mapper.UserConfigMapper;
import com.zhuji.userorg.service.ConfigEncryptionService;
import com.zhuji.userorg.service.ConfigHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfigHistoryServiceImpl implements ConfigHistoryService {

    private final ConfigHistoryMapper configHistoryMapper;
    private final UserConfigMapper userConfigMapper;
    private final ConfigEncryptionService configEncryptionService;

    public ConfigHistoryServiceImpl(ConfigHistoryMapper configHistoryMapper,
                                   UserConfigMapper userConfigMapper,
                                   ConfigEncryptionService configEncryptionService) {
        this.configHistoryMapper = configHistoryMapper;
        this.userConfigMapper = userConfigMapper;
        this.configEncryptionService = configEncryptionService;
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
        history.setVersion(getNextVersion(configId));
        configHistoryMapper.insert(history);
    }

    private Integer getNextVersion(Long configId) {
        Integer maxVersion = configHistoryMapper.selectMaxVersionByConfigId(configId);
        return maxVersion != null ? maxVersion + 1 : 1;
    }

    @Override
    public List<ConfigHistory> getConfigHistory(Long configId) {
        return configHistoryMapper.selectByConfigId(configId);
    }

    @Override
    public List<ConfigHistory> getConfigHistoryByUserId(Long userId) {
        return configHistoryMapper.selectByUserId(userId);
    }

    @Override
    public ConfigHistory getConfigHistoryByVersion(Long configId, Integer version) {
        return configHistoryMapper.selectByConfigIdAndVersion(configId, version);
    }

    @Override
    public List<ConfigHistory> getConfigHistoryByKey(Long userId, String configKey) {
        return configHistoryMapper.selectByUserIdAndConfigKey(userId, configKey);
    }

    @Override
    @Transactional
    public Object rollbackToVersion(Long configId, Integer version) {
        ConfigHistory targetVersion = configHistoryMapper.selectByConfigIdAndVersion(configId, version);
        if (targetVersion == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("config.version.not.found"));
        }

        UserConfig config = userConfigMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException(404, I18nMessageUtil.getMessage("config.not.found"));
        }

        String valueToSet = targetVersion.getConfigValue();
        if (configEncryptionService.isSensitive(targetVersion.getConfigKey())) {
            try {
                String decrypted = configEncryptionService.decrypt(targetVersion.getConfigValue());
                valueToSet = configEncryptionService.encrypt(decrypted);
            } catch (Exception e) {
            }
        }

        config.setConfigValue(valueToSet);
        userConfigMapper.updateById(config);

        saveConfigHistory(configId, config.getUserId(), config.getConfigKey(),
                          config.getConfigValue(), config.getConfigType(), "ROLLBACK", null);

        return config;
    }
}
