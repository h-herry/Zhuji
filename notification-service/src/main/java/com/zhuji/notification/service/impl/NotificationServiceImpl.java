package com.zhuji.notification.service.impl;

import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.notification.entity.Notification;
import com.zhuji.notification.enums.NotificationType;
import com.zhuji.notification.mapper.NotificationMapper;
import com.zhuji.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final JavaMailSender mailSender;
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(JavaMailSender mailSender, NotificationMapper notificationMapper) {
        this.mailSender = mailSender;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);

            saveNotification(NotificationType.EMAIL, to, subject, content, 1);
            log.info("邮件发送成功: to={}, subject={}", to, subject);
        } catch (Exception e) {
            saveNotification(NotificationType.EMAIL, to, subject, content, 0);
            log.error("邮件发送失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendSms(String phone, String content) {
        try {
            log.info("短信发送成功: phone={}, content={}", phone, content);
            saveNotification(NotificationType.SMS, phone, I18nMessageUtil.getMessage("notification.type.sms"), content, 1);
        } catch (Exception e) {
            saveNotification(NotificationType.SMS, phone, I18nMessageUtil.getMessage("notification.type.sms"), content, 0);
            log.error("短信发送失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendPush(Long userId, String content) {
        try {
            log.info("推送发送成功: userId={}, content={}", userId, content);
            saveNotification(NotificationType.PUSH, userId.toString(), I18nMessageUtil.getMessage("notification.type.push"), content, 1);
        } catch (Exception e) {
            saveNotification(NotificationType.PUSH, userId.toString(), I18nMessageUtil.getMessage("notification.type.push"), content, 0);
            log.error("推送发送失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendWechat(String openId, String content) {
        try {
            log.info("微信消息发送成功: openId={}, content={}", openId, content);
            saveNotification(NotificationType.WECHAT, openId, I18nMessageUtil.getMessage("notification.type.wechat"), content, 1);
        } catch (Exception e) {
            saveNotification(NotificationType.WECHAT, openId, I18nMessageUtil.getMessage("notification.type.wechat"), content, 0);
            log.error("微信消息发送失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void send(NotificationType type, String target, String title, String content) {
        switch (type) {
            case EMAIL:
                sendEmail(target, title, content);
                break;
            case SMS:
                sendSms(target, content);
                break;
            case PUSH:
                sendPush(Long.parseLong(target), content);
                break;
            case WECHAT:
                sendWechat(target, content);
                break;
            default:
                log.warn("未知通知类型: {}", type);
        }
    }

    private void saveNotification(NotificationType type, String target, String title, String content, int status) {
        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .type(type.getCode())
                .target(target)
                .status(status)
                .sendTime(LocalDateTime.now())
                .build();
        notificationMapper.insert(notification);
    }
}