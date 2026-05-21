package com.zhuji.notification.service;

import com.zhuji.notification.enums.NotificationType;

public interface NotificationService {
    void sendEmail(String to, String subject, String content);
    void sendSms(String phone, String content);
    void sendPush(Long userId, String content);
    void sendWechat(String openId, String content);
    void send(NotificationType type, String target, String title, String content);
}