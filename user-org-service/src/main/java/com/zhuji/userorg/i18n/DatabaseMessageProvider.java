package com.zhuji.userorg.i18n;

import com.zhuji.common.i18n.provider.MessageProvider;
import com.zhuji.userorg.service.I18nMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * 数据库消息提供器
 * 优先级高于properties文件
 */
@Slf4j
@Component
public class DatabaseMessageProvider implements MessageProvider {

    @Autowired
    @Lazy
    private I18nMessageService i18nMessageService;

    @Override
    public String getMessage(String key, Locale locale) {
        try {
            String localeStr = locale.toString();
            return i18nMessageService.getMessage(key, localeStr);
        } catch (Exception e) {
            log.debug("未找到数据库消息: key={}, locale={}", key, locale, e);
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
            log.debug("未找到数据库消息: key={}, locale={}", key, locale, e);
            return null;
        }
    }

    @Override
    public void refreshCache() {
        try {
            i18nMessageService.refreshMessageCache();
        } catch (Exception e) {
            log.error("刷新数据库消息缓存失败", e);
        }
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
