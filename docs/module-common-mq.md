# 公共消息队列模块 (common-mq)

## 1. 模块概述

common-mq模块提供统一的消息队列抽象，支持RabbitMQ和Kafka两种消息中间件，实现消息发送、接收、事务消息等功能。

### 1.1 主要功能

- **RabbitMQ生产者**：消息发送、延迟消息
- **RabbitMQ消费者**：消息接收、消息确认
- **Kafka支持**：Kafka消息发送和接收
- **事务消息**：确保消息与业务一致性

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring AMQP | 3.1.x | RabbitMQ抽象 |
| Kafka | 3.6.x | Kafka客户端 |
| Jackson | 2.15.x | JSON序列化 |

---

## 2. RabbitMQ组件

### 2.1 RabbitMQConfig - 配置类

```java
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "zhuji.exchange";
    public static final String QUEUE_NAME = "zhuji.queue";
    public static final String ROUTING_KEY = "zhuji.routing.key";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }
}
```

### 2.2 RabbitMQProducer - 生产者

```java
@Service
public class RabbitMQProducer {

    public void sendMessage(Object message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY,
            message
        );
    }

    public void sendDelayMessage(Object message, int delayMs) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY,
            message,
            msg -> {
                msg.getMessageProperties().setDelay(delayMs);
                return msg;
            }
        );
    }
}
```

### 2.3 RabbitMQConsumer - 消费者

```java
@Component
public class RabbitMQConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleMessage(Object message) {
        try {
            // 处理消息
            log.info("Received message: {}", message);
        } catch (Exception e) {
            log.error("Error processing message", e);
            throw e;
        }
    }
}
```

---

## 3. 消息类型

### 3.1 普通消息

```java
// 发送
rabbitTemplate.convertAndSend("exchange", "routing.key", message);

// 接收
@RabbitListener(queues = "queue")
public void handle(String message) {
    // 处理消息
}
```

### 3.2 延迟消息

```java
// 发送延迟10秒的消息
rabbitTemplate.convertAndSend("exchange", "routing.key", message, msg -> {
    msg.getMessageProperties().setDelay(10000);
    return msg;
});
```

### 3.3 事务消息

```java
@Transactional
public void sendTransactionalMessage() {
    // 业务操作
    userService.createUser(user);

    // 发送消息
    rabbitTemplate.convertAndSend("exchange", "routing.key", message);
}
```

---

## 4. 配置说明

### 4.1 RabbitMQ配置

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 10
        concurrency: 5
        max-concurrency: 10
```

### 4.2 Kafka配置

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
    consumer:
      group-id: zhuji-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

---

## 5. 使用场景

### 5.1 异步处理

用户注册后发送欢迎邮件，可以使用消息队列异步发送，不阻塞主流程。

### 5.2 系统解耦

订单完成后触发库存扣减、物流通知等，通过消息队列解耦。

### 5.3 流量削峰

秒杀活动时，将请求放入消息队列，后台服务按能力消费。

### 5.4 延迟重试

支付失败时，延迟一定时间后重试。

---

## 6. 注意事项

1. **消息幂等**：消费者需处理重复消息
2. **消息顺序**：需要保证顺序的消息使用相同key
3. **事务一致**：消息发送失败需回滚业务
4. **错误处理**：死信队列处理失败消息
