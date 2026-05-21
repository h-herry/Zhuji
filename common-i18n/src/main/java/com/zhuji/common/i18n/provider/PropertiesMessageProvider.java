package com.zhuji.common.i18n.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Properties文件消息提供器
 * 优先级最低
 */
@Slf4j
@Component
public class PropertiesMessageProvider implements MessageProvider {

    @Autowired
    private MessageSource messageSource;

    @Override
    public String getMessage(String key, Locale locale) {
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            log.debug("未找到properties消息: key={}, locale={}", key, locale);
            return null;
        }
    }

    @Override
    public String getMessage(String key, Locale locale, Object... args) {
        try {
            String message = getMessage(key, locale);
            if (message != null && args != null && args.length > 0) {
                return MessageFormat.format(message, args);
            }
            return message;
        } catch (Exception e) {
            log.debug("未找到properties消息: key={}, locale={}", key, locale);
            return null;
        }
    }

    @Override
    public void refreshCache() {
        log.info("Properties文件消息提供器无需刷新缓存");
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
