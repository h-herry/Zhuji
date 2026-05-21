# 系统监控服务 (system-monitor)

## 1. 模块概述

系统监控服务提供系统健康检查和性能指标监控功能，支持内存、线程、CPU等多维度监控，并集成Prometheus指标导出。

### 1.1 主要功能

- **健康检查**：多维度健康指标
- **性能监控**：系统资源使用情况
- **指标导出**：Prometheus格式导出
- **告警阈值**：基于阈值的告警

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 基础框架 |
| Actuator | - | 健康检查 |
| Micrometer | 1.12.x | 指标收集 |
| Prometheus | - | 指标导出 |

---

## 2. 健康检查指标

### 2.1 内存健康检查 (MemoryHealthIndicator)

```java
// JVM堆内存使用率
// 指标详情：
// - memory.heap.used: 已使用堆内存
// - memory.heap.committed: 已提交堆内存
// - memory.heap.max: 最大堆内存
// - memory.heap.usage: 使用率百分比

// 告警阈值：使用率 > 90% 标记为DOWN
```

### 2.2 线程健康检查 (ThreadHealthIndicator)

```java
// JVM线程状态
// 指标详情：
// - thread.count: 当前线程数
// - thread.peak.count: 峰值线程数
// - thread.daemon.count: 守护线程数
// - thread.blocked.count: 阻塞线程数
// - thread.waiting.count: 等待线程数
```

### 2.3 CPU健康检查 (CpuHealthIndicator)

```java
// 系统CPU负载
// 指标详情：
// - cpu.system.load: 系统CPU负载
// - cpu.available.processors: 可用处理器数
// - runtime.uptime: JVM运行时间
```

---

## 3. API接口

### 3.1 监控端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/monitor/health | 健康检查 |
| GET | /api/v1/monitor/metrics | 系统指标 |
| GET | /actuator/health | Actuator健康检查 |
| GET | /actuator/prometheus | Prometheus指标 |

### 3.2 健康检查响应

```json
GET /api/v1/monitor/health
{
    "status": "UP",
    "components": {
        "memory": {
            "status": "UP",
            "details": {
                "heap.used": 1073741824,
                "heap.max": 2147483648,
                "heap.usage": 50.0
            }
        },
        "thread": {
            "status": "UP",
            "details": {
                "thread.count": 50,
                "thread.peak.count": 100
            }
        },
        "cpu": {
            "status": "UP",
            "details": {
                "system.load": 0.65
            }
        }
    }
}
```

---

## 4. 配置说明

### 4.1 application.yml

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
  health:
    redis:
      enabled: true
    db:
      enabled: true

spring:
  data:
    redis:
      host: localhost
      port: 6379
```

---

## 5. Prometheus集成

### 5.1 指标格式

```
# HELP jvm_memory_used_bytes JVM内存使用
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="Eden Space"} 1.073741824E9

# HELP jvm_threads_live_threads JVM活跃线程
# TYPE jvm_threads_live_threads gauge
jvm_threads_live_threads 50.0
```

### 5.2 Prometheus配置

```yaml
scrape_configs:
  - job_name: 'zhuji-monitor'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

---

## 6. 告警配置

| 指标 | 告警阈值 | 说明 |
|------|----------|------|
| 堆内存使用率 | > 90% | 内存不足 |
| 线程数 | > 500 | 线程泄漏 |
| 系统CPU | > 80% | 负载过高 |

---

## 7. 注意事项

1. **性能影响**：健康检查避免过于频繁
2. **指标保留**：合理设置指标保留时间
3. **告警风暴**：设置告警恢复机制
