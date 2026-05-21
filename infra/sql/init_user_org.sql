CREATE DATABASE IF NOT EXISTS zhuji_user_org DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zhuji_user_org;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    org_id BIGINT COMMENT '组织ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_org_id (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS sys_org_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    type_code INT NOT NULL UNIQUE COMMENT '类型编码',
    type_name VARCHAR(50) NOT NULL COMMENT '类型名称',
    type_key VARCHAR(50) NOT NULL UNIQUE COMMENT '类型键（英文）',
    description VARCHAR(200) COMMENT '描述',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统内置：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    INDEX idx_type_code (type_code),
    INDEX idx_type_key (type_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织类型配置表';

CREATE TABLE IF NOT EXISTS sys_org_unit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    org_code VARCHAR(50) NOT NULL UNIQUE COMMENT '组织编码',
    full_name VARCHAR(200) NOT NULL COMMENT '组织全称',
    short_name VARCHAR(100) COMMENT '组织简称',
    org_type INT NOT NULL COMMENT '组织类型ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父组织ID',
    level INT DEFAULT 1 COMMENT '层级',
    path VARCHAR(500) COMMENT '层级路径',
    leader_id BIGINT COMMENT '负责人ID',
    leader_name VARCHAR(50) COMMENT '负责人姓名',
    area_code VARCHAR(20) COMMENT '区域编码',
    cost_center VARCHAR(50) COMMENT '成本中心',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    is_virtual TINYINT DEFAULT 0 COMMENT '是否虚拟组织：0-否，1-是',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    INDEX idx_org_code (org_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_org_type (org_type),
    INDEX idx_path (path(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织单元表';

INSERT INTO sys_user (username, password, email, phone, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@zhuji.com', '13800138000', 1);

INSERT INTO sys_org_type (type_code, type_name, type_key, description, sort, status, is_system) VALUES
(1, '集团', 'GROUP', '集团总部', 1, 1, 1),
(2, '公司', 'COMPANY', '子公司/控股公司', 2, 1, 1),
(3, '分公司', 'BRANCH', '分公司/办事处', 3, 1, 1),
(4, '事业部', 'DIVISION', '事业部/业务单元', 4, 1, 1),
(5, '部门', 'DEPARTMENT', '职能部门', 5, 1, 1),
(6, '小组', 'TEAM', '工作小组/项目组', 6, 1, 1),
(7, '虚拟组织', 'VIRTUAL', '临时组织/项目组', 7, 1, 1);

INSERT INTO sys_org_unit (org_code, full_name, short_name, org_type, parent_id, level, path, leader_name, area_code, cost_center, sort, status, is_virtual, description) VALUES
('GROUP001', '筑基科技集团', '筑基集团', 1, 0, 1, '/1', '张总', 'CN-BJ', 'CC-GROUP', 1, 1, 0, '集团总部'),
('COMP001', '筑基科技有限公司', '筑基科技', 2, 1, 2, '/1/2', '李总', 'CN-BJ', 'CC-COMP001', 1, 1, 0, '北京总部公司'),
('COMP002', '筑基信息技术有限公司', '筑基信息', 2, 1, 2, '/1/3', '王总', 'CN-SH', 'CC-COMP002', 2, 1, 0, '上海分公司'),
('BRANCH001', '筑基科技广州分公司', '广州分公司', 3, 2, 3, '/1/2/4', '赵经理', 'CN-GZ', 'CC-BRANCH001', 1, 1, 0, '广州分公司'),
('BRANCH002', '筑基科技深圳分公司', '深圳分公司', 3, 2, 3, '/1/2/5', '钱经理', 'CN-SZ', 'CC-BRANCH002', 2, 1, 0, '深圳分公司'),
('DIV001', '产品研发事业部', '研发事业部', 4, 2, 3, '/1/2/6', '孙总', 'CN-BJ', 'CC-DIV001', 1, 1, 0, '负责产品研发'),
('DIV002', '市场营销事业部', '营销事业部', 4, 2, 3, '/1/2/7', '周总', 'CN-BJ', 'CC-DIV002', 2, 1, 0, '负责市场营销'),
('DIV003', '客户服务事业部', '客服事业部', 4, 2, 3, '/1/2/8', '吴总', 'CN-BJ', 'CC-DIV003', 3, 1, 0, '负责客户服务'),
('DEPT001', '后端开发部', '后端部', 5, 6, 4, '/1/2/6/9', '郑经理', 'CN-BJ', 'CC-DEPT001', 1, 1, 0, '后端开发部门'),
('DEPT002', '前端开发部', '前端部', 5, 6, 4, '/1/2/6/10', '冯经理', 'CN-BJ', 'CC-DEPT002', 2, 1, 0, '前端开发部门'),
('DEPT003', '测试部', '测试部', 5, 6, 4, '/1/2/6/11', '陈经理', 'CN-BJ', 'CC-DEPT003', 3, 1, 0, '测试部门'),
('DEPT004', '产品部', '产品部', 5, 6, 4, '/1/2/6/12', '褚经理', 'CN-BJ', 'CC-DEPT004', 4, 1, 0, '产品部门'),
('DEPT005', '销售部', '销售部', 5, 7, 4, '/1/2/7/13', '卫经理', 'CN-BJ', 'CC-DEPT005', 1, 1, 0, '销售部门'),
('DEPT006', '市场部', '市场部', 5, 7, 4, '/1/2/7/14', '蒋经理', 'CN-BJ', 'CC-DEPT006', 2, 1, 0, '市场部门'),
('DEPT007', '人力资源部', '人力部', 5, 2, 3, '/1/2/15', '沈经理', 'CN-BJ', 'CC-DEPT007', 1, 1, 0, '人力资源部门'),
('DEPT008', '财务部', '财务部', 5, 2, 3, '/1/2/16', '韩经理', 'CN-BJ', 'CC-DEPT008', 2, 1, 0, '财务部门'),
('DEPT009', '行政部', '行政部', 5, 2, 3, '/1/2/17', '杨经理', 'CN-BJ', 'CC-DEPT009', 3, 1, 0, '行政部门'),
('TEAM001', 'Java开发组', 'Java组', 6, 9, 5, '/1/2/6/9/18', '朱组长', 'CN-BJ', 'CC-TEAM001', 1, 1, 0, 'Java开发小组'),
('TEAM002', 'Go开发组', 'Go组', 6, 9, 5, '/1/2/6/9/19', '秦组长', 'CN-BJ', 'CC-TEAM002', 2, 1, 0, 'Go开发小组'),
('TEAM003', 'React开发组', 'React组', 6, 10, 5, '/1/2/6/10/20', '许组长', 'CN-BJ', 'CC-TEAM003', 1, 1, 0, 'React开发小组'),
('TEAM004', 'Vue开发组', 'Vue组', 6, 10, 5, '/1/2/6/10/21', '何组长', 'CN-BJ', 'CC-TEAM004', 2, 1, 0, 'Vue开发小组'),
('PROJ001', '智慧城市项目组', '智慧城市', 7, 1, 2, '/1/22', '项目总监', 'CN-BJ', 'CC-PROJ001', 1, 1, 1, '智慧城市项目组'),
('PROJ002', '数字化转型项目组', '数字化转型', 7, 1, 2, '/1/23', '项目总监', 'CN-BJ', 'CC-PROJ002', 2, 1, 1, '数字化转型项目组');