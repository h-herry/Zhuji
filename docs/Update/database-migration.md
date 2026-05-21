# 数据库迁移脚本
## 用户管理与可配置化设计优化

**生成日期**: 2026-05-21  
**对应报告**: [源码深度分析报告 V2](./source-code-analysis-v2.md)

---

## 目录

1. [优化点 1: sys_user 表添加字段](#优化点1-sys_user-表添加字段)
2. [优化点 2: sys_org_unit.org_type 字段类型修正](#优化点2-sys_org_unitorg_type-字段类型修正)
3. [优化点 3: 新建密码历史表](#优化点3-新建密码历史表)
4. [优化点 4: 新建配置历史表](#优化点4-新建配置历史表)
5. [优化点 5: 添加默认配置数据](#优化点5-添加默认配置数据)
6. [完整迁移脚本](#完整迁移脚本)

---

## 优化点 1: sys_user 表添加字段

### 问题说明

`sys_user` 表缺少以下关键字段：
- `password_update_time` - 密码更新时间（用于密码过期策略）
- `last_login_time` - 最后登录时间（用于审计）
- `login_fail_count` - 登录失败次数（可选，已使用 Redis 实现）
- `lock_until` - 锁定截止时间（可选，已使用 Redis 实现）

### SQL 迁移脚本

```sql
-- =====================================================
-- 优化点 1: sys_user 表添加字段
-- 执行时间: 预计 5-10 秒
-- 影响范围: sys_user 表
-- =====================================================

USE zhuji_user_org;

-- 1. 添加密码更新时间字段
ALTER TABLE sys_user 
ADD COLUMN password_update_time DATETIME COMMENT '密码更新时间' AFTER password;

-- 2. 添加最后登录时间字段
ALTER TABLE sys_user 
ADD COLUMN last_login_time DATETIME COMMENT '最后登录时间' AFTER status;

-- 3. 为现有用户设置默认密码更新时间（设置为当前时间）
UPDATE sys_user 
SET password_update_time = NOW() 
WHERE password_update_time IS NULL;

-- 4. 添加索引（可选，如果需要根据密码更新时间查询）
-- ALTER TABLE sys_user ADD INDEX idx_password_update_time (password_update_time);

-- 5. 添加索引（可选，如果需要根据最后登录时间查询）
-- ALTER TABLE sys_user ADD INDEX idx_last_login_time (last_login_time);

-- 验证
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'zhuji_user_org'
  AND TABLE_NAME = 'sys_user'
  AND COLUMN_NAME IN ('password_update_time', 'last_login_time');
```

### 回滚脚本

```sql
-- =====================================================
-- 回滚脚本: 删除添加的字段
-- =====================================================

USE zhuji_user_org;

ALTER TABLE sys_user 
DROP COLUMN password_update_time,
DROP COLUMN last_login_time;
```

---

## 优化点 2: sys_org_unit.org_type 字段类型修正

### 问题说明

`sys_org_unit.org_type` 字段定义为 `INT`，但应该定义为 `BIGINT`（关联 `sys_org_type.id`）。

### SQL 迁移脚本

```sql
-- =====================================================
-- 优化点 2: sys_org_unit.org_type 字段类型修正
-- 执行时间: 预计 5-10 秒
-- 影响范围: sys_org_unit 表
-- =====================================================

USE zhuji_user_org;

-- 1. 修改 org_type 字段类型
ALTER TABLE sys_org_unit 
MODIFY COLUMN org_type BIGINT NOT NULL COMMENT '组织类型ID';

-- 2. 验证（确保数据一致）
-- 检查是否存在不匹配的组织类型
SELECT ou.id, ou.org_code, ou.org_type, ot.id AS org_type_id
FROM sys_org_unit ou
LEFT JOIN sys_org_type ot ON ou.org_type = ot.id
WHERE ot.id IS NULL;

-- 如果上述查询有结果，说明数据不一致，需要修正
-- UPDATE sys_org_unit SET org_type = 5 WHERE org_type NOT IN (SELECT id FROM sys_org_type);

-- 验证字段类型
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'zhuji_user_org'
  AND TABLE_NAME = 'sys_org_unit'
  AND COLUMN_NAME = 'org_type';
```

### 回滚脚本

```sql
-- =====================================================
-- 回滚脚本: 恢复字段类型
-- =====================================================

USE zhuji_user_org;

ALTER TABLE sys_org_unit 
MODIFY COLUMN org_type INT NOT NULL COMMENT '组织类型ID';
```

---

## 优化点 3: 新建密码历史表

### 问题说明

需要新建 `sys_user_password_history` 表，用于记录用户密码历史，防止重复使用旧密码。

### SQL 迁移脚本

```sql
-- =====================================================
-- 优化点 3: 新建密码历史表
-- 执行时间: 预计 5 秒
-- 影响范围: 新建表
-- =====================================================

USE zhuji_user_org;

-- 1. 创建密码历史表
CREATE TABLE IF NOT EXISTS sys_user_password_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
    password_hash VARCHAR(64) COMMENT '密码哈希值（用于快速比对）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户密码历史表';

-- 2. 添加外键约束（可选）
-- ALTER TABLE sys_user_password_history 
-- ADD CONSTRAINT fk_user_password_history_user 
-- FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;

-- 3. 初始化现有用户的密码历史（将当前密码作为第一条历史记录）
INSERT INTO sys_user_password_history (user_id, password, create_time)
SELECT id, password, COALESCE(password_update_time, NOW())
FROM sys_user
WHERE deleted = 0;

-- 验证
SELECT 
    TABLE_NAME,
    TABLE_COMMENT,
    ENGINE,
    TABLE_COLLATION
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'zhuji_user_org'
  AND TABLE_NAME = 'sys_user_password_history';

SELECT COUNT(*) AS total_records FROM sys_user_password_history;
```

### 回滚脚本

```sql
-- =====================================================
-- 回滚脚本: 删除密码历史表
-- =====================================================

USE zhuji_user_org;

DROP TABLE IF EXISTS sys_user_password_history;
```

---

## 优化点 4: 新建配置历史表

### 问题说明

需要新建 `sys_user_config_history` 表，用于记录配置变更历史，支持版本管理和回滚。

### SQL 迁移脚本

```sql
-- =====================================================
-- 优化点 4: 新建配置历史表
-- 执行时间: 预计 5 秒
-- 影响范围: 新建表
-- =====================================================

USE zhuji_user_org;

-- 1. 创建配置历史表
CREATE TABLE IF NOT EXISTS sys_user_config_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_id BIGINT NOT NULL COMMENT '配置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(50) COMMENT '配置类型',
    operation VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE, UPDATE, DELETE, ROLLBACK',
    operated_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    operated_by VARCHAR(50) COMMENT '操作人',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_config_id (config_id),
    INDEX idx_user_id (user_id),
    INDEX idx_config_key (config_key),
    INDEX idx_operated_time (operated_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户配置历史表';

-- 2. 添加外键约束（可选）
-- ALTER TABLE sys_user_config_history 
-- ADD CONSTRAINT fk_user_config_history_config 
-- FOREIGN KEY (config_id) REFERENCES sys_user_config(id) ON DELETE CASCADE;

-- 验证
SELECT 
    TABLE_NAME,
    TABLE_COMMENT,
    ENGINE,
    TABLE_COLLATION
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'zhuji_user_org'
  AND TABLE_NAME = 'sys_user_config_history';
```

### 回滚脚本

```sql
-- =====================================================
-- 回滚脚本: 删除配置历史表
-- =====================================================

USE zhuji_user_org;

DROP TABLE IF EXISTS sys_user_config_history;
```

---

## 优化点 5: 添加默认配置数据

### SQL 迁移脚本

```sql
-- =====================================================
-- 优化点 5: 添加默认配置数据
-- 执行时间: 预计 5 秒
-- 影响范围: sys_global_config 表
-- =====================================================

USE zhuji_user_org;

-- 1. 添加密码策略相关配置
INSERT INTO sys_global_config (config_type, config_key, config_value, value_type, description, sort_order, status) VALUES
('security', 'password.min.length', '8', 'NUMBER', '密码最小长度', 1, '1'),
('security', 'password.max.length', '32', 'NUMBER', '密码最大长度', 2, '1'),
('security', 'password.require.uppercase', 'true', 'BOOLEAN', '密码必须包含大写字母', 3, '1'),
('security', 'password.require.lowercase', 'true', 'BOOLEAN', '密码必须包含小写字母', 4, '1'),
('security', 'password.require.digit', 'true', 'BOOLEAN', '密码必须包含数字', 5, '1'),
('security', 'password.require.special.char', 'true', 'BOOLEAN', '密码必须包含特殊字符', 6, '1'),
('security', 'password.expiry.days', '90', 'NUMBER', '密码过期天数', 7, '1'),
('security', 'password.expiry.warning.days', '7', 'NUMBER', '密码过期提前警告天数', 8, '1'),
('security', 'password.history.count', '5', 'NUMBER', '密码历史记录数量', 9, '1'),
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    update_time = NOW();

-- 2. 添加 Token 相关配置
INSERT INTO sys_global_config (config_type, config_key, config_value, value_type, description, sort_order, status) VALUES
('token', 'access.token.expiration', '3600', 'NUMBER', '访问令牌过期时间（秒）', 1, '1'),
('token', 'refresh.token.expiration', '604800', 'NUMBER', '刷新令牌过期时间（秒）', 2, '1'),
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    update_time = NOW();

-- 3. 添加登录相关配置
INSERT INTO sys_global_config (config_type, config_key, config_value, value_type, description, sort_order, status) VALUES
('login', 'max.login.attempts', '5', 'NUMBER', '最大登录尝试次数', 1, '1'),
('login', 'lock.duration.minutes', '30', 'NUMBER', '账户锁定时长（分钟）', 2, '1'),
('login', 'session.timeout', '1800', 'NUMBER', '会话超时时间（秒）', 3, '1'),
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    update_time = NOW();

-- 验证
SELECT 
    config_type,
    config_key,
    config_value,
    value_type,
    description
FROM sys_global_config
WHERE config_type IN ('security', 'token', 'login')
ORDER BY config_type, sort_order;
```

---

## 完整迁移脚本

### 执行顺序

1. **优化点 1**: sys_user 表添加字段
2. **优化点 2**: sys_org_unit.org_type 字段类型修正
3. **优化点 3**: 新建密码历史表
4. **优化点 4**: 新建配置历史表
5. **优化点 5**: 添加默认配置数据

### 一键执行脚本

```sql
-- =====================================================
-- 筑基项目数据库迁移脚本 V1.0
-- 生成日期: 2026-05-21
-- 执行时间: 预计 30-60 秒
-- 影响范围: sys_user, sys_org_unit, 新建 2 张表, sys_global_config
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. sys_user 表添加字段
-- =====================================================

USE zhuji_user_org;

-- 检查字段是否已存在
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'zhuji_user_org'
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'password_update_time'
);

-- 如果字段不存在，则添加
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE sys_user ADD COLUMN password_update_time DATETIME COMMENT ''密码更新时间'' AFTER password',
    'SELECT ''字段 password_update_time 已存在，跳过'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'zhuji_user_org'
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'last_login_time'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN last_login_time DATETIME COMMENT ''最后登录时间'' AFTER status',
    'SELECT ''字段 last_login_time 已存在，跳过'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 更新现有用户的密码更新时间
UPDATE sys_user 
SET password_update_time = NOW() 
WHERE password_update_time IS NULL;

-- =====================================================
-- 2. sys_org_unit.org_type 字段类型修正
-- =====================================================

ALTER TABLE sys_org_unit 
MODIFY COLUMN org_type BIGINT NOT NULL COMMENT '组织类型ID';

-- =====================================================
-- 3. 新建密码历史表
-- =====================================================

CREATE TABLE IF NOT EXISTS sys_user_password_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
    password_hash VARCHAR(64) COMMENT '密码哈希值（用于快速比对）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户密码历史表';

-- 初始化现有用户的密码历史
INSERT INTO sys_user_password_history (user_id, password, create_time)
SELECT id, password, COALESCE(password_update_time, NOW())
FROM sys_user
WHERE deleted = 0
ON DUPLICATE KEY UPDATE create_time = create_time;

-- =====================================================
-- 4. 新建配置历史表
-- =====================================================

CREATE TABLE IF NOT EXISTS sys_user_config_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_id BIGINT NOT NULL COMMENT '配置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(50) COMMENT '配置类型',
    operation VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE, UPDATE, DELETE, ROLLBACK',
    operated_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    operated_by VARCHAR(50) COMMENT '操作人',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_config_id (config_id),
    INDEX idx_user_id (user_id),
    INDEX idx_config_key (config_key),
    INDEX idx_operated_time (operated_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户配置历史表';

-- =====================================================
-- 5. 添加默认配置数据
-- =====================================================

INSERT INTO sys_global_config (config_type, config_key, config_value, value_type, description, sort_order, status) VALUES
-- 密码策略配置
('security', 'password.min.length', '8', 'NUMBER', '密码最小长度', 1, '1'),
('security', 'password.max.length', '32', 'NUMBER', '密码最大长度', 2, '1'),
('security', 'password.require.uppercase', 'true', 'BOOLEAN', '密码必须包含大写字母', 3, '1'),
('security', 'password.require.lowercase', 'true', 'BOOLEAN', '密码必须包含小写字母', 4, '1'),
('security', 'password.require.digit', 'true', 'BOOLEAN', '密码必须包含数字', 5, '1'),
('security', 'password.require.special.char', 'true', 'BOOLEAN', '密码必须包含特殊字符', 6, '1'),
('security', 'password.expiry.days', '90', 'NUMBER', '密码过期天数', 7, '1'),
('security', 'password.expiry.warning.days', '7', 'NUMBER', '密码过期提前警告天数', 8, '1'),
('security', 'password.history.count', '5', 'NUMBER', '密码历史记录数量', 9, '1'),
-- Token 配置
('token', 'access.token.expiration', '3600', 'NUMBER', '访问令牌过期时间（秒）', 1, '1'),
('token', 'refresh.token.expiration', '604800', 'NUMBER', '刷新令牌过期时间（秒）', 2, '1'),
-- 登录配置
('login', 'max.login.attempts', '5', 'NUMBER', '最大登录尝试次数', 1, '1'),
('login', 'lock.duration.minutes', '30', 'NUMBER', '账户锁定时长（分钟）', 2, '1'),
('login', 'session.timeout', '1800', 'NUMBER', '会话超时时间（秒）', 3, '1')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    update_time = NOW();

-- =====================================================
-- 验证迁移结果
-- =====================================================

SELECT '=== 迁移完成，验证结果 ===' AS message;

-- 验证 sys_user 表字段
SELECT 'sys_user 表新增字段' AS check_item;
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'zhuji_user_org'
  AND TABLE_NAME = 'sys_user'
  AND COLUMN_NAME IN ('password_update_time', 'last_login_time');

-- 验证 sys_org_unit.org_type 字段类型
SELECT 'sys_org_unit.org_type 字段类型' AS check_item;
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'zhuji_user_org'
  AND TABLE_NAME = 'sys_org_unit'
  AND COLUMN_NAME = 'org_type';

-- 验证密码历史表
SELECT '密码历史表' AS check_item;
SELECT 
    TABLE_NAME,
    TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'zhuji_user_org'
  AND TABLE_NAME = 'sys_user_password_history';

-- 验证配置历史表
SELECT '配置历史表' AS check_item;
SELECT 
    TABLE_NAME,
    TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'zhuji_user_org'
  AND TABLE_NAME = 'sys_user_config_history';

-- 验证新增配置数据
SELECT '新增配置数据' AS check_item;
SELECT COUNT(*) AS config_count
FROM sys_global_config
WHERE config_type IN ('security', 'token', 'login');

SET FOREIGN_KEY_CHECKS = 1;

SELECT '=== 迁移成功完成 ===' AS message;
```

---

## 注意事项

### 执行前准备

1. **备份数据库**
   ```bash
   mysqldump -u root -p zhuji_user_org > zhuji_user_org_backup_$(date +%Y%m%d_%H%M%S).sql
   ```

2. **检查数据库连接**
   ```sql
   SELECT DATABASE();
   SELECT VERSION();
   ```

3. **检查表是否存在**
   ```sql
   SHOW TABLES;
   ```

### 执行建议

1. **生产环境执行建议**
   - 在业务低峰期执行
   - 先在测试环境验证
   - 准备回滚脚本
   - 监控执行过程

2. **分步执行建议**
   - 如果担心风险，可以分步执行每个优化点
   - 每执行一步，验证结果
   - 出现问题立即回滚

### 回滚完整脚本

```sql
-- =====================================================
-- 回滚脚本：撤销所有迁移
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE zhuji_user_org;

-- 1. 删除 sys_user 表新增字段
ALTER TABLE sys_user 
DROP COLUMN IF EXISTS password_update_time,
DROP COLUMN IF EXISTS last_login_time;

-- 2. 恢复 sys_org_unit.org_type 字段类型
ALTER TABLE sys_org_unit 
MODIFY COLUMN org_type INT NOT NULL COMMENT '组织类型ID';

-- 3. 删除密码历史表
DROP TABLE IF EXISTS sys_user_password_history;

-- 4. 删除配置历史表
DROP TABLE IF EXISTS sys_user_config_history;

-- 5. 删除新增的配置数据
DELETE FROM sys_global_config 
WHERE config_type IN ('security', 'token', 'login');

SET FOREIGN_KEY_CHECKS = 1;

SELECT '=== 回滚完成 ===' AS message;
```

---

**文件生成时间**: 2026-05-21 14:35:00  
**对应报告**: [源码深度分析报告 V2](./source-code-analysis-v2.md)
