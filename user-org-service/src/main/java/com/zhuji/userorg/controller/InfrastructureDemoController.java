package com.zhuji.userorg.controller;

import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.userorg.service.InfrastructureDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "基础设施示例", description = "展示分布式锁、限流、消息队列、分布式事务等功能")
@RestController
@RequestMapping("/api/v1/demo")
public class InfrastructureDemoController {

    private final InfrastructureDemoService demoService;

    public InfrastructureDemoController(InfrastructureDemoService demoService) {
        this.demoService = demoService;
    }

    @Operation(summary = "分布式锁示例 - 注解方式")
    @PutMapping("/lock/annotation")
    public ApiResponse<Void> updateWithLock(
            @RequestParam Long userId,
            @RequestParam String username) {
        demoService.updateUserWithLock(userId, username);
        return ApiResponse.success();
    }

    @Operation(summary = "分布式锁示例 - 手动方式")
    @PutMapping("/lock/manual")
    public ApiResponse<Void> updateWithManualLock(@RequestParam Long userId) {
        demoService.updateUserWithManualLock(userId);
        return ApiResponse.success();
    }

    @Operation(summary = "Sentinel限流示例")
    @GetMapping("/sentinel/{userId}")
    public ApiResponse<String> testSentinel(@PathVariable Long userId) {
        String result = demoService.getUserById(userId);
        return ApiResponse.success(result);
    }

    @Operation(summary = "发送消息到队列")
    @PostMapping("/mq/send")
    public ApiResponse<Void> sendMessage(
            @RequestParam String topic,
            @RequestParam String message) {
        demoService.sendMessageToQueue(topic, message);
        return ApiResponse.success();
    }

    @Operation(summary = "发送延迟消息")
    @PostMapping("/mq/delay")
    public ApiResponse<Void> sendDelayMessage(
            @RequestParam String topic,
            @RequestParam String message,
            @RequestParam Long delayMs) {
        demoService.sendDelayMessage(topic, message, delayMs);
        return ApiResponse.success();
    }

    @Operation(summary = "分布式事务示例")
    @PostMapping("/transaction")
    public ApiResponse<Void> testTransaction(
            @RequestParam Long userId,
            @RequestParam Long orgId) {
        demoService.createUserAndOrgWithTransaction(userId, orgId);
        return ApiResponse.success();
    }
}