package com.zhuji.userorg.event;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConfigChangePublisher {

    private static final String CONFIG_CHANGE_CHANNEL = "config:change";

    private final RedisTemplate<String, Object> redisTemplate;

    public ConfigChangePublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publishConfigChange(Long userId, String configKey, String operation) {
        ConfigChangeMessage message = new ConfigChangeMessage(
            userId, configKey, operation, System.currentTimeMillis()
        );

        redisTemplate.convertAndSend(CONFIG_CHANGE_CHANNEL, message);
    }
}
