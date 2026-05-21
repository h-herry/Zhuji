package com.zhuji.common.i18n.provider;

import java.util.Locale;

/**
 * 消息提供器接口
 * 用于支持多种消息来源（数据库、properties文件等）
 */
public interface MessageProvider {

    /**
     * 获取消息
     *
     * @param key    消息键
     * @param locale 语言环境
     * @return 消息内容，未找到返回null
     */
    String getMessage(String key, Locale locale);

    /**
     * 获取消息（带参数）
     *
     * @param key    消息键
     * @param locale 语言环境
     * @param args   参数
     * @return 消息内容，未找到返回null
     */
    String getMessage(String key, Locale locale, Object... args);

    /**
     * 刷新消息缓存
     */
    void refreshCache();

    /**
     * 获取优先级
     *
     * @return 优先级，数字越小优先级越高
     */
    int getPriority();
}
