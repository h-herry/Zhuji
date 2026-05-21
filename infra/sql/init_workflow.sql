-- 创建数据库
CREATE DATABASE IF NOT EXISTS zhuji_workflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zhuji_workflow;

-- 流程定义表
CREATE TABLE IF NOT EXISTS wf_process_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    process_code VARCHAR(64) NOT NULL COMMENT '流程编码',
    process_name VARCHAR(128) NOT NULL COMMENT '流程名称',
    description VARCHAR(512) COMMENT '流程描述',
    version INT DEFAULT 1 COMMENT '版本号',
    status INT DEFAULT 0 COMMENT '状态：0-草稿，1-已发布',
    process_xml TEXT COMMENT '流程XML定义',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    UNIQUE KEY uk_process_code (process_code),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程定义表';

-- 流程实例表
CREATE TABLE IF NOT EXISTS wf_process_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    process_definition_id BIGINT NOT NULL COMMENT '流程定义ID',
    process_code VARCHAR(64) NOT NULL COMMENT '流程编码',
    business_key VARCHAR(64) NOT NULL COMMENT '业务键',
    business_type VARCHAR(64) COMMENT '业务类型',
    status INT DEFAULT 1 COMMENT '状态：1-运行中，2-已完成，3-已驳回，4-已终止',
    current_node_code VARCHAR(64) COMMENT '当前节点编码',
    current_node_name VARCHAR(128) COMMENT '当前节点名称',
    start_user_id VARCHAR(64) COMMENT '发起人ID',
    start_user_name VARCHAR(128) COMMENT '发起人姓名',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    INDEX idx_process_code (process_code),
    INDEX idx_business_key (business_key),
    INDEX idx_status (status),
    INDEX idx_start_user_id (start_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程实例表';

-- 流程任务表
CREATE TABLE IF NOT EXISTS wf_process_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    process_instance_id BIGINT NOT NULL COMMENT '流程实例ID',
    process_code VARCHAR(64) NOT NULL COMMENT '流程编码',
    business_key VARCHAR(64) NOT NULL COMMENT '业务键',
    task_code VARCHAR(64) NOT NULL COMMENT '任务编码',
    task_name VARCHAR(128) NOT NULL COMMENT '任务名称',
    node_code VARCHAR(64) COMMENT '节点编码',
    node_name VARCHAR(128) COMMENT '节点名称',
    assignee VARCHAR(64) COMMENT '处理人ID',
    assignee_name VARCHAR(128) COMMENT '处理人姓名',
    status INT DEFAULT 1 COMMENT '状态：1-待办，2-已完成，3-已驳回，4-已终止',
    action VARCHAR(32) COMMENT '操作：APPROVE-通过，REJECT-驳回',
    comment VARCHAR(512) COMMENT '审批意见',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    UNIQUE KEY uk_task_code (task_code),
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_assignee (assignee),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程任务表';

-- 初始化示例数据
INSERT INTO wf_process_definition (process_code, process_name, description, version, status, process_xml, create_time, update_time) VALUES
('leave_approval', '请假审批流程', '员工请假审批流程', 1, 1, '<process></process>', NOW(), NOW()),
('expense_reimbursement', '费用报销流程', '费用报销审批流程', 1, 1, '<process></process>', NOW(), NOW());
