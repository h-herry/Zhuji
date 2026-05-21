ALTER TABLE sys_user_config_history
ADD COLUMN version INT DEFAULT 1 COMMENT '版本号';

CREATE INDEX idx_config_version ON sys_user_config_history(config_id, version);
