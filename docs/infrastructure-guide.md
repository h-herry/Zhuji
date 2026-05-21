# 基础设施使用指南

## 📋 目录
- [缓存体系使用](#缓存体系使用)
- [消息队列使用](#消息队列使用)
- [服务治理使用](#服务治理使用)
- [监控体系使用](#监控体系使用)
- [日志体系使用](#日志体系使用)

## 1. 缓存体系使用

### 1.1 Redis缓存

#### 基本使用
```java
@Autowired
private RedisUtil redisUtil;

// 设置缓存
redisUtil.set("user:123", user);

// 设置带过期时间的缓存
redisUtil.set("user:123", user, 3600, TimeUnit.SECONDS);

// 获取缓存
Object value = redisUtil.get("user:123");

// 删除缓存
redisUtil.delete("user:123");
```

#### Spring Cache注解
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

### 1.2 分布式锁

#### 注解方式（推荐）
```java
@DistributedLock(key = "order:create:{userId}", expireTime = 30000)
public Order createOrder(Long userId, OrderRequest request) {
    // 业务逻辑
    return order;
}
```

#### 手动方式
```java
@Autowired
private RedisDistributedLock redisDistributedLock;

public void process() {
    String lockKey = "my-lock";
    boolean locked = false;
    
    try {
        locked = redisDistributedLock.tryLock(lockKey, 30000, 10000, 100);
        if (locked) {
            // 执行业务逻辑
        } else {
            throw new BusinessException("系统繁忙，请稍后重试");
        }
    } finally {
        if (locked) {
            redisDistributedLock.unlock(lockKey);
        }
    }
}
```

#### 可重入锁
```java
String ownerId = UUID.randomUUID().toString();
boolean locked = redisDistributedLock.tryReentrantLock("my-lock", ownerId);
try {
    // 执行业务
} finally {
    redisDistributedLock.unlockReentrantLock("my-lock", ownerId);
}
```

## 2. 消息队列使用

### 2.1 RabbitMQ

#### 发送消息
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

#### 发送延迟消息
```java
public void sendDelayMessage() {
    messageProducer.sendWithDelay("order.topic", "order-data", 5000);
}
```

#### 发送事务消息
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

#### 消费消息
```java
@Component
public class UserMessageConsumer {
    
    @RabbitListener(queues = "user.queue")
    public void handleMessage(Message message, Channel channel) throws IOException {
        try {
            String body = new String(message.getBody());
            User user = JSON.parseObject(body, User.class);
            
            // 处理业务逻辑
            
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
```

### 2.2 Kafka

#### 发送消息
```java
@Autowired
@Qualifier("kafkaProducer")
private MessageProducer kafkaProducer;

public void sendKafkaMessage() {
    kafkaProducer.send("user-topic", "user-data");
}
```

#### 消费消息
```java
@Component
public class KafkaMessageConsumer {
    
    @KafkaListener(topics = "user-topic", groupId = "zhuji-group")
    public void handleMessage(String message) {
        log.info("收到Kafka消息: {}", message);
        // 处理业务逻辑
    }
}
```

## 3. 服务治理使用

### 3.1 Nacos配置

#### 动态配置
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

#### 配置文件（Nacos）
```yaml
# Data ID: user-org-service.yaml
# Group: DEFAULT_GROUP

app:
  config:
    name: zhuji-user-org
    max-connections: 100
    feature-enabled: true
```

### 3.2 Sentinel限流熔断

#### 注解方式
```java
@SentinelResource(
    value = "getUser",
    blockHandler = "handleBlock",
    fallback = "handleFallback"
)
public User getUser(Long id) {
    return userRepository.findById(id);
}

// 限流处理
public User handleBlock(Long id, BlockException e) {
    log.warn("触发限流: userId={}", id);
    return User.builder().name("系统繁忙").build();
}

// 降级处理
public User handleFallback(Long id, Throwable t) {
    log.error("触发降级: userId={}", id, t);
    return User.builder().name("服务降级").build();
}
```

#### 代码方式
```java
public void process() {
    Entry entry = null;
    try {
        entry = SphU.entry("my-resource");
        // 执行业务逻辑
    } catch (BlockException e) {
        // 处理限流
        log.warn("触发限流");
    } finally {
        if (entry != null) {
            entry.exit();
        }
    }
}
```

### 3.3 Seata分布式事务

#### AT模式（推荐）
```java
@GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
public void createOrder(OrderRequest request) {
    // 创建订单
    Order order = orderService.create(request);
    
    // 扣减库存
    inventoryService.deduct(request.getProductId(), request.getQuantity());
    
    // 扣减余额
    accountService.deduct(request.getUserId(), request.getAmount());
}
```

#### TCC模式
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

## 4. 监控体系使用

### 4.1 SkyWalking链路追踪

#### 启动配置
```bash
java -javaagent:/path/to/skywalking-agent.jar \
     -Dskywalking.agent.service_name=user-org-service \
     -Dskywalking.collector.backend_service=localhost:11800 \
     -Dskywalking.logging.level=INFO \
     -jar user-org-service.jar
```

#### 自定义Span
```java
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.apache.skywalking.apm.toolkit.trace.TracingContext;

public void customSpan() {
    ActiveSpan activeSpan = TraceContext.activeSpan();
    activeSpan.tag("custom-tag", "tag-value");
    activeSpan.info("自定义日志信息");
}
```

### 4.2 Prometheus指标监控

#### 自定义指标
```java
@Component
public class CustomMetrics {
    
    private final Counter requestCounter;
    private final Gauge activeConnections;
    private final Timer requestTimer;
    
    public CustomMetrics(MeterRegistry registry) {
        this.requestCounter = Counter.builder("api.requests")
            .description("API请求总数")
            .tag("service", "user-org")
            .register(registry);
        
        this.activeConnections = Gauge.builder("active.connections")
            .description("活跃连接数")
            .register(registry);
        
        this.requestTimer = Timer.builder("api.request.duration")
            .description("API请求耗时")
            .register(registry);
    }
    
    public void incrementRequest() {
        requestCounter.increment();
    }
    
    public void recordTimer() {
        requestTimer.record(() -> {
            // 执行业务逻辑
        });
    }
}
```

### 4.3 Grafana可视化

#### 访问Grafana
- URL: http://localhost:3000
- 用户名: admin
- 密码: admin

#### 导入预设Dashboard
1. 登录Grafana
2. 点击 "+" -> "Import"
3. 输入Dashboard ID（如：4701 - JVM监控）
4. 选择Prometheus数据源
5. 点击"Import"

## 5. 日志体系使用

### 5.1 日志规范

#### 日志级别使用
- **ERROR**: 错误日志，需要立即处理
- **WARN**: 警告日志，可能存在问题
- **INFO**: 重要业务日志
- **DEBUG**: 调试日志（生产环境关闭）

#### 日志格式
```java
@Slf4j
@Service
public class UserService {
    
    public User createUser(UserRequest request) {
        log.info("[创建用户] 开始处理, request={}", request);
        
        try {
            User user = buildUser(request);
            userRepository.save(user);
            
            log.info("[创建用户] 处理成功, userId={}", user.getId());
            return user;
        } catch (Exception e) {
            log.error("[创建用户] 处理失败, request={}", request, e);
            throw new BusinessException("创建用户失败", e);
        }
    }
}
```

### 5.2 TraceId链路追踪

#### 自动注入（已配置）
所有请求自动注入TraceId，格式：
```
2024-01-15 10:30:45.123 [http-nio-8081-exec-1] INFO  c.z.u.controller.UserController [a1b2c3d4] - 用户登录成功
```

#### 手动获取TraceId
```java
import org.slf4j.MDC;

public String getTraceId() {
    return MDC.get("traceId");
}
```

### 5.3 ELK日志查询

#### 访问Kibana
- URL: http://localhost:5601

#### 查询示例
```
# 查询特定服务的日志
app_name: "user-org-service"

# 查询错误日志
level: "ERROR"

# 查询特定TraceId的日志
traceId: "a1b2c3d4"

# 组合查询
app_name: "user-org-service" AND level: "ERROR"
```

## 6. 完整示例

### 示例场景：创建订单

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
        log.info("[创建订单] 开始处理, request={}", request);
        
        // 创建订单
        Order order = Order.builder()
            .userId(request.getUserId())
            .productId(request.getProductId())
            .quantity(request.getQuantity())
            .amount(request.getAmount())
            .status(OrderStatus.CREATED)
            .build();
        orderRepository.save(order);
        
        // 扣减库存
        inventoryService.deduct(request.getProductId(), request.getQuantity());
        
        // 扣减余额
        accountService.deduct(request.getUserId(), request.getAmount());
        
        // 发送消息
        sendOrderMessage(order);
        
        log.info("[创建订单] 处理成功, orderId={}", order.getId());
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
        log.warn("[创建订单] 触发限流, request={}", request);
        throw new BusinessException("系统繁忙，请稍后重试");
    }
}
```

## 7. 最佳实践

### 7.1 缓存使用
- ✅ 合理设置过期时间
- ✅ 使用分布式锁防止缓存击穿
- ✅ 缓存空值防止缓存穿透
- ✅ 使用批量操作减少网络开销

### 7.2 消息队列使用
- ✅ 消息幂等性处理
- ✅ 消息重试机制
- ✅ 死信队列处理失败消息
- ✅ 消息顺序性保证

### 7.3 分布式事务使用
- ✅ 优先使用AT模式
- ✅ 合理设置事务超时时间
- ✅ 做好幂等性处理
- ✅ 异常处理和回滚

### 7.4 监控告警
- ✅ 设置合理的告警阈值
- ✅ 关键指标监控
- ✅ 日志告警规则
- ✅ 定期检查监控面板