package com.zhuji.userorg.event;

import org.springframework.context.ApplicationEvent;

public class ConfigChangeEvent extends ApplicationEvent {
    private final String configKey;

    public ConfigChangeEvent(Object source, String configKey) {
        super(source);
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }
}
