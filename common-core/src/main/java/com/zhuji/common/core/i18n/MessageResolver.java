package com.zhuji.common.core.i18n;

public interface MessageResolver {

    String getMessage(String key);

    String getMessage(String key, Object... args);

    default String resolve(String key) {
        return getMessage(key);
    }

    default String resolve(String key, Object... args) {
        return getMessage(key, args);
    }
}