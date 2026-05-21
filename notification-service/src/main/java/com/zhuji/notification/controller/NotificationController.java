package com.zhuji.notification.controller;

import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.notification.enums.NotificationType;
import com.zhuji.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通知管理", description = "邮件、短信、推送通知接口")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "发送邮件")
    @PostMapping("/email")
    public ApiResponse<Void> sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content) {
        notificationService.sendEmail(to, subject, content);
        return ApiResponse.success();
    }

    @Operation(summary = "发送短信")
    @PostMapping("/sms")
    public ApiResponse<Void> sendSms(
            @RequestParam String phone,
            @RequestParam String content) {
        notificationService.sendSms(phone, content);
        return ApiResponse.success();
    }

    @Operation(summary = "发送推送")
    @PostMapping("/push")
    public ApiResponse<Void> sendPush(
            @RequestParam Long userId,
            @RequestParam String content) {
        notificationService.sendPush(userId, content);
        return ApiResponse.success();
    }

    @Operation(summary = "发送微信消息")
    @PostMapping("/wechat")
    public ApiResponse<Void> sendWechat(
            @RequestParam String openId,
            @RequestParam String content) {
        notificationService.sendWechat(openId, content);
        return ApiResponse.success();
    }

    @Operation(summary = "通用发送接口")
    @PostMapping("/send")
    public ApiResponse<Void> send(
            @RequestParam NotificationType type,
            @RequestParam String target,
            @RequestParam(required = false) String title,
            @RequestParam String content) {
        notificationService.send(type, target, title, content);
        return ApiResponse.success();
    }
}