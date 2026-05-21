package com.zhuji.common.i18n.util;

import com.zhuji.common.i18n.provider.CompositeMessageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 多语言消息工具类
 * 支持从多个来源（数据库、properties文件）获取消息
 */
@Slf4j
@Component
public class I18nMessageUtil {

    private static CompositeMessageProvider messageProvider;

    @Autowired
    public void setMessageProvider(CompositeMessageProvider messageProvider) {
        I18nMessageUtil.messageProvider = messageProvider;
    }

    /**
     * 获取消息（使用当前语言环境）
     *
     * @param key 消息键
     * @return 消息内容，未找到返回key本身
     */
    public static String getMessage(String key) {
        return getMessage(key, LocaleContextHolder.getLocale());
    }

    /**
     * 获取消息（使用当前语言环境，带参数）
     *
     * @param key  消息键
     * @param args 参数
     * @return 消息内容，未找到返回key本身
     */
    public static String getMessage(String key, Object... args) {
        return getMessage(key, LocaleContextHolder.getLocale(), args);
    }

    /**
     * 获取消息（指定语言环境）
     *
     * @param key    消息键
     * @param locale 语言环境
     * @return 消息内容，未找到返回key本身
     */
    public static String getMessage(String key, Locale locale) {
        try {
            String message = messageProvider.getMessage(key, locale);
            return message != null ? message : key;
        } catch (Exception e) {
            log.warn("获取消息失败: key={}, locale={}", key, locale, e);
            return key;
        }
    }

    /**
     * 获取消息（指定语言环境，带参数）
     *
     * @param key    消息键
     * @param locale 语言环境
     * @param args   参数
     * @return 消息内容，未找到返回key本身
     */
    public static String getMessage(String key, Locale locale, Object... args) {
        try {
            String message = messageProvider.getMessage(key, locale, args);
            return message != null ? message : key;
        } catch (Exception e) {
            log.warn("获取消息失败: key={}, locale={}", key, locale, e);
            return key;
        }
    }

    /**
     * 获取消息（指定语言代码）
     *
     * @param key     消息键
     * @param langCode 语言代码：zh_CN, en_US等
     * @return 消息内容，未找到返回key本身
     */
    public static String getMessageByLanguage(String key, String langCode) {
        Locale locale = parseLocale(langCode);
        return getMessage(key, locale);
    }

    /**
     * 获取消息（指定语言代码，带参数）
     *
     * @param key     消息键
     * @param langCode 语言代码：zh_CN, en_US等
     * @param args   参数
     * @return 消息内容，未找到返回key本身
     */
    public static String getMessageByLanguage(String key, String langCode, Object... args) {
        Locale locale = parseLocale(langCode);
        return getMessage(key, locale, args);
    }

    /**
     * 刷新消息缓存
     */
    public static void refreshCache() {
        try {
            messageProvider.refreshCache();
            log.info("消息缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新消息缓存失败", e);
        }
    }

    /**
     * 解析语言代码为Locale对象
     *
     * @param langCode 语言代码
     * @return Locale对象
     */
    private static Locale parseLocale(String langCode) {
        if (langCode == null || langCode.isEmpty()) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        return switch (langCode.toLowerCase()) {
            case "en", "en_us", "english" -> Locale.ENGLISH;
            case "ja", "ja_jp", "japanese" -> Locale.JAPANESE;
            case "ko", "ko_kr", "korean" -> Locale.KOREAN;
            case "zh", "zh_cn", "chinese" -> Locale.SIMPLIFIED_CHINESE;
            case "zh_tw", "zh_hk" -> Locale.TRADITIONAL_CHINESE;
            default -> {
                if (langCode.contains("_")) {
                    String[] parts = langCode.split("_");
                    yield new Locale(parts[0], parts[1]);
                } else if (langCode.contains("-")) {
                    String[] parts = langCode.split("-");
                    yield new Locale(parts[0], parts[1]);
                }
                yield new Locale(langCode);
            }
        };
    }
}
