# 第三方服务集成 (third-party-service)

## 1. 模块概述

第三方服务集成模块提供统一第三方API调用能力，包括短信服务、HTTP接口调用等，采用适配器模式支持多种第三方服务。

### 1.1 主要功能

- **短信服务**：支持多短信服务商
- **HTTP调用**：通用HTTP请求封装
- **重试机制**：失败自动重试
- **适配器模式**：支持多种服务商切换

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 基础框架 |
| WebClient | - | HTTP客户端 |
| Resilience4j | 2.x | 重试限流 |

---

## 2. 核心组件

### 2.1 适配器接口

```java
public interface ThirdPartyAdapter {
    String getType();

    ThirdPartyResponse sendRequest(ThirdPartyRequest request);
}
```

### 2.2 短信适配器

```java
@Component
public class SmsAdapter implements ThirdPartyAdapter {

    @Override
    public String getType() {
        return "SMS";
    }

    @Override
    public ThirdPartyResponse sendRequest(ThirdPartyRequest request) {
        // 发送短信逻辑
    }
}
```

### 2.3 HTTP适配器

```java
@Component
public class HttpAdapter implements ThirdPartyAdapter {

    @Override
    public String getType() {
        return "HTTP";
    }

    @Override
    public ThirdPartyResponse sendRequest(ThirdPartyRequest request) {
        // HTTP请求逻辑
    }
}
```

---

## 3. API接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/thirdparty/sms | 发送短信 |
| POST | /api/v1/thirdparty/http | HTTP请求 |

### 3.1 发送短信请求

```json
POST /api/v1/thirdparty/sms
{
    "phone": "13800138000",
    "template": "LOGIN_CODE",
    "params": {
        "code": "123456"
    }
}
```

---

## 4. 重试配置

| 参数 | 默认值 | 说明 |
|------|--------|------|
| maxAttempts | 3 | 最大重试次数 |
| waitDuration | 1000 | 重试间隔(ms) |
| retryExceptions | IOException, TimeoutException | 可重试异常 |

---

## 5. 使用示例

### 5.1 发送短信

```java
@Autowired
private ThirdPartyService thirdPartyService;

public void sendLoginCode(String phone, String code) {
    ThirdPartyRequest request = ThirdPartyRequest.builder()
        .type("SMS")
        .phone(phone)
        .template("LOGIN_CODE")
        .params(Map.of("code", code))
        .build();

    thirdPartyService.send(request);
}
```

---

## 6. 注意事项

1. **限流**：注意第三方API调用频率限制
2. **安全**：敏感信息加密存储
3. **监控**：记录调用日志和耗时
