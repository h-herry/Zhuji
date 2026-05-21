# 工作流服务 (workflow-service)

## 1. 模块概述

工作流服务集成了业界领先的开源工作流引擎Flowable 7.0，提供流程编排和任务调度功能，支持自定义流程定义、流程实例管理、任务审批、历史记录查询等。

### 1.1 主要功能

- **流程定义管理**：流程模型的创建、编辑、部署
- **流程实例管理**：流程实例的启动、查询、挂起、终止
- **任务管理**：任务的查询、领取、完成、转交
- **历史记录**：流程历史、任务历史的查询
- **流程变量**：支持复杂业务数据的流转
- **候选人/候选组**：支持任务候选人和候选组

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 基础框架 |
| Flowable | 7.0.0 | 工作流引擎 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| Redis | 7.x | 缓存 |
| MySQL | 8.x | 数据库 |

---

## 2. 数据库设计

### 2.1 Flowable数据表

Flowable使用自有数据表存储流程定义、实例、任务等数据：

| 表名前缀 | 说明 |
|----------|------|
| ACT_RE_* | 流程定义表 |
| ACT_RU_* | 运行时数据表 |
| ACT_HI_* | 历史数据表 |
| ACT_GE_* | 通用数据表 |

### 2.2 自定义扩展表

#### 2.2.1 流程定义表 (wf_process_definition)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| flowable_id | VARCHAR(100) | Flowable流程定义ID |
| name | VARCHAR(100) | 流程名称 |
| process_key | VARCHAR(100) | 流程Key |
| version | INT | 版本号 |
| description | VARCHAR(500) | 描述 |
| category | VARCHAR(100) | 分类 |
| status | VARCHAR(20) | 状态：1-激活，0-挂起 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 2.2.2 流程实例表 (wf_process_instance)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| flowable_id | VARCHAR(100) | Flowable流程实例ID |
| definition_id | BIGINT | 流程定义ID |
| business_key | VARCHAR(100) | 业务标识 |
| status | VARCHAR(20) | 状态：RUNNING-运行中，COMPLETED-已完成，TERMINATED-已终止 |
| start_user | VARCHAR(50) | 发起人 |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |

#### 2.2.3 任务记录表 (wf_task_record)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| flowable_id | VARCHAR(100) | Flowable任务ID |
| instance_id | BIGINT | 流程实例ID |
| task_name | VARCHAR(100) | 任务名称 |
| task_key | VARCHAR(100) | 任务Key |
| assignee | VARCHAR(50) | 办理人 |
| candidate_users | VARCHAR(500) | 候选人(逗号分隔) |
| candidate_groups | VARCHAR(500) | 候选组(逗号分隔) |
| status | VARCHAR(20) | 状态 |
| due_date | DATETIME | 截止时间 |
| create_time | DATETIME | 创建时间 |
| end_time | DATETIME | 结束时间 |

---

## 3. API接口

### 3.1 流程定义接口 (ProcessDefinitionController)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/v1/process-definitions | 部署流程定义 | 是 |
| GET | /api/v1/process-definitions | 分页查询流程定义 | 是 |
| GET | /api/v1/process-definitions/{id} | 获取流程定义详情 | 是 |
| GET | /api/v1/process-definitions/{id}/xml | 获取流程定义XML | 是 |
| PUT | /api/v1/process-definitions/{id}/suspend | 挂起流程定义 | 是 |
| PUT | /api/v1/process-definitions/{id}/activate | 激活流程定义 | 是 |
| DELETE | /api/v1/process-definitions/{id} | 删除流程定义 | 是 |

### 3.2 流程实例接口 (ProcessInstanceController)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/v1/process-instances | 启动流程实例 | 是 |
| GET | /api/v1/process-instances | 分页查询流程实例 | 是 |
| GET | /api/v1/process-instances/{id} | 获取流程实例详情 | 是 |
| DELETE | /api/v1/process-instances/{id} | 终止流程实例 | 是 |
| GET | /api/v1/process-instances/{id}/variables | 获取流程变量 | 是 |
| GET | /api/v1/process-instances/{id}/tasks | 获取流程任务列表 | 是 |
| GET | /api/v1/process-instances/{id}/history | 获取流程历史 | 是 |

### 3.3 任务接口 (TaskController)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/v1/tasks | 查询待办任务 | 是 |
| GET | /api/v1/tasks/{id} | 获取任务详情 | 是 |
| POST | /api/v1/tasks/{id}/claim | 领取任务 | 是 |
| POST | /api/v1/tasks/{id}/complete | 完成任务 | 是 |
| POST | /api/v1/tasks/{id}/delegate | 转交任务 | 是 |
| POST | /api/v1/tasks/{id}/assign | 指派任务 | 是 |

---

## 4. API调用示例

### 4.1 部署流程定义

```bash
POST /api/v1/process-definitions
Content-Type: multipart/form-data

file: [流程文件.bpmn20.xml]
name: 请假流程
category: leave
```

**响应示例**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "flowableId": "leaveProcess:1:xxxxx",
        "name": "请假流程",
        "processKey": "leaveProcess",
        "version": 1,
        "status": "1"
    }
}
```

### 4.2 启动流程实例

```bash
POST /api/v1/process-instances
Content-Type: application/json

{
    "processDefinitionKey": "leaveProcess",
    "businessKey": "LEAVE-2024-001",
    "variables": {
        "applyUser": "admin",
        "leaveType": "年假",
        "days": 5
    }
}
```

### 4.3 查询待办任务

```bash
GET /api/v1/tasks?assignee=admin&page=1&size=10
```

**响应示例**：
```json
{
    "code": 200,
    "data": {
        "records": [
            {
                "id": "xxxxx",
                "name": "部门经理审批",
                "processInstanceId": "yyyyy",
                "processDefinitionKey": "leaveProcess",
                "assignee": "admin",
                "createTime": "2024-01-15 10:00:00",
                "dueDate": "2024-01-16 10:00:00"
            }
        ],
        "total": 1,
        "size": 10,
        "current": 1
    }
}
```

### 4.4 完成任务

```bash
POST /api/v1/tasks/{taskId}/complete
Content-Type: application/json

{
    "variables": {
        "approved": true,
        "comment": "同意请假申请"
    }
}
```

---

## 5. 核心代码

### 5.1 FlowableConfig - 工作流引擎配置

```java
@Configuration
public class FlowableConfig {

    @Bean
    public ProcessEngine processEngine() {
        ProcessEngineConfiguration configuration = new StandaloneProcessEngineConfiguration();
        configuration.setJdbcUrl("jdbc:mysql://localhost:3306/zhuji_workflow");
        configuration.setJdbcUsername("root");
        configuration.setJdbcPassword("root");
        configuration.setJdbcDriver("com.mysql.cj.jdbc.Driver");
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        configuration.setHistory("full");
        configuration.setAsyncExecutorEnabled(true);
        return configuration.buildProcessEngine();
    }
}
```

### 5.2 FlowableProcessService - 流程服务

```java
@Service
public class FlowableProcessService {

    public ProcessInstance startProcess(String processDefinitionKey, String businessKey,
                                        Map<String, Object> variables) {
        return runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey(processDefinitionKey)
                .businessKey(businessKey)
                .variables(variables)
                .start();
    }

    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    public List<Task> getTasksByAssignee(String assignee) {
        return taskService.createTaskQuery()
                .taskAssignee(assignee)
                .list();
    }
}
```

---

## 6. Flowable7.0 新特性

### 6.1 ProcessInstanceBuilder API

Flowable 7.0 使用新的 `ProcessInstanceBuilder` API 启动流程实例：

```java
// Flowable 7.0 推荐方式
ProcessInstance instance = runtimeService.createProcessInstanceBuilder()
        .processDefinitionKey(processDefinitionKey)
        .businessKey(businessKey)
        .variables(variables)
        .start();
```

### 6.2 历史级别配置

Flowable 7.0 使用字符串配置历史级别：

```java
// Flowable 7.0
configuration.setHistory("full");

// 旧版本
// configuration.setHistoryLevel(ProcessEngineConfiguration.HISTORYLEVEL_FULL);
```

---

## 7. 集成注意事项

### 7.1 数据库配置

Flowable需要MySQL数据库支持，确保数据库字符集为utf8mb4。

### 7.2 事务管理

Flowable需要在事务环境下运行，确保Service方法添加 `@Transactional` 注解。

### 7.3 异步执行器

启用异步执行器可以提高流程处理性能：

```java
configuration.setAsyncExecutorEnabled(true);
configuration.setAsyncExecutorActivate(true);
```

---

## 8. 扩展功能

### 8.1 自定义监听器

可以添加任务监听器来处理任务事件：

```java
@FlowableTaskListener(event = FlowableTaskEventType.CREATE)
public void onTaskCreateDelegate(Status status) {
    // 任务创建时的处理逻辑
}
```

### 8.2 表单集成

Flowable支持动态表单和外部表单，可以根据业务需求选择合适的表单类型。

### 8.3 多实例支持

支持会签、串行等任务多实例模式：

```java
// 会签任务
runtimeService.createProcessInstanceBuilder()
        // 配置多实例
        .start();
```

---

## 9. 缓存策略

| 缓存Key | 说明 | 过期时间 |
|---------|------|----------|
| process:definition:{id} | 流程定义缓存 | 1小时 |
| process:definition:key:{key} | 按Key缓存流程定义 | 1小时 |
| task:count:{assignee} | 任务数量缓存 | 5分钟 |
