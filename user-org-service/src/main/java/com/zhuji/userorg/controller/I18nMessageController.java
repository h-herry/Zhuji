package com.zhuji.userorg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.userorg.entity.I18nMessage;
import com.zhuji.userorg.service.I18nMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "多语言消息管理", description = "多语言消息的增删改查接口")
@RestController
@RequestMapping("/api/v1/i18n/messages")
public class I18nMessageController {

    @Autowired
    private I18nMessageService i18nMessageService;

    @Operation(summary = "分页查询多语言消息")
    @GetMapping
    public ApiResponse<Page<I18nMessage>> pageMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String messageKey,
            @RequestParam(required = false) String locale,
            @RequestParam(required = false) String module) {
        return ApiResponse.success(i18nMessageService.pageMessages(page, size, messageKey, locale, module));
    }

    @Operation(summary = "获取单个多语言消息")
    @GetMapping("/{id}")
    public ApiResponse<I18nMessage> getMessage(@PathVariable Long id) {
        return ApiResponse.success(i18nMessageService.getById(id));
    }

    @Operation(summary = "获取某个语言的所有消息")
    @GetMapping("/locale/{locale}")
    public ApiResponse<Map<String, String>> getMessagesByLocale(@PathVariable String locale) {
        return ApiResponse.success(i18nMessageService.getMessagesByLocale(locale));
    }

    @Operation(summary = "获取某个语言和模块的消息")
    @GetMapping("/locale/{locale}/module/{module}")
    public ApiResponse<Map<String, String>> getMessagesByModule(
            @PathVariable String locale,
            @PathVariable String module) {
        return ApiResponse.success(i18nMessageService.getMessagesByModule(locale, module));
    }

    @Operation(summary = "创建或更新多语言消息")
    @PostMapping
    public ApiResponse<Boolean> saveOrUpdateMessage(@RequestBody I18nMessage message) {
        return ApiResponse.success(i18nMessageService.saveOrUpdateMessage(message));
    }

    @Operation(summary = "批量创建多语言消息")
    @PostMapping("/batch")
    public ApiResponse<Boolean> batchSaveMessages(@RequestBody List<I18nMessage> messages) {
        for (I18nMessage message : messages) {
            i18nMessageService.saveOrUpdateMessage(message);
        }
        return ApiResponse.success(true);
    }

    @Operation(summary = "删除多语言消息")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteMessage(@PathVariable Long id) {
        return ApiResponse.success(i18nMessageService.deleteMessage(id));
    }

    @Operation(summary = "批量删除多语言消息")
    @DeleteMapping("/batch")
    public ApiResponse<Boolean> batchDeleteMessages(@RequestBody List<Long> ids) {
        return ApiResponse.success(i18nMessageService.batchDeleteMessages(ids));
    }

    @Operation(summary = "启用/禁用多语言消息")
    @PutMapping("/{id}/toggle")
    public ApiResponse<Boolean> toggleMessage(
            @PathVariable Long id,
            @RequestParam Integer isActive) {
        return ApiResponse.success(i18nMessageService.toggleMessage(id, isActive));
    }

    @Operation(summary = "刷新多语言消息缓存")
    @PostMapping("/refresh")
    public ApiResponse<Void> refreshCache() {
        i18nMessageService.refreshMessageCache();
        return ApiResponse.success();
    }

    @Operation(summary = "获取消息（用于测试）")
    @GetMapping("/test")
    public ApiResponse<String> getMessage(
            @RequestParam String messageKey,
            @RequestParam(required = false) String locale) {
        return ApiResponse.success(i18nMessageService.getMessage(messageKey, locale));
    }
}
