package com.zhuji.common.cache.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RedisDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisDistributedLock(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String LOCK_PREFIX = "zhuji:lock:";
    private static final String REENTRANT_LOCK_PREFIX = "zhuji:reentrant:lock:";
    private static final long DEFAULT_EXPIRE_TIME = 30000L;
    private static final long DEFAULT_WAIT_TIME = 10000L;
    private static final long DEFAULT_SLEEP_TIME = 100L;

    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_EXPIRE_TIME, DEFAULT_WAIT_TIME, DEFAULT_SLEEP_TIME);
    }

    public boolean tryLock(String key, long expireTime, long waitTime, long sleepTime) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            while (System.currentTimeMillis() - startTime < waitTime) {
                Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.MILLISECONDS);

                if (Boolean.TRUE.equals(success)) {
                    log.debug("获取锁成功, key: {}", key);
                    return true;
                }

                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("获取锁被中断, key: {}", key);
            return false;
        }

        log.debug("获取锁失败, key: {}", key);
        return false;
    }

    public boolean unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        try {
            Object value = redisTemplate.opsForValue().get(lockKey);
            if (value != null) {
                return Boolean.TRUE.equals(redisTemplate.delete(lockKey));
            }
            return false;
        } catch (Exception e) {
            log.error("释放锁异常, key: {}", key, e);
            return false;
        }
    }

    public void lock(String key) {
        while (!tryLock(key)) {
            try {
                Thread.sleep(DEFAULT_SLEEP_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("获取锁被中断", e);
            }
        }
    }

    public boolean tryLockWithTimeout(String key, long timeout, TimeUnit unit) {
        long millis = unit.toMillis(timeout);
        return tryLock(key, DEFAULT_EXPIRE_TIME, millis, DEFAULT_SLEEP_TIME);
    }

    public void lockInterruptibly(String key) throws InterruptedException {
        while (!tryLock(key)) {
            Thread.sleep(DEFAULT_SLEEP_TIME);
        }
    }

    public boolean tryReentrantLock(String key, String ownerId) {
        String lockKey = REENTRANT_LOCK_PREFIX + key;
        Object currentOwner = redisTemplate.opsForValue().get(lockKey);

        if (currentOwner == null) {
            redisTemplate.opsForValue().set(lockKey, ownerId, DEFAULT_EXPIRE_TIME, TimeUnit.MILLISECONDS);
            return true;
        }

        if (ownerId.equals(currentOwner.toString())) {
            redisTemplate.expire(lockKey, DEFAULT_EXPIRE_TIME, TimeUnit.MILLISECONDS);
            return true;
        }

        return false;
    }

    public void unlockReentrantLock(String key, String ownerId) {
        String lockKey = REENTRANT_LOCK_PREFIX + key;
        Object currentOwner = redisTemplate.opsForValue().get(lockKey);

        if (currentOwner != null && ownerId.equals(currentOwner.toString())) {
            redisTemplate.delete(lockKey);
        }
    }
}