CREATE DATABASE IF NOT EXISTS zhuji_notification DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zhuji_notification;

CREATE TABLE IF NOT EXISTS sys_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(200) COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type VARCHAR(20) NOT NULL COMMENT '通知类型：email/sms/push/wechat',
    target VARCHAR(200) NOT NULL COMMENT '目标地址（邮箱/手机号/用户ID/OpenID）',
    user_id BIGINT COMMENT '用户ID',
    status TINYINT DEFAULT 0 COMMENT '发送状态：0-待发送，1-成功，2-失败',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    error_msg VARCHAR(500) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    send_time DATETIME COMMENT '发送时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';

CREATE TABLE IF NOT EXISTS sys_notification_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    template_code VARCHAR(50) NOT NULL UNIQUE COMMENT '模板编码',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    type VARCHAR(20) NOT NULL COMMENT '模板类型：email/sms/push/wechat',
    subject VARCHAR(200) COMMENT '邮件主题',
    content TEXT NOT NULL COMMENT '模板内容（支持变量替换）',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    description VARCHAR(500) COMMENT '模板描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    INDEX idx_template_code (template_code),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知模板表';

INSERT INTO sys_notification_template (template_code, template_name, type, subject, content, status, description) VALUES
('EMAIL_WELCOME', '欢迎邮件', 'email', '欢迎加入筑基科技', '尊敬的{{username}}，欢迎加入筑基科技！', 1, '新用户注册欢迎邮件'),
('SMS_VERIFY', '验证码短信', 'sms', '', '【筑基科技】您的验证码是{{code}}，有效期5分钟。', 1, '短信验证码模板'),
('PUSH_SYSTEM', '系统推送', 'push', '', '系统通知：{{message}}', 1, '系统推送模板'),
('WECHAT_NOTIFY', '微信通知', 'wechat', '', '{{content}}', 1, '微信消息模板');