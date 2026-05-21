# 通知服务 (notification-service)

## 1. 模块概述

通知服务提供统一的系统通知功能，支持多种通知渠道（站内信、邮件、短信等），实现通知的发送、管理和查询。

### 1.1 主要功能

- **通知渠道管理**：支持多种通知渠道
- **通知发送**：统一的通知发送接口
- **通知管理**：通知的CRUD操作
- **通知查询**：支持分页、筛选查询

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 基础框架 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| RabbitMQ | 3.12.x | 消息队列 |
| Redis | 7.x | 缓存 |

---

## 2. 数据库设计

### 2.1 通知表 (notification)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| user_id | BIGINT | 接收用户ID |
| title | VARCHAR(200) | 通知标题 |
| content | TEXT | 通知内容 |
| type | VARCHAR(20) | 通知类型 |
| channel | VARCHAR(20) | 通知渠道 |
| status | VARCHAR(20) | 状态 |
| read_status | TINYINT | 已读状态 |
| send_time | DATETIME | 发送时间 |
| read_time | DATETIME | 阅读时间 |

---

## 3. API接口

### 3.1 通知管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/notifications | 发送通知 |
| GET | /api/v1/notifications/{id} | 获取通知详情 |
| GET | /api/v1/notifications | 分页查询通知 |
| PUT | /api/v1/notifications/{id}/read | 标记已读 |
| DELETE | /api/v1/notifications/{id} | 删除通知 |

### 3.2 请求响应示例

**发送通知请求**
```json
POST /api/v1/notifications
{
    "userId": 1,
    "title": "系统通知",
    "content": "您的账号已成功注册",
    "type": "SYSTEM",
    "channel": "IN_APP"
}
```

---

## 4. 通知类型

| 类型 | 说明 |
|------|------|
| SYSTEM | 系统通知 |
| ORDER | 订单通知 |
| MESSAGE | 消息通知 |
| ACTIVITY | 活动通知 |
| ALERT | 告警通知 |

### 4.1 通知渠道

| 渠道 | 说明 |
|------|------|
| IN_APP | 站内信 |
| EMAIL | 邮件 |
| SMS | 短信 |

---

## 5. 使用示例

### 5.1 发送通知

```java
@Autowired
private NotificationService notificationService;

public void sendNotification(Long userId, String title, String content) {
    NotificationRequest request = NotificationRequest.builder()
        .userId(userId)
        .title(title)
        .content(content)
        .type("SYSTEM")
        .channel("IN_APP")
        .build();

    notificationService.sendNotification(request);
}
```

---

## 6. 注意事项

1. **异步发送**：通知通过消息队列异步发送
2. **失败重试**：发送失败自动重试
3. **已读状态**：需实现批量标记已读功能
