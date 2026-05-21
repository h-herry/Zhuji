package com.zhuji.userorg.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtService jwtService;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate,
                                 JwtService jwtService) {
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }

    public void addToBlacklist(String token) {
        try {
            long expiration = jwtService.getAccessTokenExpiration();
            String blacklistKey = BLACKLIST_PREFIX + token;

            redisTemplate.opsForValue().set(blacklistKey, "1", expiration, TimeUnit.SECONDS);
            log.debug("Token已加入黑名单, expiration={}秒", expiration);
        } catch (Exception e) {
            log.error("添加Token到黑名单失败", e);
        }
    }

    public boolean isBlacklisted(String token) {
        try {
            String blacklistKey = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            log.error("检查Token黑名单状态失败", e);
            return false;
        }
    }

    public void logout(String token) {
        addToBlacklist(token);
    }
}
