package com.zhuji.userorg.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.zhuji.common.cache.annotation.DistributedLock;
import com.zhuji.common.cache.util.RedisDistributedLock;
import com.zhuji.common.mq.message.BaseMessage;
import com.zhuji.common.mq.producer.MessageProducer;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InfrastructureDemoService {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureDemoService.class);

    private final RedisDistributedLock redisDistributedLock;
    private final MessageProducer messageProducer;

    public InfrastructureDemoService(RedisDistributedLock redisDistributedLock, MessageProducer messageProducer) {
        this.redisDistributedLock = redisDistributedLock;
        this.messageProducer = messageProducer;
    }

    @DistributedLock(key = "user:update:{userId}", expireTime = 30000)
    public void updateUserWithLock(Long userId, String username) {
        log.info("使用分布式锁更新用户: userId={}, username={}", userId, username);
    }

    public void updateUserWithManualLock(Long userId) {
        String lockKey = "user:manual:" + userId;
        boolean locked = false;

        try {
            locked = redisDistributedLock.tryLock(lockKey);
            if (locked) {
                log.info("手动获取锁成功，执行业务逻辑");
            } else {
                log.warn("获取锁失败，请稍后重试");
            }
        } finally {
            if (locked) {
                redisDistributedLock.unlock(lockKey);
            }
        }
    }

    @SentinelResource(value = "getUserById", blockHandler = "handleBlock", fallback = "handleFallback")
    public String getUserById(Long userId) {
        log.info("查询用户: userId={}", userId);
        return "User-" + userId;
    }

    public String handleBlock(Long userId, Exception e) {
        log.warn("Sentinel限流触发: userId={}", userId);
        return "系统繁忙，请稍后重试";
    }

    public String handleFallback(Long userId, Throwable t) {
        log.error("Sentinel降级触发: userId={}", userId, t);
        return "服务降级，请稍后重试";
    }

    public void sendMessageToQueue(String topic, String message) {
        BaseMessage baseMessage = BaseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .topic(topic)
                .body(message)
                .producer("user-org-service")
                .sendTime(LocalDateTime.now())
                .retryCount(0)
                .build();

        messageProducer.send(baseMessage);
        log.info("消息发送成功: topic={}, messageId={}", topic, baseMessage.getMessageId());
    }

    public void sendDelayMessage(String topic, String message, long delayMs) {
        messageProducer.sendWithDelay(topic, message, delayMs);
        log.info("延迟消息发送成功: topic={}, delay={}ms", topic, delayMs);
    }

    @GlobalTransactional(name = "create-user-org", rollbackFor = Exception.class)
    public void createUserAndOrgWithTransaction(Long userId, Long orgId) {
        log.info("分布式事务开始: userId={}, orgId={}", userId, orgId);

        log.info("创建用户: userId={}", userId);

        log.info("创建组织: orgId={}", orgId);

        log.info("分布式事务提交成功");
    }
}