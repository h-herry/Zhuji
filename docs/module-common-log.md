# 公共日志模块 (common-log)

## 1. 模块概述

common-log模块提供统一的日志处理功能，包括日志记录、日志格式化、日志切分等，配合ELK实现分布式日志追踪。

### 1.1 主要功能

- **统一日志格式**：标准化日志输出
- **日志切分**：按时间、大小切分日志
- **异步日志**：异步记录提高性能
- **日志脱敏**：敏感信息脱敏

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Logback | 1.4.x | 日志框架 |
| Logstash | 8.x | 日志收集 |

---

## 2. 日志配置

### 2.1 logback-spring.xml

```xml
<configuration>
    <springProperty scope="context" name="APP_NAME" source="spring.application.name"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/${APP_NAME}.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/${APP_NAME}.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

---

## 3. 日志格式

### 3.1 标准格式

```
2024-05-21 10:30:00.123 [http-nio-8080-exec-1] INFO  c.z.userorg.service.UserService - 用户登录成功: userId=1, username=admin
```

### 3.2 字段说明

| 字段 | 说明 |
|------|------|
| 时间 | yyyy-MM-dd HH:mm:ss.SSS |
| 线程 | [线程名] |
| 级别 | INFO/WARN/ERROR |
| 类名 | 类全限定名 |
| 内容 | 日志消息 |

---

## 4. 日志级别

| 级别 | 说明 | 使用场景 |
|------|------|----------|
| DEBUG | 调试信息 | 开发环境 |
| INFO | 一般信息 | 正常业务流程 |
| WARN | 警告信息 | 异常但可处理 |
| ERROR | 错误信息 | 异常需处理 |

---

## 5. 使用示例

```java
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public void login(String username) {
        try {
            // 业务逻辑
            log.info("用户登录成功: username={}", username);
        } catch (Exception e) {
            log.error("用户登录失败: username={}", username, e);
        }
    }
}
```
