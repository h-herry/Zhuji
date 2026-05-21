# 公共安全模块 (common-security)

## 1. 模块概述

common-security模块提供统一的安全认证和授权功能，包括JWT Token工具、认证过滤器、安全配置等基础安全组件。

### 1.1 主要功能

- **JWT工具类**：Token生成、验证、解析
- **认证过滤器**：JWT认证过滤器
- **安全配置**：Spring Security配置基类
- **用户信息工具**：当前登录用户信息获取

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Security | 6.x | 安全框架 |
| JJWT | 0.12.x | JWT处理 |
| Java 21 | 21 | 运行时 |

---

## 2. 核心组件

### 2.1 JwtUtil - JWT工具类

```java
public class JwtUtil {
    private static final String SECRET_KEY = "ZhujiSecretKeyForJWTTokenGeneration2024VeryLongSecretKey";
    private static final long EXPIRATION_TIME = 86400000; // 24小时

    public static String generateToken(Long userId, String username) { ... }
    public static Claims parseToken(String token) { ... }
    public static String getUsernameFromToken(String token) { ... }
    public static Long getUserIdFromToken(String token) { ... }
    public static boolean isTokenExpired(String token) { ... }
    public static boolean validateToken(String token) { ... }
}
```

#### Token结构

Header:
```json
{
    "alg": "HS256",
    "typ": "JWT"
}
```

Payload:
```json
{
    "userId": 1,
    "username": "admin",
    "iat": 1716192000,
    "exp": 1716278400
}
```

### 2.2 JwtAuthenticationFilter - JWT认证过滤器

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 从请求头获取Token
        String token = extractToken(request);

        // 2. 验证Token
        if (token != null && jwtUtil.validateToken(token)) {
            // 3. 解析用户信息
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 4. 设置安全上下文
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 5. 添加请求属性
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
        }

        filterChain.doFilter(request, response);
    }
}
```

### 2.3 SecurityConfig - 安全配置基类

```java
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## 3. 安全机制

### 3.1 认证流程

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client  │────>│ Gateway  │────>│  Filter  │────>│ Controller│
└──────────┘     └──────────┘     └──────────┘     └──────────┘
    │                                     │
    │  1. Login Request                   │
    │  {username, password}                │
    │                                     │
    │                       2. Validate   │
    │                       Credentials    │
    │                                     │
    │                       3. Generate   │
    │                       JWT Token     │
    │                                     │
    │  4. Return Token                    │
    │  {token, expiresIn}                 │
```

### 3.2 请求认证流程

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client  │────>│ Gateway  │────>│ JwtFilter │────>│ Controller│
└──────────┘     └──────────┘     └──────────┘     └──────────┘
    │                                     │
    │  1. Request + Token                 │
    │  Authorization: Bearer xxx           │
    │                                     │
    │                       2. Extract    │
    │                       Token         │
    │                                     │
    │                       3. Validate   │
    │                       & Parse       │
    │                                     │
    │                       4. Set        │
    │                       Security      │
    │                       Context       │
    │                                     │
    │  5. Process Request                 │
```

---

## 4. 使用示例

### 4.1 生成Token

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        // 验证用户密码后生成Token
        String token = JwtUtil.generateToken(userId, username);

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .expiresIn(86400)
                .build();

        return ApiResponse.success(response);
    }
}
```

### 4.2 验证Token

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUser(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        // 从Token中获取当前用户
        String token = authHeader.replace("Bearer ", "");
        String username = JwtUtil.getUsernameFromToken(token);

        // 业务逻辑
        return ApiResponse.success(userService.getUserById(id));
    }
}
```

### 4.3 获取当前用户

```java
public class CurrentUser {

    public static Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    public static String getUsername(HttpServletRequest request) {
        return (String) request.getAttribute("username");
    }
}
```

---

## 5. Token配置

### 5.1 配置参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| jwt.secret | ZhujiSecretKeyForJWTTokenGeneration2024VeryLongSecretKey | 签名密钥 |
| jwt.expiration | 86400000 | 过期时间（毫秒） |

### 5.2 安全建议

1. **密钥安全**：生产环境必须更换默认密钥
2. **过期时间**：建议设置为2小时以内
3. **Token刷新**：实现Refresh Token机制
4. **HTTPS**：生产环境必须使用HTTPS

---

## 6. 依赖关系

```
common-security
├── common-core
├── spring-security
├── jjwt (0.12.x)
│
└── 被以下模块依赖:
    ├── user-org-service
    ├── api-gateway
    └── 其他需要认证的模块
```

---

## 7. 注意事项

1. **过滤器顺序**：JwtAuthenticationFilter必须在所有认证过滤器之前
2. **排除路径**：登录、注册等公开接口需要在安全配置中排除
3. **Token传递**：前端必须通过Authorization头传递Token，格式为`Bearer {token}`
4. **异常处理**：Token过期或无效时返回401状态码
