package com.zhuji.userorg.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfigChangeMessageListener implements MessageListener {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    public ConfigChangeMessageListener(CacheManager cacheManager,
                                       RedisTemplate<String, Object> redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            byte[] body = message.getBody();
            ConfigChangeMessage configMessage = (ConfigChangeMessage) redisTemplate.getValueSerializer().deserialize(body);

            if (configMessage == null) {
                log.warn("收到空的配置变更消息");
                return;
            }

            log.info("收到配置变更消息: userId={}, key={}, operation={}",
                     configMessage.getUserId(), configMessage.getConfigKey(), configMessage.getOperation());

            if (configMessage.getUserId() != null) {
                clearUserConfigCache(configMessage.getUserId(), configMessage.getConfigKey());
            } else {
                clearAllConfigCache();
            }
        } catch (Exception e) {
            log.error("处理配置变更消息失败", e);
        }
    }

    private void clearUserConfigCache(Long userId, String configKey) {
        var cache = cacheManager.getCache("user-config");
        if (cache != null) {
            cache.evict(userId);

            if (configKey != null) {
                cache.evict(userId + ":" + configKey);
            }
        }
    }

    private void clearAllConfigCache() {
        var cache = cacheManager.getCache("user-config");
        if (cache != null) {
            cache.clear();
        }
    }
}
