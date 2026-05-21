-- 初始化数据库脚本
-- 创建时间: 2024-01-01

-- 系统多语言消息表
CREATE TABLE IF NOT EXISTS sys_i18n_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    message_key VARCHAR(200) NOT NULL COMMENT '消息键',
    locale VARCHAR(20) NOT NULL COMMENT '语言区域',
    message_value TEXT NOT NULL COMMENT '消息内容',
    module VARCHAR(50) DEFAULT 'common' COMMENT '模块',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    UNIQUE KEY uk_key_locale (message_key, locale),
    KEY idx_locale (locale),
    KEY idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统多语言消息表';

-- 初始化一些默认消息
INSERT INTO sys_i18n_message (message_key, locale, message_value, module) VALUES
('common.success', 'zh_CN', '操作成功', 'common'),
('common.success', 'en_US', 'Operation successful', 'common'),
('common.failed', 'zh_CN', '操作失败', 'common'),
('common.failed', 'en_US', 'Operation failed', 'common'),
('user.notFound', 'zh_CN', '用户不存在', 'user'),
('user.notFound', 'en_US', 'User not found', 'user');
