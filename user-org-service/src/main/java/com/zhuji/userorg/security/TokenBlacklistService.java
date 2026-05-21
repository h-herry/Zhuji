package com.zhuji.userorg.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
        long expiration = jwtService.getAccessTokenExpiration();
        String blacklistKey = BLACKLIST_PREFIX + token;

        redisTemplate.opsForValue().set(blacklistKey, "1", expiration, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String token) {
        String blacklistKey = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }

    public void logout(String token) {
        addToBlacklist(token);
    }
}
