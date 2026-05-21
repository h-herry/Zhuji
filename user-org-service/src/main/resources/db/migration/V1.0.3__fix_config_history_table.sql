-- 修复Bug1: sys_user_config_history表缺少列
-- 修复Bug2: sys_user_password_history表列名不匹配

USE zhuji_user_org;

-- 1. 修复sys_user_config_history表，添加缺失的列
ALTER TABLE sys_user_config_history
ADD COLUMN config_id BIGINT COMMENT '配置ID' AFTER id,
ADD COLUMN user_id BIGINT COMMENT '用户ID' AFTER config_id,
ADD COLUMN config_type VARCHAR(50) COMMENT '配置类型' AFTER config_key;

-- 添加新列的索引
ALTER TABLE sys_user_config_history
ADD INDEX idx_config_id (config_id),
ADD INDEX idx_user_id (user_id);

-- 删除可能存在的旧索引并重新创建（确保在config_id存在后创建）
DROP INDEX idx_config_version ON sys_user_config_history;
CREATE INDEX idx_config_version ON sys_user_config_history(config_id, version);

-- 2. 修复sys_user_password_history表列名
ALTER TABLE sys_user_password_history
CHANGE COLUMN password_hash password VARCHAR(255) NOT NULL COMMENT '密码哈希';
