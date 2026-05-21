package com.zhuji.userorg.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class UserLockService {

    private static final String LOCK_KEY_PREFIX = "user:lock:";
    private static final String FAIL_COUNT_PREFIX = "user:fail:";
    private static final long LOCK_MINUTES = 30;

    @Value("${user.lock.max.fail.count:5}")
    private int maxFailCount;

    private final RedisTemplate<String, Object> redisTemplate;

    public UserLockService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isLocked(Long userId) {
        String lockKey = LOCK_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    public void incrementFailCount(Long userId) {
        String failKey = FAIL_COUNT_PREFIX + userId;
        Long count = redisTemplate.opsForValue().increment(failKey);
        redisTemplate.expire(failKey, 1, TimeUnit.HOURS);
        
        if (count != null && count >= maxFailCount) {
            lockUser(userId);
        }
    }

    public void lockUser(Long userId) {
        String lockKey = LOCK_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(lockKey, System.currentTimeMillis(), LOCK_MINUTES, TimeUnit.MINUTES);
    }

    public void unlockUser(Long userId) {
        String lockKey = LOCK_KEY_PREFIX + userId;
        String failKey = FAIL_COUNT_PREFIX + userId;
        redisTemplate.delete(lockKey);
        redisTemplate.delete(failKey);
    }

    public void resetFailCount(Long userId) {
        String failKey = FAIL_COUNT_PREFIX + userId;
        redisTemplate.delete(failKey);
    }
}
