package com.zhuji.common.i18n.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 组合消息提供器
 * 按照优先级顺序从多个消息提供器中查找消息
 */
@Slf4j
@Component
public class CompositeMessageProvider implements MessageProvider {

    private final List<MessageProvider> messageProviders;

    @Autowired
    public CompositeMessageProvider(List<MessageProvider> messageProviders) {
        this.messageProviders = messageProviders.stream()
                .sorted(Comparator.comparingInt(MessageProvider::getPriority))
                .toList();
        log.info("已加载 {} 个消息提供器", this.messageProviders.size());
    }

    @Override
    public String getMessage(String key, Locale locale) {
        for (MessageProvider provider : messageProviders) {
            String message = provider.getMessage(key, locale);
            if (message != null) {
                return message;
            }
        }
        return null;
    }

    @Override
    public String getMessage(String key, Locale locale, Object... args) {
        for (MessageProvider provider : messageProviders) {
            String message = provider.getMessage(key, locale, args);
            if (message != null) {
                return message;
            }
        }
        return null;
    }

    @Override
    public void refreshCache() {
        for (MessageProvider provider : messageProviders) {
            try {
                provider.refreshCache();
            } catch (Exception e) {
                log.error("刷新消息提供器缓存失败: {}", provider.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
