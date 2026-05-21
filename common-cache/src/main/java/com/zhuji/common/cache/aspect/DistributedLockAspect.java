package com.zhuji.common.cache.aspect;

import com.zhuji.common.cache.annotation.DistributedLock;
import com.zhuji.common.cache.util.RedisDistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
public class DistributedLockAspect {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);

    private final RedisDistributedLock redisDistributedLock;

    public DistributedLockAspect(RedisDistributedLock redisDistributedLock) {
        this.redisDistributedLock = redisDistributedLock;
    }

    @Around("@annotation(com.zhuji.common.cache.annotation.DistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = distributedLock.key();
        long expireTime = distributedLock.expireTime();
        long waitTime = distributedLock.waitTime();
        long sleepTime = distributedLock.sleepTime();

        String lockKey = buildKey(key, joinPoint);

        boolean locked = false;
        String ownerId = null;
        try {
            if ("reentrant".equals(distributedLock.lockType())) {
                ownerId = UUID.randomUUID().toString();
                locked = redisDistributedLock.tryReentrantLock(lockKey, ownerId);
            } else {
                locked = redisDistributedLock.tryLock(lockKey, expireTime, waitTime, sleepTime);
            }

            if (!locked) {
                log.warn("获取分布式锁失败, key: {}", lockKey);
                throw new RuntimeException("系统繁忙，请稍后重试");
            }

            log.debug("获取分布式锁成功, key: {}", lockKey);
            return joinPoint.proceed();
        } finally {
            if (locked) {
                if ("reentrant".equals(distributedLock.lockType())) {
                    redisDistributedLock.unlockReentrantLock(lockKey, ownerId);
                } else {
                    redisDistributedLock.unlock(lockKey);
                }
                log.debug("释放分布式锁, key: {}", lockKey);
            }
        }
    }

    private String buildKey(String key, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (paramNames == null || args == null) {
            return key;
        }

        String result = key;
        for (int i = 0; i < paramNames.length; i++) {
            String placeholder = "{" + paramNames[i] + "}";
            if (result.contains(placeholder)) {
                Object arg = args[i];
                result = result.replace(placeholder, arg != null ? arg.toString() : "null");
            }
        }

        return result;
    }
}