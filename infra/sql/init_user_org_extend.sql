-- 用户配置管理扩展表

USE zhuji_user_org;

-- 用户配置表
CREATE TABLE IF NOT EXISTS sys_user_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    value_type VARCHAR(20) DEFAULT 'STRING' COMMENT '值类型：STRING, NUMBER, BOOLEAN, JSON',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    UNIQUE KEY uk_user_config (user_id, config_key),
    INDEX idx_user_id (user_id),
    INDEX idx_config_type (config_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户配置表';

-- 角色配置表
CREATE TABLE IF NOT EXISTS sys_role_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    value_type VARCHAR(20) DEFAULT 'STRING' COMMENT '值类型：STRING, NUMBER, BOOLEAN, JSON',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    UNIQUE KEY uk_role_config (role_id, config_key),
    INDEX idx_role_id (role_id),
    INDEX idx_config_type (config_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色配置表';

-- 组织配置表
CREATE TABLE IF NOT EXISTS sys_org_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    org_id BIGINT NOT NULL COMMENT '组织ID',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    value_type VARCHAR(20) DEFAULT 'STRING' COMMENT '值类型：STRING, NUMBER, BOOLEAN, JSON',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    UNIQUE KEY uk_org_config (org_id, config_key),
    INDEX idx_org_id (org_id),
    INDEX idx_config_type (config_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织配置表';

-- 全局配置表
CREATE TABLE IF NOT EXISTS sys_global_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    value_type VARCHAR(20) DEFAULT 'STRING' COMMENT '值类型：STRING, NUMBER, BOOLEAN, JSON',
    description VARCHAR(200) COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(10) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    UNIQUE KEY uk_config_key (config_key),
    INDEX idx_config_type (config_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局配置表';

-- 用户-角色关系表（扩展，支持多角色和主角色）
CREATE TABLE IF NOT EXISTS sys_user_role_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    is_primary VARCHAR(10) DEFAULT '0' COMMENT '是否主角色：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

-- 用户-组织关系表（支持多组织）
CREATE TABLE IF NOT EXISTS sys_user_org_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    org_id BIGINT NOT NULL COMMENT '组织ID',
    relation_type VARCHAR(20) DEFAULT 'MEMBER' COMMENT '关系类型：MEMBER-成员，LEADER-负责人',
    is_primary VARCHAR(10) DEFAULT '0' COMMENT '是否主组织：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    UNIQUE KEY uk_user_org (user_id, org_id),
    INDEX idx_user_id (user_id),
    INDEX idx_org_id (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户组织关系表';

-- 初始化一些默认全局配置
INSERT INTO sys_global_config (config_type, config_key, config_value, value_type, description, sort_order, status) VALUES
('i18n', 'default.language', 'zh_CN', 'STRING', '系统默认语言', 1, '1'),
('i18n', 'supported.languages', 'zh_CN,en_US,zh_TW,ja_JP,ko_KR', 'STRING', '支持的语言列表', 2, '1'),
('system', 'max.login.attempts', '5', 'NUMBER', '最大登录尝试次数', 1, '1'),
('system', 'session.timeout', '1800', 'NUMBER', '会话超时时间（秒）', 2, '1'),
('security', 'password.min.length', '8', 'NUMBER', '密码最小长度', 1, '1'),
('security', 'password.require.special.char', 'true', 'BOOLEAN', '密码必须包含特殊字符', 2, '1'),
('security', 'password.expiry.days', '90', 'NUMBER', '密码过期天数', 3, '1'),
('role', 'default.user.role', 'USER', 'STRING', '新用户默认角色', 1, '1'),
('org', 'allow.virtual.org', 'true', 'BOOLEAN', '允许创建虚拟组织', 1, '1'),
('org', 'max.org.depth', '10', 'NUMBER', '最大组织层级深度', 2, '1');