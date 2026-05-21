# 筑基 (Zhuji) API 接口文档

## 目录
1. [API 概览](#1-api-概览)
2. [认证授权](#2-认证授权)
3. [用户组织 API](#3-用户组织-api)
4. [多语言 API](#4-多语言-api)
5. [工作流 API](#5-工作流-api)
6. [API 集成](#6-api-集成)
7. [通用响应格式](#7-通用响应格式)
8. [错误码说明](#8-错误码说明)
9. [API 调用示例](#9-api-调用示例)

---

## 1. API 概览

### 1.1 基础信息
- **Base URL**: `https://api.zhuji.com/api/v1`
- **协议**: HTTPS
- **认证方式**: JWT Bearer Token / OAuth2
- **数据格式**: JSON
- **字符编码**: UTF-8

### 1.2 API 版本控制
```
https://api.zhuji.com/api/v1/users     # 版本 1
https://api.zhuji.com/api/v2/users     # 版本 2
```

### 1.3 接口列表
| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 认证 | `/api/v1/auth` | 登录、登出、刷新 Token |
| 用户组织 | `/api/v1/users` | 用户管理 |
| 组织架构 | `/api/v1/orgs` | 组织单元管理 |
| 角色权限 | `/api/v1/roles` | 角色和权限管理 |
| 多语言 | `/api/v1/i18n` | 国际化资源管理 |
| 工作流 | `/api/v1/workflows` | 工作流引擎 |
| API 集成 | `/api/v1/integrations` | 第三方平台集成 |

---

## 2. 认证授权

### 2.1 登录接口
**请求**:
```
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  },
  "timestamp": 1716230400000
}
```

### 2.2 使用 Token 访问
在后续请求中，在 Header 中携带 Token：
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### 2.3 刷新 Token
**请求**:
```
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9..."
}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "expiresIn": 3600
  },
  "timestamp": 1716230400000
}
```

### 2.4 OAuth2 第三方登录
**请求**:
```
GET /api/v1/auth/oauth2/authorize?provider=dingtalk&redirect_uri=https://app.zhuji.com/callback
```

**回调**:
```
GET /api/v1/auth/oauth2/callback?code=xxx&state=yyy
```

---

## 3. 用户组织 API

### 3.1 创建用户
**请求**:
```
POST /api/v1/users
Content-Type: application/json
Authorization: Bearer {token}

{
  "username": "zhangsan",
  "password": "Password@123",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "orgId": 1001,
  "roleIds": [1, 2]
}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1001,
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "status": 1,
    "createTime": "2026-05-20T10:00:00"
  },
  "timestamp": 1716230400000
}
```

### 3.2 获取用户详情
**请求**:
```
GET /api/v1/users/{id}
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1001,
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "status": 1,
    "org": {
      "id": 1001,
      "name": "技术部"
    },
    "roles": [
      {
        "id": 1,
        "name": "ROLE_ADMIN"
      }
    ],
    "createTime": "2026-05-20T10:00:00"
  },
  "timestamp": 1716230400000
}
```

### 3.3 分页查询用户
**请求**:
```
GET /api/v1/users?page=0&size=10&username=zhang&status=1
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "content": [
      {
        "id": 1001,
        "username": "zhangsan",
        "email": "zhangsan@example.com",
        "status": 1
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 100,
    "totalPages": 10
  },
  "timestamp": 1716230400000
}
```

### 3.4 更新用户
**请求**:
```
PUT /api/v1/users/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "email": "newemail@example.com",
  "phone": "13900139000",
  "status": 1
}
```

### 3.5 删除用户
**请求**:
```
DELETE /api/v1/users/{id}
Authorization: Bearer {token}
```

### 3.6 批量导入用户
**请求**:
```
POST /api/v1/users/import
Content-Type: multipart/form-data
Authorization: Bearer {token}

file: users.xlsx
```

### 3.7 获取组织架构树
**请求**:
```
GET /api/v1/orgs/tree
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "name": "总公司",
      "parentId": null,
      "children": [
        {
          "id": 2,
          "name": "技术部",
          "parentId": 1,
          "children": []
        }
      ]
    }
  ],
  "timestamp": 1716230400000
}
```

---

## 4. 多语言 API

### 4.1 获取国际化消息
**请求**:
```
GET /api/v1/i18n/messages?locale=zh_CN
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "user.welcome": "欢迎，{0}！",
    "user.login.success": "登录成功",
    "user.login.fail": "登录失败，请检查用户名和密码"
  },
  "timestamp": 1716230400000
}
```

### 4.2 添加国际化消息
**请求**:
```
POST /api/v1/i18n/messages
Content-Type: application/json
Authorization: Bearer {token}

{
  "locale": "zh_CN",
  "key": "user.logout.success",
  "value": "退出成功"
}
```

### 4.3 批量导入国际化资源
**请求**:
```
POST /api/v1/i18n/import
Content-Type: multipart/form-data
Authorization: Bearer {token}

file: i18n_zh_CN.properties
```

---

## 5. 工作流 API (Flowable 7.0)

### 5.1 流程定义管理

#### 5.1.1 部署流程定义
```
POST /api/v1/process-definitions
Content-Type: multipart/form-data
Authorization: Bearer {token}

file: [流程文件.bpmn20.xml]
name: 请假流程
category: leave
```

#### 5.1.2 查询流程定义
```
GET /api/v1/process-definitions?page=1&size=10
Authorization: Bearer {token}
```

#### 5.1.3 获取流程定义
```
GET /api/v1/process-definitions/{id}
Authorization: Bearer {token}
```

#### 5.1.4 挂起流程定义
```
PUT /api/v1/process-definitions/{id}/suspend
Authorization: Bearer {token}
```

#### 5.1.5 激活流程定义
```
PUT /api/v1/process-definitions/{id}/activate
Authorization: Bearer {token}
```

### 5.2 流程实例管理

#### 5.2.1 启动流程实例
```
POST /api/v1/process-instances
Content-Type: application/json
Authorization: Bearer {token}

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

#### 5.2.2 查询流程实例
```
GET /api/v1/process-instances?page=1&size=10
Authorization: Bearer {token}
```

#### 5.2.3 获取流程实例详情
```
GET /api/v1/process-instances/{id}
Authorization: Bearer {token}
```

#### 5.2.4 终止流程实例
```
DELETE /api/v1/process-instances/{id}
Authorization: Bearer {token}
```

### 5.3 任务管理

#### 5.3.1 查询待办任务
```
GET /api/v1/tasks?assignee=admin&page=1&size=10
Authorization: Bearer {token}
```

#### 5.3.2 获取任务详情
```
GET /api/v1/tasks/{id}
Authorization: Bearer {token}
```

#### 5.3.3 领取任务
```
POST /api/v1/tasks/{id}/claim
Authorization: Bearer {token}
```

#### 5.3.4 完成任务
```
POST /api/v1/tasks/{id}/complete
Content-Type: application/json
Authorization: Bearer {token}

{
  "variables": {
    "approved": true,
    "comment": "同意请假申请"
  }
}
```

#### 5.3.5 转交任务
```
POST /api/v1/tasks/{id}/delegate
Content-Type: application/json
Authorization: Bearer {token}

{
  "userId": "targetUser"
}
```

#### 5.3.6 指派任务
```
POST /api/v1/tasks/{id}/assign
Content-Type: application/json
Authorization: Bearer {token}

{
  "userId": "assignee"
}
```
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "processInstanceId": "proc-001",
    "processKey": "leave-approval",
    "businessKey": "order-001",
    "startTime": "2026-05-20T10:00:00",
    "status": "RUNNING"
  },
  "timestamp": 1716230400000
}
```

### 5.2 查询待办任务
**请求**:
```
GET /api/v1/workflows/tasks?assignee=zhangsan&page=0&size=10
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "content": [
      {
        "taskId": "task-001",
        "taskName": "经理审批",
        "processInstanceId": "proc-001",
        "createTime": "2026-05-20T10:05:00",
        "assignee": "lisi"
      }
    ],
    "totalElements": 5,
    "totalPages": 1
  },
  "timestamp": 1716230400000
}
```

### 5.3 完成任务
**请求**:
```
POST /api/v1/workflows/tasks/{taskId}/complete
Content-Type: application/json
Authorization: Bearer {token}

{
  "variables": {
    "approved": true,
    "comment": "同意"
  }
}
```

### 5.4 查询流程历史
**请求**:
```
GET /api/v1/workflows/history/{processInstanceId}
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "processInstanceId": "proc-001",
    "processKey": "leave-approval",
    "startTime": "2026-05-20T10:00:00",
    "endTime": "2026-05-20T11:00:00",
    "status": "COMPLETED",
    "tasks": [
      {
        "taskName": "提交申请",
        "assignee": "zhangsan",
        "startTime": "2026-05-20T10:00:00",
        "endTime": "2026-05-20T10:05:00"
      },
      {
        "taskName": "经理审批",
        "assignee": "lisi",
        "startTime": "2026-05-20T10:05:00",
        "endTime": "2026-05-20T11:00:00"
      }
    ]
  },
  "timestamp": 1716230400000
}
```

---

## 6. API 集成

### 6.1 注册第三方平台
**请求**:
```
POST /api/v1/integrations
Content-Type: application/json
Authorization: Bearer {token}

{
  "platform": "dingtalk",
  "appKey": "your-app-key",
  "appSecret": "your-app-secret",
  "redirectUri": "https://app.zhuji.com/callback"
}
```

### 6.2 调用第三方 API
**请求**:
```
POST /api/v1/integrations/{platform}/call
Content-Type: application/json
Authorization: Bearer {token}

{
  "api": "user.get",
  "params": {
    "userid": "zhangsan"
  }
}
```

### 6.3 查看集成日志
**请求**:
```
GET /api/v1/integrations/logs?platform=dingtalk&status=fail&page=0&size=10
Authorization: Bearer {token}
```

---

## 7. 通用响应格式

### 7.1 成功响应
```json
{
  "code": 200,
  "message": "成功",
  "data": { ... },
  "timestamp": 1716230400000
}
```

### 7.2 分页响应
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "content": [ ... ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 100,
    "totalPages": 10
  },
  "timestamp": 1716230400000
}
```

### 7.3 错误响应
```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "timestamp": 1716230400000
}
```

---

## 8. 错误码说明

### 8.1 通用错误码
| 错误码 | 说明 | HTTP 状态码 |
|--------|------|-------------|
| 200 | 成功 | 200 |
| 400 | 请求参数错误 | 400 |
| 401 | 未授权（Token 无效或过期） | 401 |
| 403 | 禁止访问（权限不足） | 403 |
| 404 | 资源不存在 | 404 |
| 409 | 资源冲突（如用户名已存在） | 409 |
| 500 | 服务器内部错误 | 500 |

### 8.2 业务错误码
| 错误码 | 说明 |
|--------|------|
| 10001 | 用户名已存在 |
| 10002 | 邮箱已存在 |
| 10003 | 手机号已存在 |
| 10004 | 原密码错误 |
| 10005 | 用户已被禁用 |
| 20001 | 角色已存在 |
| 20002 | 角色下有关联用户，无法删除 |
| 30001 | 第三方平台配置不存在 |
| 30002 | 调用第三方 API 失败 |

---

## 9. API 调用示例

### 9.1 cURL 示例
**登录**:
```bash
curl -X POST https://api.zhuji.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

**获取用户列表**:
```bash
curl -X GET https://api.zhuji.com/api/v1/users?page=0\&size=10 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**创建用户**:
```bash
curl -X POST https://api.zhuji.com/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -d '{
    "username": "zhangsan",
    "password": "Password@123",
    "email": "zhangsan@example.com"
  }'
```

### 9.2 JavaScript (Fetch API) 示例
```javascript
// 登录
async function login(username, password) {
  const response = await fetch('https://api.zhuji.com/api/v1/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  });
  const data = await response.json();
  if (data.code === 200) {
    localStorage.setItem('accessToken', data.data.accessToken);
    localStorage.setItem('refreshToken', data.data.refreshToken);
  }
  return data;
}

// 获取用户列表
async function getUsers(page, size) {
  const token = localStorage.getItem('accessToken');
  const response = await fetch(`https://api.zhuji.com/api/v1/users?page=${page}&size=${size}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return await response.json();
}
```

### 9.3 Java (RestTemplate) 示例
```java
@RestController
public class UserController {
    @Autowired
    private RestTemplate restTemplate;
    
    public ApiResponse<List<User>> getUsers(int page, int size) {
        String url = "https://api.zhuji.com/api/v1/users?page={page}&size={size}";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            ApiResponse.class,
            page, size
        );
        
        return response.getBody();
    }
}
```

---

## 附录

### A. Postman 集合
下载 Postman 集合：[zhuji-api.postman_collection.json](https://api.zhuji.com/docs/zhuji-api.postman_collection.json)

### B. OpenAPI (Swagger) 文档
访问 Swagger UI: `https://api.zhuji.com/swagger-ui.html`

### C. 参考文档
- [RFC 7519 - JWT](https://tools.ieft.org/html/rfc7519)
- [OAuth 2.0](https://oauth.net/2/)

---

**文档版本**: v1.0  
**最后更新**: 2026-05-20  
**维护者**: 筑基架构团队
