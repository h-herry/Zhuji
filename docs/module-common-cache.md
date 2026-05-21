# 公共缓存模块 (common-cache)

## 1. 模块概述

common-cache模块提供统一的Redis缓存抽象和分布式锁功能，封装了分布式场景下的缓存操作和并发控制。

### 1.1 主要功能

- **Redis连接管理**：自动配置多数据源Redis连接
- **缓存操作封装**：简化缓存CRUD操作
- **分布式锁**：基于Redis的分布式锁实现
- **缓存工具类**：常用缓存操作工具方法

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Data Redis | 3.2.x | Redis操作 |
| Redisson | 3.25.x | 分布式锁 |
| Lettuce | - | Redis客户端 |

---

## 2. 核心组件

### 2.1 RedisConfig - Redis配置

```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 配置Redis连接工厂
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        // 配置RedisTemplate，支持对象序列化
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 配置缓存管理器
    }
}
```

### 2.2 DistributedLock - 分布式锁

```java
public class DistributedLock {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "lock:";

    public boolean tryLock(String key, long expireTime, TimeUnit unit) {
        String lockKey = LOCK_PREFIX + key;
        Boolean result = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "1", expireTime, unit);
        return Boolean.TRUE.equals(result);
    }

    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        redisTemplate.delete(lockKey);
    }
}
```

### 2.3 CacheKeys - 缓存Key常量

```java
public class CacheKeys {

    public static String userKey(Long userId) {
        return "user:" + userId;
    }

    public static String tokenKey(Long userId) {
        return "auth:token:" + userId;
    }

    public static String orgKey(Long orgId) {
        return "org:" + orgId;
    }

    public static String permissionKey(Long roleId) {
        return "permission:role:" + roleId;
    }

    public static String userRolesKey(Long userId) {
        return "user-roles:" + userId;
    }

    public static String userPermissionsKey(Long userId) {
        return "user-permissions:" + userId;
    }

    public static String configKey(String configKey) {
        return "config:" + configKey;
    }

    public static String configTypeKey(String configType) {
        return "config:type:" + configType;
    }

    public static String i18nMessageKey(String messageKey, String locale) {
        return "i18n-messages:" + messageKey + ":" + locale;
    }

    public static String i18nLocaleKey(String locale) {
        return "i18n-messages:locale:" + locale;
    }

    public static String i18nModuleKey(String locale, String module) {
        return "i18n-messages:module:" + locale + ":" + module;
    }
}
```

---

## 3. 使用示例

### 3.1 缓存使用

```java
@Service
public class UserService {

    @Cacheable(value = "user", key = "#id")
    public UserVO getUserById(Long id) {
        // 首次调用后结果会被缓存
        return userMapper.selectById(id);
    }

    @CacheEvict(value = "user", key = "#id")
    public void updateUser(Long id, UserDTO dto) {
        // 更新后清除缓存
        userMapper.updateById(dto);
    }

    @CacheEvict(value = "user", allEntries = true)
    public void clearAllUsers() {
        // 清除所有用户缓存
    }
}
```

### 3.2 分布式锁使用

```java
@Service
public class OrderService {

    @Autowired
    private DistributedLock distributedLock;

    public void createOrder(Long userId, OrderDTO orderDTO) {
        String lockKey = "order:create:" + userId;

        if (distributedLock.tryLock(lockKey, 30, TimeUnit.SECONDS)) {
            try {
                // 业务逻辑
                orderMapper.insert(order);
            } finally {
                distributedLock.unlock(lockKey);
            }
        } else {
            throw new BusinessException("请求过于频繁，请稍后重试");
        }
    }
}
```

### 3.3 RedisTemplate直接操作

```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

public void setUserSession(Long userId, UserSession session) {
    String key = "session:user:" + userId;
    redisTemplate.opsForValue().set(key, session, 30, TimeUnit.MINUTES);
}

public UserSession getUserSession(Long userId) {
    String key = "session:user:" + userId;
    return (UserSession) redisTemplate.opsForValue().get(key);
}

public void deleteUserSession(Long userId) {
    String key = "session:user:" + userId;
    redisTemplate.delete(key);
}
```

---

## 4. 配置说明

### 4.1 application.yml

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: # Redis密码
      database: 0
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
```

### 4.2 多Redis配置

```java
@Bean
@ConfigurationProperties(prefix = "spring.data.redis.cluster")
public RedisClusterConfiguration redisClusterConfiguration() {
    return new RedisClusterConfiguration();
}
```

---

## 5. 缓存策略

### 5.1 缓存过期时间

| 缓存类型 | 缓存Key | 过期时间 | 说明 |
|----------|---------|----------|------|
| 用户信息 | user:{userId} | 1小时 | 频繁访问 |
| 用户角色 | user-roles:{userId} | 30分钟 | 角色变更时清除 |
| 用户权限 | user-permissions:{userId} | 30分钟 | 权限变更时清除 |
| 认证Token | auth:token:{userId} | 24小时 | 与JWT过期时间一致 |
| 组织信息 | org:{orgId} | 30分钟 | 相对稳定 |
| 角色权限 | permission:role:{roleId} | 1小时 | 权限变更时清除 |
| 全局配置 | config:{configKey} | 30分钟 | 配置变更时清除 |
| 配置类型 | config:type:{configType} | 30分钟 | 配置变更时清除 |
| 多语言消息 | i18n-messages:{key}:{locale} | 1小时 | 消息变更时清除 |
| 语言缓存 | i18n-messages:locale:{locale} | 1小时 | 消息变更时清除 |
| 模块消息 | i18n-messages:module:{locale}:{module} | 1小时 | 消息变更时清除 |

### 5.2 缓存淘汰策略

- **LRU**（Least Recently Used）：最近最少使用
- **TTL**（Time To Live）：定时过期
- **主动清除**：数据变更时主动清除相关缓存

### 5.3 缓存一致性

- **Cache-Aside模式**：先更新数据库，再删除缓存
- **双写一致性**：使用分布式锁保证并发场景下的数据一致性
- **多实例同步**：通过Redis Pub/Sub实现跨实例缓存刷新通知

---

## 6. 注意事项

1. **序列化**：Redis存储对象需实现Serializable
2. **锁粒度**：分布式锁key要保证唯一性
3. **过期时间**：合理设置缓存过期时间，避免内存溢出
4. **分布式**：缓存一致性问题需业务层处理
