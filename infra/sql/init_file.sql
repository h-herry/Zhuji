CREATE DATABASE IF NOT EXISTS zhuji_file DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zhuji_file;

CREATE TABLE IF NOT EXISTS sys_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    file_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    original_name VARCHAR(255) COMMENT '原始文件名',
    file_type VARCHAR(100) COMMENT '文件类型（MIME类型）',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_url VARCHAR(500) COMMENT '访问URL',
    storage_type VARCHAR(20) DEFAULT 'local' COMMENT '存储类型：local/minio/oss',
    bucket_name VARCHAR(100) COMMENT '存储桶名称',
    path VARCHAR(500) COMMENT '文件路径',
    user_id BIGINT COMMENT '上传用户ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    INDEX idx_file_name (file_name),
    INDEX idx_user_id (user_id),
    INDEX idx_storage_type (storage_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

CREATE TABLE IF NOT EXISTS sys_file_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    category_code VARCHAR(50) NOT NULL UNIQUE COMMENT '分类编码',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    description VARCHAR(500) COMMENT '分类描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    INDEX idx_category_code (category_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件分类表';

INSERT INTO sys_file_category (category_code, category_name, parent_id, sort, status, description) VALUES
('IMAGE', '图片', 0, 1, 1, '图片文件'),
('DOCUMENT', '文档', 0, 2, 1, '文档文件'),
('VIDEO', '视频', 0, 3, 1, '视频文件'),
('AUDIO', '音频', 0, 4, 1, '音频文件'),
('ARCHIVE', '压缩包', 0, 5, 1, '压缩包文件'),
('OTHER', '其他', 0, 100, 1, '其他文件');