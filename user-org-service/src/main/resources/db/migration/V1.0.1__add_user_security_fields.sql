-- 添加用户安全相关字段
-- 1. 修复sys_org_unit.org_type字段类型
ALTER TABLE sys_org_unit MODIFY COLUMN org_type BIGINT;

-- 2. 在sys_user表添加安全相关字段
ALTER TABLE sys_user 
ADD COLUMN password_update_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '密码更新时间',
ADD COLUMN login_fail_count INT DEFAULT 0 COMMENT '登录失败次数',
ADD COLUMN lock_until DATETIME COMMENT '锁定到期时间';

-- 3. 创建密码历史表
CREATE TABLE IF NOT EXISTS sys_user_password_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户密码历史表';

-- 4. 创建Token黑名单表
CREATE TABLE IF NOT EXISTS sys_token_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    token VARCHAR(512) NOT NULL COMMENT 'Token',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_token (token),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token黑名单表';

-- 5. 创建配置历史表
CREATE TABLE IF NOT EXISTS sys_user_config_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    operation VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE/UPDATE/DELETE',
    operator VARCHAR(64) COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_config_key (config_key),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户配置历史表';
