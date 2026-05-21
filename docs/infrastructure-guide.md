# 基础设施使用指南 | Infrastructure Usage Guide

## 📋 目录 | Table of Contents
- [缓存体系使用](#缓存体系使用)
- [消息队列使用](#消息队列使用)
- [服务治理使用](#服务治理使用)
- [监控体系使用](#监控体系使用)
- [日志体系使用](#日志体系使用)

## 1. 缓存体系使用 | Cache System Usage

### 1.1 Redis缓存 | Redis Cache

#### 基本使用 | Basic Usage
```java
@Autowired
private RedisUtil redisUtil;

// 设置缓存 | Set cache
redisUtil.set("user:123", user);

// 设置带过期时间的缓存 | Set cache with expiration
redisUtil.set("user:123", user, 3600, TimeUnit.SECONDS);

// 获取缓存 | Get cache
Object value = redisUtil.get("user:123");

// 删除缓存 | Delete cache
redisUtil.delete("user:123");
```

#### Spring Cache注解 | Spring Cache Annotations
```java
@Cacheable(value = "user", key = "#id")
public User getUserById(Long id) {
    return userRepository.findById(id);
}

@CacheEvict(value = "user", key = "#id")
public void deleteUser(Long id) {
    userRepository.deleteById(id);
}

@CachePut(value = "user", key = "#user.id")
public User updateUser(User user) {
    return userRepository.save(user);
}
```

### 1.2 分布式锁 | Distributed Lock

#### 注解方式（推荐）| Annotation Style (Recommended)
```java
@DistributedLock(key = "order:create:{userId}", expireTime = 30000)
public Order createOrder(Long userId, OrderRequest request) {
    // 业务逻辑 | Business logic
    return order;
}
```

#### 手动方式 | Manual Style
```java
@Autowired
private RedisDistributedLock redisDistributedLock;

public void process() {
    String lockKey = "my-lock";
    boolean locked = false;
    
    try {
        locked = redisDistributedLock.tryLock(lockKey, 30000, 10000, 100);
        if (locked) {
            // 执行业务逻辑 | Execute business logic
        } else {
            throw new BusinessException("系统繁忙，请稍后重试 | System busy, please try again later");
        }
    } finally {
        if (locked) {
            redisDistributedLock.unlock(lockKey);
        }
    }
}
```

#### 可重入锁 | Reentrant Lock
```java
String ownerId = UUID.randomUUID().toString();
boolean locked = redisDistributedLock.tryReentrantLock("my-lock", ownerId);
try {
    // 执行业务 | Execute business
} finally {
    redisDistributedLock.unlockReentrantLock("my-lock", ownerId);
}
```

## 2. 消息队列使用 | Message Queue Usage

### 2.1 RabbitMQ

#### 发送消息 | Send Message
```java
@Autowired
@Qualifier("rabbitMQProducer")
private MessageProducer messageProducer;

public void sendUserMessage(User user) {
    BaseMessage message = BaseMessage.builder()
        .messageId(UUID.randomUUID().toString())
        .topic("user.topic")
        .tag("create")
        .body(JSON.toJSONString(user))
        .producer("user-org-service")
        .sendTime(LocalDateTime.now())
        .retryCount(0)
        .build();
    
    messageProducer.send(message);
}
```

#### 发送延迟消息 | Send Delay Message
```java
public void sendDelayMessage() {
    messageProducer.sendWithDelay("order.topic", "order-data", 5000);
}
```

#### 发送事务消息 | Send Transaction Message
```java
public void sendTransactionMessage() {
    BaseMessage message = BaseMessage.builder()
        .messageId(UUID.randomUUID().toString())
        .topic("payment.topic")
        .body("payment-data")
        .build();
    
    messageProducer.sendTransactionMessage(message);
}
```

#### 消费消息 | Consume Message
```java
@Component
public class UserMessageConsumer {
    
    @RabbitListener(queues = "user.queue")
    public void handleMessage(Message message, Channel channel) throws IOException {
        try {
            String body = new String(message.getBody());
            User user = JSON.parseObject(body, User.class);
            
            // 处理业务逻辑 | Process business logic
            
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
```

### 2.2 Kafka

#### 发送消息 | Send Message
```java
@Autowired
@Qualifier("kafkaProducer")
private MessageProducer kafkaProducer;

public void sendKafkaMessage() {
    kafkaProducer.send("user-topic", "user-data");
}
```

#### 消费消息 | Consume Message
```java
@Component
public class KafkaMessageConsumer {
    
    @KafkaListener(topics = "user-topic", groupId = "zhuji-group")
    public void handleMessage(String message) {
        log.info("收到Kafka消息: {}", message);
        // 处理业务逻辑 | Process business logic
    }
}
```

## 3. 服务治理使用 | Service Governance Usage

### 3.1 Nacos配置 | Nacos Config

#### 动态配置 | Dynamic Config
```java
@RefreshScope
@ConfigurationProperties(prefix = "app.config")
@Data
public class AppConfig {
    private String name;
    private Integer maxConnections;
    private Boolean featureEnabled;
}
```

#### 配置文件（Nacos）| Config File (Nacos)
```yaml
# Data ID: user-org-service.yaml
# Group: DEFAULT_GROUP

app:
  config:
    name: zhuji-user-org
    max-connections: 100
    feature-enabled: true
```

### 3.2 Sentinel限流熔断 | Sentinel Rate Limiting & Circuit Breaker

#### 注解方式 | Annotation Style
```java
@SentinelResource(
    value = "getUser",
    blockHandler = "handleBlock",
    fallback = "handleFallback"
)
public User getUser(Long id) {
    return userRepository.findById(id);
}

// 限流处理 | Rate limiting handler
public User handleBlock(Long id, BlockException e) {
    log.warn("触发限流: userId={}", id);
    return User.builder().name("系统繁忙 | System busy").build();
}

// 降级处理 | Fallback handler
public User handleFallback(Long id, Throwable t) {
    log.error("触发降级: userId={}", id, t);
    return User.builder().name("服务降级 | Service degraded").build();
}
```

#### 代码方式 | Code Style
```java
public void process() {
    Entry entry = null;
    try {
        entry = SphU.entry("my-resource");
        // 执行业务逻辑 | Execute business logic
    } catch (BlockException e) {
        // 处理限流 | Handle rate limiting
        log.warn("触发限流 | Rate limit triggered");
    } finally {
        if (entry != null) {
            entry.exit();
        }
    }
}
```

### 3.3 Seata分布式事务 | Seata Distributed Transaction

#### AT模式（推荐）| AT Mode (Recommended)
```java
@GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
public void createOrder(OrderRequest request) {
    // 创建订单 | Create order
    Order order = orderService.create(request);
    
    // 扣减库存 | Deduct inventory
    inventoryService.deduct(request.getProductId(), request.getQuantity());
    
    // 扣减余额 | Deduct balance
    accountService.deduct(request.getUserId(), request.getAmount());
}
```

#### TCC模式 | TCC Mode
```java
@LocalTCC
public interface InventoryTccService {
    
    @TwoPhaseBusinessAction(name = "prepareDeduct", commitMethod = "commit", rollbackMethod = "rollback")
    boolean prepareDeduct(@BusinessActionContextParameter(paramName = "productId") Long productId,
                         @BusinessActionContextParameter(paramName = "quantity") Integer quantity);
    
    boolean commit(BusinessActionContext context);
    
    boolean rollback(BusinessActionContext context);
}
```

## 4. 监控体系使用 | Monitoring System Usage

### 4.1 SkyWalking链路追踪 | SkyWalking Distributed Tracing

#### 启动配置 | Startup Config
```bash
java -javaagent:/path/to/skywalking-agent.jar \
     -Dskywalking.agent.service_name=user-org-service \
     -Dskywalking.collector.backend_service=localhost:11800 \
     -Dskywalking.logging.level=INFO \
     -jar user-org-service.jar
```

#### 自定义Span | Custom Span
```java
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.apache.skywalking.apm.toolkit.trace.TracingContext;

public void customSpan() {
    ActiveSpan activeSpan = TraceContext.activeSpan();
    activeSpan.tag("custom-tag", "tag-value");
    activeSpan.info("自定义日志信息 | Custom log info");
}
```

### 4.2 Prometheus指标监控 | Prometheus Metrics Monitoring

#### 自定义指标 | Custom Metrics
```java
@Component
public class CustomMetrics {
    
    private final Counter requestCounter;
    private final Gauge activeConnections;
    private final Timer requestTimer;
    
    public CustomMetrics(MeterRegistry registry) {
        this.requestCounter = Counter.builder("api.requests")
            .description("API请求总数 | Total API requests")
            .tag("service", "user-org")
            .register(registry);
        
        this.activeConnections = Gauge.builder("active.connections")
            .description("活跃连接数 | Active connections")
            .register(registry);
        
        this.requestTimer = Timer.builder("api.request.duration")
            .description("API请求耗时 | API request duration")
            .register(registry);
    }
    
    public void incrementRequest() {
        requestCounter.increment();
    }
    
    public void recordTimer() {
        requestTimer.record(() -> {
            // 执行业务逻辑 | Execute business logic
        });
    }
}
```

### 4.3 Grafana可视化 | Grafana Visualization

#### 访问Grafana | Access Grafana
- URL: http://localhost:3000
- 用户名 | Username: admin
- 密码 | Password: admin

#### 导入预设Dashboard | Import Preset Dashboard
1. 登录Grafana | Login Grafana
2. 点击 "+" -> "Import"
3. 输入Dashboard ID（如：4701 - JVM监控）| Enter Dashboard ID (e.g., 4701 - JVM Monitoring)
4. 选择Prometheus数据源 | Select Prometheus data source
5. 点击"Import" | Click "Import"

## 5. 日志体系使用 | Log System Usage

### 5.1 日志规范 | Log Standards

#### 日志级别使用 | Log Level Usage
- **ERROR**: 错误日志，需要立即处理 | Error log, needs immediate attention
- **WARN**: 警告日志，可能存在问题 | Warning log, potential issues
- **INFO**: 重要业务日志 | Important business log
- **DEBUG**: 调试日志（生产环境关闭）| Debug log (disabled in production)

#### 日志格式 | Log Format
```java
@Slf4j
@Service
public class UserService {
    
    public User createUser(UserRequest request) {
        log.info("[创建用户 | Create User] 开始处理, request={}", request);
        
        try {
            User user = buildUser(request);
            userRepository.save(user);
            
            log.info("[创建用户 | Create User] 处理成功, userId={}", user.getId());
            return user;
        } catch (Exception e) {
            log.error("[创建用户 | Create User] 处理失败, request={}", request, e);
            throw new BusinessException("创建用户失败 | Failed to create user", e);
        }
    }
}
```

### 5.2 TraceId链路追踪 | TraceId Distributed Tracing

#### 自动注入（已配置）| Auto Injection (Configured)
所有请求自动注入TraceId，格式 | All requests automatically inject TraceId, format：
```
2024-01-15 10:30:45.123 [http-nio-8081-exec-1] INFO  c.z.u.controller.UserController [a1b2c3d4] - 用户登录成功 | User logged in successfully
```

#### 手动获取TraceId | Get TraceId Manually
```java
import org.slf4j.MDC;

public String getTraceId() {
    return MDC.get("traceId");
}
```

### 5.3 ELK日志查询 | ELK Log Query

#### 访问Kibana | Access Kibana
- URL: http://localhost:5601

#### 查询示例 | Query Examples
```
# 查询特定服务的日志 | Query logs for specific service
app_name: "user-org-service"

# 查询错误日志 | Query error logs
level: "ERROR"

# 查询特定TraceId的日志 | Query logs for specific TraceId
traceId: "a1b2c3d4"

# 组合查询 | Combined query
app_name: "user-org-service" AND level: "ERROR"
```

## 6. 完整示例 | Complete Example

### 示例场景：创建订单 | Example Scenario: Create Order

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final AccountService accountService;
    private final RedisDistributedLock redisDistributedLock;
    private final MessageProducer messageProducer;
    
    @DistributedLock(key = "order:create:{request.userId}", expireTime = 30000)
    @SentinelResource(value = "createOrder", blockHandler = "handleBlock")
    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public Order createOrder(OrderRequest request) {
        log.info("[创建订单 | Create Order] 开始处理, request={}", request);
        
        // 创建订单 | Create order
        Order order = Order.builder()
            .userId(request.getUserId())
            .productId(request.getProductId())
            .quantity(request.getQuantity())
            .amount(request.getAmount())
            .status(OrderStatus.CREATED)
            .build();
        orderRepository.save(order);
        
        // 扣减库存 | Deduct inventory
        inventoryService.deduct(request.getProductId(), request.getQuantity());
        
        // 扣减余额 | Deduct balance
        accountService.deduct(request.getUserId(), request.getAmount());
        
        // 发送消息 | Send message
        sendOrderMessage(order);
        
        log.info("[创建订单 | Create Order] 处理成功, orderId={}", order.getId());
        return order;
    }
    
    private void sendOrderMessage(Order order) {
        BaseMessage message = BaseMessage.builder()
            .messageId(UUID.randomUUID().toString())
            .topic("order.topic")
            .tag("created")
            .body(JSON.toJSONString(order))
            .producer("order-service")
            .sendTime(LocalDateTime.now())
            .retryCount(0)
            .build();
        
        messageProducer.send(message);
    }
    
    public Order handleBlock(OrderRequest request, BlockException e) {
        log.warn("[创建订单 | Create Order] 触发限流, request={}", request);
        throw new BusinessException("系统繁忙，请稍后重试 | System busy, please try again later");
    }
}
```

## 7. 最佳实践 | Best Practices

### 7.1 缓存使用 | Cache Usage
- ✅ 合理设置过期时间 | Set reasonable expiration time
- ✅ 使用分布式锁防止缓存击穿 | Use distributed lock to prevent cache breakdown
- ✅ 缓存空值防止缓存穿透 | Cache null values to prevent cache penetration
- ✅ 使用批量操作减少网络开销 | Use batch operations to reduce network overhead

### 7.2 消息队列使用 | Message Queue Usage
- ✅ 消息幂等性处理 | Message idempotency handling
- ✅ 消息重试机制 | Message retry mechanism
- ✅ 死信队列处理失败消息 | Dead letter queue for failed messages
- ✅ 消息顺序性保证 | Message order guarantee

### 7.3 分布式事务使用 | Distributed Transaction Usage
- ✅ 优先使用AT模式 | Prefer AT mode
- ✅ 合理设置事务超时时间 | Set reasonable transaction timeout
- ✅ 做好幂等性处理 | Ensure idempotency
- ✅ 异常处理和回滚 | Exception handling and rollback

### 7.4 监控告警 | Monitoring & Alerting
- ✅ 设置合理的告警阈值 | Set reasonable alert thresholds
- ✅ 关键指标监控 | Key metrics monitoring
- ✅ 日志告警规则 | Log alert rules
- ✅ 定期检查监控面板 | Regularly check monitoring dashboard
