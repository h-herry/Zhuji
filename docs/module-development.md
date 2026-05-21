# 筑基 (Zhuji) 模块开发指南

## 目录
1. [快速开始](#1-快速开始)
2. [模块开发规范](#2-模块开发规范)
3. [通用模块开发](#3-通用模块开发)
4. [业务模块开发](#4-业务模块开发)
5. [API 开发规范](#5-api-开发规范)
6. [测试规范](#6-测试规范)
7. [最佳实践](#7-最佳实践)
8. [常见问题](#8-常见问题)

---

## 1. 快速开始

### 1.1 环境准备
**必需环境**:
- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.x+
- Nacos 2.3+

**推荐 IDE**:
- IntelliJ IDEA (推荐)
- VS Code + Java 插件

### 1.2 创建新模块

**步骤 1: 使用 Maven 原型创建模块**
```bash
cd zhuji-project
mvn archetype:generate \
  -DarchetypeGroupId=com.zhuji \
  -DarchetypeArtifactId=zhuji-module-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=com.zhuji.modules \
  -DartifactId=your-module \
  -Dversion=1.0.0-SNAPSHOT
```

**步骤 2: 手动创建模块结构**
```
your-module/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/zhuji/modules/yourmodule/
    │   │   ├── controller/      # API 控制器
    │   │   ├── service/         # 业务服务接口
    │   │   │   └── impl/       # 业务服务实现
    │   │   ├── repository/      # 数据访问层
    │   │   ├── domain/          # 实体类
    │   │   ├── dto/             # 数据传输对象
    │   │   ├── vo/              # 视图对象
    │   │   ├── config/          # 配置类
    │   │   └── YourModuleApplication.java  # 启动类
    │   └── resources/
    │       ├── application.yml
    │       ├── mapper/          # MyBatis Mapper XML
    │       └── bootstrap.yml
    └── test/
        └── java/com/zhuji/modules/yourmodule/
            └── YourModuleTest.java
```

**步骤 3: 配置 pom.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.zhuji</groupId>
        <artifactId>zhuji-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>your-module</artifactId>
    <packaging>jar</packaging>
    <name>your-module</name>
    <description>你的模块描述</description>
    
    <dependencies>
        <!-- 依赖公共模块 -->
        <dependency>
            <groupId>com.zhuji</groupId>
            <artifactId>common-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.zhuji</groupId>
            <artifactId>common-security</artifactId>
        </dependency>
        <dependency>
            <groupId>com.zhuji</groupId>
            <artifactId>common-cache</artifactId>
        </dependency>
    </dependencies>
</project>
```

---

## 2. 模块开发规范

### 2.1 包结构规范
```
com.zhuji.modules.{module-name}/
├── controller/       # API 层：接收请求，参数校验，返回响应
├── service/          # 业务层接口
│   └── impl/       # 业务层实现
├── repository/       # 数据访问层接口
│   └── impl/       # 数据访问层实现（可选）
├── domain/           # 实体模型（JPA 注解）
├── dto/              # 数据传输对象（接收请求）
├── vo/               # 视图对象（返回响应）
├── config/           # Spring 配置类
├── aspect/           # AOP 切面
├── listener/         # 事件监听器
├── handler/          # 处理器（如异常处理器）
├── util/             # 工具类
└── constants/        # 常量定义
```

### 2.2 命名规范
| 类型 | 命名规则 | 示例 |
|------|---------|------|
| 控制器 | XxxController | UserController |
| 服务接口 | XxxService | UserService |
| 服务实现 | XxxServiceImpl | UserServiceImpl |
| 数据访问接口 | XxxRepository | UserRepository |
| 实体类 | Xxx | User |
| DTO | XxxDTO / XxxRequest / XxxParam | UserDTO / CreateUserRequest |
| VO | XxxVO / XxxResponse | UserVO / UserResponse |
| Mapper (MyBatis) | XxxMapper | UserMapper |

### 2.3 代码规范
**使用 Lombok 简化代码**:
```java
@Data               // getter + setter
@Builder            // 建造者模式
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
public class UserDTO {
    private Long id;
    private String username;
    private String email;
}
```

**使用 Hutool 工具库**:
```java
// 字符串工具
StrUtil.isNotBlank(str);

// 日期工具
DateUtil.date();

// JSON 工具
JSONUtil.toJsonStr(obj);
```

---

## 3. 通用模块开发

### 3.1 common-core 模块
**职责**: 提供全局通用的常量、枚举、异常、工具类

**目录结构**:
```
common-core/
├── constants/       # 全局常量
├── enums/           # 全局枚举
├── exception/       # 全局异常
├── util/            # 工具类
└── base/            # 基础抽象类
```

**示例代码**:

*全局异常基类*:
```java
package com.zhuji.common.core.exception;

public class BusinessException extends RuntimeException {
    private Integer code;
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    // getter
}
```

*全局错误码枚举*:
```java
package com.zhuji.common.core.enums;

public enum ErrorCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误");
    
    private Integer code;
    private String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    // getters
}
```

### 3.2 common-security 模块
**职责**: 提供统一的安全认证和授权功能

**核心类**:

*JWT 工具类*:
```java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;
    
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
```

*Spring Security 配置*:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
```

### 3.3 common-cache 模块
**职责**: 封装 Redis 操作，提供缓存注解，支持分布式锁

**核心类**:

*Redis 配置*:
```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

*缓存注解*:
```java
@Service
public class UserService {
    @Cacheable(value = "user", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    @CacheEvict(value = "user", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

### 3.4 common-i18n 模块
**职责**: 提供多语言支持，支持数据库消息存储和缓存

**核心类**:

*国际化配置*:
```java
@Configuration
public class I18nConfig {
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:i18n/messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }
}
```

*消息工具类*:
```java
@Component
public class I18nMessageUtil {
    @Autowired
    private MessageSource messageSource;
    
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
```

### 3.5 common-mq 模块
**职责**: 封装消息队列操作，支持 RabbitMQ

**核心类**:

*消息发送者*:
```java
@Component
public class RabbitMQSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void send(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
```

*消息监听器*:
```java
@Component
public class RabbitMQListener {
    @RabbitListener(queues = "example.queue")
    public void handleMessage(String message) {
        // 处理消息
    }
}
```

### 3.6 common-task 模块
**职责**: 提供定时任务支持，支持任务日志

**核心类**:

*定时任务配置*:
```java
@Configuration
@EnableScheduling
public class TaskConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("task-");
        return scheduler;
    }
}
```

*定时任务示例*:
```java
@Component
public class ExampleTask {
    @Scheduled(cron = "0 0 * * * ?")
    @ScheduledLog(description = "示例定时任务")
    public void execute() {
        // 任务逻辑
    }
}
```

### 3.7 common-monitor 模块
**职责**: 提供监控支持，集成 Prometheus 和 Zipkin

**核心类**:

*监控配置*:
```java
@Configuration
public class MonitorConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "zhuji");
    }
}
```

### 3.8 common-crypto 模块
**职责**: 提供加密支持，集成 Jasypt

**核心类**:

*加密工具类*:
```java
@Component
public class CryptoUtil {
    @Autowired
    private StringEncryptor stringEncryptor;
    
    public String encrypt(String plainText) {
        return stringEncryptor.encrypt(plainText);
    }
    
    public String decrypt(String encryptedText) {
        return stringEncryptor.decrypt(encryptedText);
    }
}
```

### 3.9 common-export 模块
**职责**: 提供导出支持，集成 EasyExcel

**核心类**:

*Excel导出工具*:
```java
@Component
public class ExcelExportUtil {
    public void export(HttpServletResponse response, List<?> data, Class<?> clazz, String fileName) {
        // 使用 EasyExcel 导出
    }
}
```

### 3.10 common-audit 模块
**职责**: 提供审计日志支持

**核心类**:

*审计日志注解*:
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    String description();
    String module() default "";
}
```

*审计日志切面*:
```java
@Aspect
@Component
public class AuditLogAspect {
    @Around("@annotation(auditLog)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        // 记录审计日志
        return joinPoint.proceed();
    }
}
```

---

## 4. 业务模块开发

### 4.1 用户组织模块 (user-org-service)
**实体设计**:

*User 实体*:
```java
@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    private String password;
    private String email;
    private String phone;
    private Integer status; // 0-禁用 1-启用
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

*OrgUnit 实体（树形结构）*:
```java
@Data
@TableName("sys_org_unit")
public class OrgUnit {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private Long parentId; // 父级组织 ID
    private Integer level;  // 层级
    private String path;     // 层级路径，如 /1/3/5
    
    @TableField(exist = false)
    private List<OrgUnit> children; // 子节点（非数据库字段）
}
```

**Repository 层**:
```java
@Mapper
public interface UserRepository extends BaseMapper<User> {
    // 自定义 SQL（可选）
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
}
```

**Service 层**:
```java
public interface UserService {
    User getUserById(Long id);
    Page<User> listUsers(Page<User> page, User query);
    User createUser(UserDTO userDTO);
    void updateUser(UserDTO userDTO);
    void deleteUser(Long id);
}

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    
    @Override
    public User getUserById(Long id) {
        return userRepository.selectById(id);
    }
    
    @Override
    public Page<User> listUsers(Page<User> page, User query) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
                .like(StrUtil.isNotBlank(query.getUsername()), User::getUsername, query.getUsername())
                .eq(ObjUtil.isNotNull(query.getStatus()), User::getStatus, query.getStatus())
                .orderByDesc(User::getCreateTime);
        return userRepository.selectPage(page, wrapper);
    }
}
```

**Controller 层**:
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
        return ApiResponse.success(vo);
    }
    
    @PostMapping
    public ApiResponse<Void> createUser(@RequestBody @Valid CreateUserRequest request) {
        UserDTO dto = BeanUtil.copyProperties(request, UserDTO.class);
        userService.createUser(dto);
        return ApiResponse.success();
    }
}
```

---

## 5. API 开发规范

### 5.1 统一响应格式
**ApiResponse 类**:
```java
@Data
@Builder
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
```

### 5.2 参数校验
**使用 Bean Validation**:
```java
@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在 3-20 之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 之间")
    private String password;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
```

**全局异常处理**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ApiResponse.error(400, message);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }
}
```

### 5.3 API 版本管理
**URL 路径版本控制**:
```
/api/v1/users     # 版本 1
/api/v2/users     # 版本 2
```

**请求头版本控制**:
```java
@RequestMapping(value = "/users", headers = "X-API-Version=1")
public class UserControllerV1 { }

@RequestMapping(value = "/users", headers = "X-API-Version=2")
public class UserControllerV2 { }
```

---

## 6. 测试规范

### 6.1 单元测试
**Service 层测试**:
```java
@SpringBootTest
class UserServiceTest {
    @MockBean
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Test
    void testGetUserById() {
        // 准备数据
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test");
        when(userRepository.selectById(1L)).thenReturn(mockUser);
        
        // 执行测试
        User result = userService.getUserById(1L);
        
        // 验证结果
        assertEquals("test", result.getUsername());
        verify(userRepository).selectById(1L);
    }
}
```

### 6.2 集成测试
**Controller 层测试**:
```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

### 6.3 压力测试
**使用 JMeter**:
1. 创建测试计划
2. 添加线程组（模拟用户）
3. 添加 HTTP 请求（调用 API）
4. 添加聚合报告（查看结果）

---

## 7. 最佳实践

### 7.1 DDD 领域驱动设计
**分层架构**:
```
interfaces/      # 用户接口层：API 控制器
application/     # 应用层：用例编排（无业务逻辑）
domain/          # 领域层：核心业务逻辑（实体、值对象、领域服务）
infrastructure/  # 基础设施层：数据访问、外部服务调用
```

**示例**:
```java
// interfaces 层
@RestController
public class UserController {
    private final UserApplicationService userAppService;
    
    @PostMapping("/users")
    public ApiResponse<Void> createUser(@RequestBody CreateUserRequest request) {
        CreateUserCommand command = BeanUtil.copyProperties(request, CreateUserCommand.class);
        userAppService.createUser(command);
        return ApiResponse.success();
    }
}

// application 层
@Service
@RequiredArgsConstructor
public class UserApplicationService {
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;
    
    @Transactional
    public void createUser(CreateUserCommand command) {
        // 1. 校验业务规则
        userDomainService.checkUsernameUnique(command.getUsername());
        
        // 2. 创建领域对象
        User user = User.create(command.getUsername(), command.getPassword());
        
        // 3. 持久化
        userRepository.save(user);
        
        // 4. 发布领域事件
        userDomainService.publishEvent(new UserCreatedEvent(user.getId()));
    }
}

// domain 层
@Service
public class UserDomainService {
    public void checkUsernameUnique(String username) {
        // 业务逻辑：检查用户名唯一性
    }
    
    public void publishEvent(Object event) {
        // 发布领域事件
    }
}

// infrastructure 层
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserMapper userMapper;
    
    @Override
    public void save(User user) {
        userMapper.insert(user);
    }
}
```

### 7.2 事务管理
**声明式事务**:
```java
@Service
public class UserService {
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserDTO dto) {
        // 业务逻辑
    }
}
```

**分布式事务 (Seata)**:
```java
@Service
public class OrderService {
    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public void createOrder(CreateOrderRequest request) {
        // 1. 创建订单（订单服务）
        orderClient.createOrder(request);
        
        // 2. 扣减库存（库存服务）
        inventoryClient.deductInventory(request);
        
        // 3. 扣减账户余额（账户服务）
        accountClient.deductBalance(request);
    }
}
```

### 7.3 异步处理
**Spring Async**:
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        return executor;
    }
}

@Service
public class NotificationService {
    @Async("taskExecutor")
    public CompletableFuture<Void> sendEmail(String to, String subject, String body) {
        // 发送邮件逻辑
        return CompletableFuture.completedFuture(null);
    }
}
```

---

## 8. 常见问题

### Q1: 如何集成 MyBatis-Plus？
**A**: 添加依赖并配置：
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
</dependency>
```

```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
```

### Q2: 如何实现多数据源？
**A**: 使用 MyBatis-Plus 动态数据源：
```java
@Configuration
public class DataSourceConfig {
    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("master", masterDataSource());
        dataSourceMap.put("slave", slaveDataSource());
        
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource());
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        return dynamicDataSource;
    }
}
```

### Q3: 如何实现接口幂等性？
**A**: 使用 Redis + Token 机制：
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String key() default "";
    int expire() default 60; // 过期时间（秒）
}

@Aspect
@Component
public class IdempotentAspect {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Around("@annotation(idempotent)")
    public Object checkIdempotent(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String token = HttpContextUtil.getRequest().getHeader("Idempotent-Token");
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(400, "幂等 Token 不能为空");
        }
        
        String key = "idempotent:" + idempotent.key() + ":" + token;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(idempotent.expire()));
        if (Boolean.FALSE.equals(success)) {
            throw new BusinessException(400, "重复请求");
        }
        
        return joinPoint.proceed();
    }
}
```

---

## 附录

### A. 参考文档
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [DDD 领域驱动设计](https://domainlanguage.com/)

### B. 常用依赖
```xml
<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>

<!-- Hutool -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
</dependency>

<!-- BeanUtils -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-core</artifactId>
</dependency>
```

---

**文档版本**: v1.1  
**最后更新**: 2026-05-21  
**维护者**: 筑基架构团队
