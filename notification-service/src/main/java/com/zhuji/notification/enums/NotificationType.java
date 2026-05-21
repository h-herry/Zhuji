package com.zhuji.notification.enums;

public enum NotificationType {
    EMAIL("email", "邮件"),
    SMS("sms", "短信"),
    PUSH("push", "推送"),
    WECHAT("wechat", "微信");

    private final String code;
    private final String desc;

    NotificationType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}