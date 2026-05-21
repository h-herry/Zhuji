package com.zhuji.userorg.service;

import com.zhuji.userorg.entity.ConfigHistory;

import java.util.List;

public interface ConfigHistoryService {

    void saveConfigHistory(Long configId, Long userId, String configKey, String configValue,
                          String configType, String operation, String operator);

    List<ConfigHistory> getConfigHistory(Long configId);

    List<ConfigHistory> getConfigHistoryByUserId(Long userId);
}
