# 公共核心模块 (common-core)

## 1. 模块概述

common-core是整个项目的基础核心模块，提供了通用的工具类、异常处理、结果封装等基础功能，所有业务模块都依赖于此模块。

### 1.1 主要功能

- **统一响应结构**：ApiResponse通用响应封装
- **异常处理**：BusinessException业务异常、ErrorCode错误码枚举
- **工具类**：StringUtils、DateUtils、IdUtils等
- **通用实体**：BaseEntity基础实体、Page分页结果

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 基础框架 |
| Jackson | 2.15.x | JSON处理 |
| Lombok | - | 已移除，手动实现 |

---

## 2. 核心类说明

### 2.1 ApiResponse<T> - 统一响应封装

```java
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> error(int code, String message) { ... }
    public static <T> ApiResponse<T> error(ErrorCode errorCode) { ... }
}
```

#### 响应示例

**成功响应**
```json
{
    "code": 200,
    "message": "success",
    "data": { ... },
    "timestamp": 1716192000000
}
```

**错误响应**
```json
{
    "code": 400,
    "message": "请求参数错误",
    "data": null,
    "timestamp": 1716192000000
}
```

### 2.2 ErrorCode - 错误码枚举

```java
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "内部服务器错误"),
    USER_ALREADY_EXISTS(1001, "用户已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    // ... 更多错误码
}
```

### 2.3 BusinessException - 业务异常

```java
public class BusinessException extends RuntimeException {
    private final int code;
    private final String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
```

### 2.4 PageResult<T> - 分页结果

```java
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long size;
    private long current;
    private long pages;

    public static <T> PageResult<T> of(Page<T> page) { ... }
}
```

---

## 3. 错误码规范

### 3.1 错误码区间

| 区间 | 模块 | 说明 |
|------|------|------|
| 200 | - | 成功 |
| 400-499 | - | 客户端错误 |
| 500-599 | - | 服务端错误 |
| 1000-1999 | 用户模块 | user-org-service |
| 2000-2999 | 通知模块 | notification-service |
| 3000-3999 | 文件模块 | file-service |
| 4000-4999 | 工作流模块 | workflow-service |
| 5000-5999 | 第三方模块 | third-party-service |
| 6000-6999 | 系统模块 | system-service |
| 7000-7999 | 监控模块 | system-monitor |

### 3.2 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 内部服务器错误 |

---

## 4. 工具类

### 4.1 IdUtils - ID生成工具

```java
public class IdUtils {
    public static Long generateId() {
        // 使用雪花算法生成Long类型ID
    }
}
```

### 4.2 StringUtils - 字符串工具

```java
public class StringUtils {
    public static boolean isEmpty(String str) { ... }
    public static boolean isNotEmpty(String str) { ... }
    public static boolean isBlank(String str) { ... }
    public static boolean isNotBlank(String str) { ... }
    public static String trim(String str) { ... }
    public static String defaultIfEmpty(String str, String defaultStr) { ... }
}
```

### 4.3 DateUtils - 日期工具

```java
public class DateUtils {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    public static String format(LocalDateTime dateTime) { ... }
    public static String format(LocalDateTime dateTime, String pattern) { ... }
    public static LocalDateTime parse(String dateStr) { ... }
    public static LocalDateTime parse(String dateStr, String pattern) { ... }
}
```

---

## 5. 依赖关系

```
common-core
├── 无外部依赖（纯工具模块）
│
└── 被以下模块依赖:
    ├── common-security
    ├── common-cache
    ├── common-log
    ├── common-mq
    ├── common-i18n
    ├── user-org-service
    ├── notification-service
    ├── file-service
    ├── workflow-service
    ├── third-party-service
    ├── system-service
    ├── system-monitor
    └── api-gateway
```

---

## 6. 使用示例

### 6.1 Controller返回

```java
@GetMapping("/{id}")
public ApiResponse<UserVO> getUser(@PathVariable Long id) {
    UserVO user = userService.getUserById(id);
    return ApiResponse.success(user);
}
```

### 6.2 抛出业务异常

```java
public UserVO getUserById(Long id) {
    User user = getById(id);
    if (user == null) {
        throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    return convertToVO(user);
}
```

### 6.3 自定义错误码

```java
public enum UserErrorCode implements ErrorCode {
    USER_ALREADY_EXISTS(1001, "用户已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误");

    private final int code;
    private final String message;
}
```

---

## 7. 注意事项

1. **不要使用Lombok**：所有类手动实现getter/setter，确保Java 21兼容性
2. **统一响应格式**：所有Controller必须使用ApiResponse包装返回值
3. **异常处理**：业务异常使用BusinessException，避免直接抛出RuntimeException
4. **错误码规范**：新增错误码需遵循区间规范
