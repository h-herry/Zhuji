package com.zhuji.common.core.enums;

public enum ErrorCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    USER_ALREADY_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    EMAIL_ALREADY_EXISTS(1003, "邮箱已存在"),
    PHONE_ALREADY_EXISTS(1004, "手机号已存在"),
    WRONG_PASSWORD(1005, "原密码错误"),
    USER_DISABLED(1006, "用户已被禁用"),

    ROLE_ALREADY_EXISTS(2001, "角色已存在"),
    ROLE_NOT_FOUND(2002, "角色不存在"),
    ROLE_HAS_USERS(2003, "角色下有关联用户，无法删除"),

    ORG_NOT_FOUND(3001, "组织不存在"),
    ORG_HAS_CHILDREN(3002, "组织下有子组织，无法删除"),

    NOTIFICATION_SEND_FAILED(4001, "通知发送失败"),
    NOTIFICATION_TYPE_NOT_SUPPORTED(4002, "不支持的通知类型"),

    FILE_UPLOAD_FAILED(5001, "文件上传失败"),
    FILE_NOT_FOUND(5002, "文件不存在"),
    FILE_SIZE_EXCEEDED(5003, "文件大小超出限制"),

    WORKFLOW_DEFINITION_NOT_FOUND(6001, "流程定义不存在"),
    WORKFLOW_INSTANCE_NOT_FOUND(6002, "流程实例不存在"),
    WORKFLOW_TASK_NOT_FOUND(6003, "任务不存在"),

    THIRD_PARTY_CALL_FAILED(7001, "第三方接口调用失败"),

    SYSTEM_PARAM_NOT_FOUND(8001, "系统参数不存在"),
    SYSTEM_PARAM_ALREADY_EXISTS(8002, "系统参数已存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}