# 筑基 (Zhuji) 模块开发指南
# Zhuji Module Development Guide

## 目录 | Table of Contents
1. [快速开始 | Quick Start](#1-快速开始--quick-start)
2. [模块开发规范 | Module Development Standards](#2-模块开发规范--module-development-standards)
3. [通用模块开发 | Common Module Development](#3-通用模块开发--common-module-development)
4. [业务模块开发 | Business Module Development](#4-业务模块开发--business-module-development)
5. [API 开发规范 | API Development Standards](#5-api-开发规范--api-development-standards)
6. [测试规范 | Testing Standards](#6-测试规范--testing-standards)
7. [最佳实践 | Best Practices](#7-最佳实践--best-practices)
8. [常见问题 | FAQs](#8-常见问题--faqs)

---

## 1. 快速开始 | Quick Start

### 1.1 环境准备 | Environment Preparation
**必需环境 | Required Environment**:
- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.x+
- Nacos 2.3+

**推荐 IDE | Recommended IDE**:
- IntelliJ IDEA (推荐 | recommended)
- VS Code + Java 插件 | VS Code + Java plugins

### 1.2 创建新模块 | Create New Module

**步骤 1: 使用 Maven 原型创建模块 | Step 1: Create Module with Maven Archetype**
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

**步骤 2: 手动创建模块结构 | Step 2: Manually Create Module Structure**
```
your-module/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/zhuji/modules/yourmodule/
    │   │   ├── controller/      # API 控制器 | API controllers
    │   │   ├── service/         # 业务服务接口 | business service interfaces
    │   │   │   └── impl/       # 业务服务实现 | business service implementations
    │   │   ├── repository/      # 数据访问层 | data access layer
    │   │   ├── domain/          # 实体类 | entity classes
    │   │   ├── dto/             # 数据传输对象 | data transfer objects
    │   │   ├── vo/              # 视图对象 | view objects
    │   │   ├── config/          # 配置类 | configuration classes
    │   │   └── YourModuleApplication.java  # 启动类 | bootstrap class
    │   └── resources/
    │       ├── application.yml
    │       ├── mapper/          # MyBatis Mapper XML
    │       └── bootstrap.yml
    └── test/
        └── java/com/zhuji/modules/yourmodule/
            └── YourModuleTest.java
```

**步骤 3: 配置 pom.xml | Step 3: Configure pom.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.zhuji</groupId>
        <artifactId>zhuji-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>your-module</artifactId>
    <packaging>jar</packaging>
    <name>your-module</name>
    <description>你的模块描述 | Your module description</description>
    
    <dependencies>
        <!-- 依赖公共模块 | Dependencies on common modules -->
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

## 2. 模块开发规范 | Module Development Standards

### 2.1 包结构规范 | Package Structure Standards
```
com.zhuji.modules.{module-name}/
├── controller/       # API 层：接收请求，参数校验，返回响应 | API layer: receive requests, validate parameters, return responses
├── service/          # 业务层接口 | business layer interfaces
│   └── impl/       # 业务层实现 | business layer implementations
├── repository/       # 数据访问层接口 | data access layer interfaces
│   └── impl/       # 数据访问层实现（可选）| data access layer implementations (optional)
├── domain/           # 实体模型（JPA 注解）| entity models (JPA annotations)
├── dto/              # 数据传输对象（接收请求）| data transfer objects (for requests)
├── vo/               # 视图对象（返回响应）| view objects (for responses)
├── config/           # Spring 配置类 | Spring configuration classes
├── aspect/           # AOP 切面 | AOP aspects
├── listener/         # 事件监听器 | event listeners
├── handler/          # 处理器（如异常处理器）| handlers (e.g., exception handlers)
├── util/             # 工具类 | utility classes
└── constants/        # 常量定义 | constant definitions
```

### 2.2 命名规范 | Naming Standards
| 类型 | Type | 命名规则 | Naming Rule | 示例 | Example |
|------|------|----------|-------------|------|---------|
| 控制器 | Controller | XxxController | UserController |
| 服务接口 | Service Interface | XxxService | UserService |
| 服务实现 | Service Implementation | XxxServiceImpl | UserServiceImpl |
| 数据访问接口 | Repository Interface | XxxRepository | UserRepository |
| 实体类 | Entity | Xxx | User |
| DTO | DTO | XxxDTO / XxxRequest / XxxParam | UserDTO / CreateUserRequest |
| VO | VO | XxxVO / XxxResponse | UserVO / UserResponse |
| Mapper (MyBatis) | Mapper (MyBatis) | XxxMapper | UserMapper |

### 2.3 代码规范 | Code Standards
**使用 Lombok 简化代码 | Simplify Code with Lombok**:
```java
@Data               // getter + setter
@Builder            // 建造者模式 | builder pattern
@NoArgsConstructor  // 无参构造 | no-args constructor
@AllArgsConstructor // 全参构造 | all-args constructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
}
```

**使用 Hutool 工具库 | Use Hutool Tool Library**:
```java
// 字符串工具 | String utilities
StrUtil.isNotBlank(str);

// 日期工具 | Date utilities
DateUtil.date();

// JSON 工具 | JSON utilities
JSONUtil.toJSONString(obj);
```

---

## 3. 通用模块开发 | Common Module Development

### 3.1 common-core 模块 | common-core Module
**职责 | Responsibilities**: 提供全局通用的常量、枚举、异常、工具类 | Provide global common constants, enums, exceptions, utility classes

**目录结构 | Directory Structure**:
```
common-core/
├── constants/       # 全局常量 | global constants
├── enums/           # 全局枚举 | global enums
├── exception/       # 全局异常 | global exceptions
├── util/            # 工具类 | utility classes
└── base/            # 基础抽象类 | base abstract classes
```

**示例代码 | Example Code**:

*全局异常基类 | Global Exception Base Class*:
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

*全局错误码枚举 | Global Error Code Enum*:
```java
package com.zhuji.common.core.enums;

public enum ErrorCode {
    SUCCESS(200, "成功 | Success"),
    BAD_REQUEST(400, "请求参数错误 | Bad Request"),
    UNAUTHORIZED(401, "未授权 | Unauthorized"),
    FORBIDDEN(403, "禁止访问 | Forbidden"),
    NOT_FOUND(404, "资源不存在 | Not Found"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误 | Internal Server Error");
    
    private Integer code;
    private String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    // getters
}
```

### 3.2 common-security 模块 | common-security Module
**职责 | Responsibilities**: 提供统一的安全认证和授权功能 | Provide unified security authentication and authorization features

**核心类 | Core Classes**:

*JWT 工具类 | JWT Utility Class*:
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

*Spring Security 配置 | Spring Security Configuration*:
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

### 3.3 common-cache 模块 | common-cache Module
**职责 | Responsibilities**: 封装 Redis 操作，提供缓存注解，支持分布式锁 | Encapsulate Redis operations, provide cache annotations, support distributed locks

**核心类 | Core Classes**:

*Redis 配置 | Redis Configuration*:
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

*缓存注解 | Cache Annotations*:
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

### 3.4 common-i18n 模块 | common-i18n Module
**职责 | Responsibilities**: 提供多语言支持，支持数据库消息存储和缓存 | Provide multi-language support, support database message storage and caching

**核心类 | Core Classes**:

*国际化配置 | I18n Configuration*:
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

*消息工具类 | Message Utility Class*:
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

### 3.5 common-mq 模块 | common-mq Module
**职责 | Responsibilities**: 封装消息队列操作，支持 RabbitMQ | Encapsulate message queue operations, support RabbitMQ

**核心类 | Core Classes**:

*消息发送者 | Message Sender*:
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

*消息监听器 | Message Listener*:
```java
@Component
public class RabbitMQListener {
    @RabbitListener(queues = "example.queue")
    public void handleMessage(String message) {
        // 处理消息 | Handle message
    }
}
```

### 3.6 common-task 模块 | common-task Module
**职责 | Responsibilities**: 提供定时任务支持，支持任务日志 | Provide scheduled task support, support task logs

**核心类 | Core Classes**:

*定时任务配置 | Scheduled Task Configuration*:
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

*定时任务示例 | Scheduled Task Example*:
```java
@Component
public class ExampleTask {
    @Scheduled(cron = "0 0 * * * ?")
    @ScheduledLog(description = "示例定时任务 | Example Scheduled Task")
    public void execute() {
        // 任务逻辑 | Task logic
    }
}
```

### 3.7 common-monitor 模块 | common-monitor Module
**职责 | Responsibilities**: 提供监控支持，集成 Prometheus 和 Zipkin | Provide monitoring support, integrate Prometheus and Zipkin

**核心类 | Core Classes**:

*监控配置 | Monitoring Configuration*:
```java
@Configuration
public class MonitorConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "zhuji");
    }
}
```

### 3.8 common-crypto 模块 | common-crypto Module
**职责 | Responsibilities**: 提供加密支持，集成 Jasypt | Provide encryption support, integrate Jasypt

**核心类 | Core Classes**:

*加密工具类 | Encryption Utility Class*:
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

### 3.9 common-export 模块 | common-export Module
**职责 | Responsibilities**: 提供导出支持，集成 EasyExcel | Provide export support, integrate EasyExcel

**核心类 | Core Classes**:

*Excel导出工具 | Excel Export Utility*:
```java
@Component
public class ExcelExportUtil {
    public void export(HttpServletResponse response, List<?> data, Class<?> clazz, String fileName) {
        // 使用 EasyExcel 导出 | Export using EasyExcel
    }
}
```

### 3.10 common-audit 模块 | common-audit Module
**职责 | Responsibilities**: 提供审计日志支持 | Provide audit log support

**核心类 | Core Classes**:

*审计日志注解 | Audit Log Annotation*:
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    String description();
    String module() default "";
}
```

*审计日志切面 | Audit Log Aspect*:
```java
@Aspect
@Component
public class AuditLogAspect {
    @Around("@annotation(auditLog)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        // 记录审计日志 | Record audit log
        return joinPoint.proceed();
    }
}
```

---

## 4. 业务模块开发 | Business Module Development

### 4.1 用户组织模块 (user-org-service) | User Organization Module (user-org-service)
**实体设计 | Entity Design**:

*User 实体 | User Entity*:
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
    private Integer status; // 0-禁用 | 0-disabled, 1-启用 | 1-enabled
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

*OrgUnit 实体（树形结构）| OrgUnit Entity (Tree Structure)*:
```java
@Data
@TableName("sys_org_unit")
public class OrgUnit {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private Long parentId; // 父级组织 ID | Parent organization ID
    private Integer level;  // 层级 | Level
    private String path;     // 层级路径，如 /1/3/5 | Hierarchy path, e.g., /1/3/5
    
    @TableField(exist = false)
    private List<OrgUnit> children; // 子节点（非数据库字段）| Child nodes (non-database field)
}
```

**Repository 层 | Repository Layer**:
```java
@Mapper
public interface UserRepository extends BaseMapper<User> {
    // 自定义 SQL（可选）| Custom SQL (optional)
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
}
```

**Service 层 | Service Layer**:
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

**Controller 层 | Controller Layer**:
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

## 5. API 开发规范 | API Development Standards

### 5.1 统一响应格式 | Unified Response Format
**ApiResponse 类 | ApiResponse Class**:
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
                .message("成功 | Success")
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

### 5.2 参数校验 | Parameter Validation
**使用 Bean Validation | Use Bean Validation**:
```java
@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空 | Username cannot be blank")
    @Size(min = 3, max = 20, message = "用户名长度必须在 3-20 之间 | Username must be between 3-20 characters")
    private String username;
    
    @NotBlank(message = "密码不能为空 | Password cannot be blank")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 之间 | Password must be between 6-20 characters")
    private String password;
    
    @Email(message = "邮箱格式不正确 | Invalid email format")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确 | Invalid phone format")
    private String phone;
}
```

**全局异常处理 | Global Exception Handling**:
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

### 5.3 API 版本控制 | API Version Control
**URL 路径版本控制 | URL Path Version Control**:
```
/api/v1/users     # 版本 1 | Version 1
/api/v2/users     # 版本 2 | Version 2
```

**请求头版本控制 | Header Version Control**:
```java
@RequestMapping(value = "/users", headers = "X-API-Version=1")
public class UserControllerV1 { }

@RequestMapping(value = "/users", headers = "X-API-Version=2")
public class UserControllerV2 { }
```

---

## 6. 测试规范 | Testing Standards

### 6.1 单元测试 | Unit Testing
**Service 层测试 | Service Layer Testing**:
```java
@SpringBootTest
class UserServiceTest {
    @MockBean
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Test
    void testGetUserById() {
        // 准备数据 | Prepare data
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test");
        when(userRepository.selectById(1L)).thenReturn(mockUser);
        
        // 执行测试 | Execute test
        User result = userService.getUserById(1L);
        
        // 验证结果 | Verify result
        assertEquals("test", result.getUsername());
        verify(userRepository).selectById(1L);
    }
}
```

### 6.2 集成测试 | Integration Testing
**Controller 层测试 | Controller Layer Testing**:
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

### 6.3 压力测试 | Load Testing
**使用 JMeter | Use JMeter**:
1. 创建测试计划 | Create test plan
2. 添加线程组（模拟用户）| Add thread group (simulate users)
3. 添加 HTTP 请求（调用 API）| Add HTTP requests (call APIs)
4. 添加聚合报告（查看结果）| Add aggregate report (view results)

---

## 7. 最佳实践 | Best Practices

### 7.1 DDD 领域驱动设计 | DDD Domain-Driven Design
**分层架构 | Layered Architecture**:
```
interfaces/      # 用户接口层：API 控制器 | User interface layer: API controllers
application/     # 应用层：用例编排（无业务逻辑）| Application layer: use case orchestration (no business logic)
domain/          # 领域层：核心业务逻辑（实体、值对象、领域服务）| Domain layer: core business logic (entities, value objects, domain services)
infrastructure/  # 基础设施层：数据访问、外部服务调用 | Infrastructure layer: data access, external service calls
```

**示例 | Example**:
```java
// interfaces 层 | interfaces layer
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

// application 层 | application layer
@Service
@RequiredArgsConstructor
public class UserApplicationService {
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;
    
    @Transactional
    public void createUser(CreateUserCommand command) {
        // 1. 校验业务规则 | Validate business rules
        userDomainService.checkUsernameUnique(command.getUsername());
        
        // 2. 创建领域对象 | Create domain object
        User user = User.create(command.getUsername(), command.getPassword());
        
        // 3. 持久化 | Persist
        userRepository.save(user);
        
        // 4. 发布领域事件 | Publish domain event
        userDomainService.publishEvent(new UserCreatedEvent(user.getId()));
    }
}

// domain 层 | domain layer
@Service
public class UserDomainService {
    public void checkUsernameUnique(String username) {
        // 业务逻辑：检查用户名唯一性 | Business logic: Check username uniqueness
    }
    
    public void publishEvent(Object event) {
        // 发布领域事件 | Publish domain event
    }
}

// infrastructure 层 | infrastructure layer
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserMapper userMapper;
    
    @Override
    public void save(User user) {
        userMapper.insert(user);
    }
}
```

### 7.2 事务管理 | Transaction Management
**声明式事务 | Declarative Transactions**:
```java
@Service
public class UserService {
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserDTO dto) {
        // 业务逻辑 | Business logic
    }
}
```

**分布式事务 (Seata) | Distributed Transaction (Seata)**:
```java
@Service
public class OrderService {
    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public void createOrder(CreateOrderRequest request) {
        // 1. 创建订单（订单服务）| 1. Create order (order service)
        orderClient.createOrder(request);
        
        // 2. 扣减库存（库存服务）| 2. Deduct inventory (inventory service)
        inventoryClient.deductInventory(request);
        
        // 3. 扣减账户余额（账户服务）| 3. Deduct account balance (account service)
        accountClient.deductBalance(request);
    }
}
```

### 7.3 异步处理 | Async Processing
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
        // 发送邮件逻辑 | Send email logic
        return CompletableFuture.completedFuture(null);
    }
}
```

---

## 8. 常见问题 | FAQs

### Q1: 如何集成 MyBatis-Plus？| How to integrate MyBatis-Plus?
**A**: 添加依赖并配置 | Add dependency and configure:
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

### Q2: 如何实现多数据源？| How to implement multi-data source?
**A**: 使用 MyBatis-Plus 动态数据源 | Use MyBatis-Plus dynamic data source:
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

### Q3: 如何实现接口幂等性？| How to implement API idempotency?
**A**: 使用 Redis + Token 机制 | Use Redis + Token mechanism:
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String key() default "";
    int expire() default 60; // 过期时间（秒）| Expire time (seconds)
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
            throw new BusinessException(400, "幂等 Token 不能为空 | Idempotent token cannot be blank");
        }
        
        String key = "idempotent:" + idempotent.key() + ":" + token;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(idempotent.expire()));
        if (Boolean.FALSE.equals(success)) {
            throw new BusinessException(400, "重复请求 | Duplicate request");
        }
        
        return joinPoint.proceed();
    }
}
```

---

## 附录 | Appendix

### A. 参考文档 | References
- [Spring Boot 官方文档 | Spring Boot Official Documentation](https://spring.io/projects/spring-boot)
- [MyBatis-Plus 官方文档 | MyBatis-Plus Official Documentation](https://baomidou.com/)
- [DDD 领域驱动设计 | DDD Domain-Driven Design](https://domainlanguage.com/)

### B. 常用依赖 | Common Dependencies
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

**文档版本 | Document Version**: v1.1  
**最后更新 | Last Updated**: 2026-05-21  
**维护者 | Maintainers**: 筑基架构团队 | Zhuji Architecture Team
