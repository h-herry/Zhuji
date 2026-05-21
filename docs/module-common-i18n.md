# 公共国际化模块 (common-i18n)

## 1. 模块概述

common-i18n模块提供多语言支持功能，支持中文、英文、繁体中文、日语、韩语等多种语言，提供统一的消息获取工具和国际化配置。支持多语言可配置化，可在运行时切换语言并通过Cookie持久化。

### 1.1 主要功能

- **多语言支持**：简体中文、英文、繁体中文、日语、韩语
- **消息获取**：支持参数化的国际化消息
- **动态切换**：运行时语言切换
- **消息缓存**：提高消息获取性能
- **多语言可配置化**：支持的语言列表可在数据库配置
- **Cookie持久化**：语言设置通过Cookie持久化

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Context | 6.x | 国际化支持 |
| Spring MessageSource | - | 消息源支持 |
| JDK 21 | 21 | 运行时 |

---

## 2. 核心组件

### 2.1 I18nConfig - 国际化配置

```java
@Configuration
public class I18nConfig {

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource source =
            new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:i18n/messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(false);
        source.setCacheSeconds(3600);
        return source;
    }
}
```

### 2.2 I18nMessageUtil - 消息工具类

```java
public class I18nMessageUtil {

    public static String getMessage(String code) {
        return messageSource().getMessage(code, null, Locale.getDefault());
    }

    public static String getMessage(String code, String... args) {
        return messageSource().getMessage(code, args, Locale.getDefault());
    }

    public static String getMessage(String code, Locale locale, String... args) {
        return messageSource().getMessage(code, args, locale);
    }
}
```

---

## 3. 消息资源文件

### 3.1 文件结构

```
src/main/resources/
└── i18n/
    ├── messages.properties          # 默认（中文）
    ├── messages_zh_CN.properties    # 中文（简体）
    ├── messages_en_US.properties    # 英文
    ├── messages_zh_TW.properties    # 中文（繁体）
    ├── messages_ja_JP.properties    # 日语
    └── messages_ko_KR.properties    # 韩语
```

### 3.2 消息格式

```properties
# 默认语言（中文简体）
user.not.found=用户不存在
user.username.exists=用户名已存在
user.password.error=密码错误
permission.denied=权限不足
user.assign.roles.success=角色分配成功
user.assign.org.success=组织分配成功

# 英文
user.not.found=User not found
user.username.exists=Username already exists
user.password.error=Incorrect password
permission.denied=Permission denied
user.assign.roles.success=Role assigned successfully
user.assign.org.success=Organization assigned successfully
```

---

## 4. 支持的语言

| 语言代码 | 语言名称 | 状态 |
|----------|----------|------|
| zh_CN | 简体中文 | 默认 |
| en_US | English | 支持 |
| zh_TW | 繁體中文 | 支持 |
| ja_JP | 日本語 | 支持 |
| ko_KR | 한국어 | 支持 |

---

## 5. API接口

### 5.1 I18nController

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/v1/i18n/languages | 获取支持的语言列表 | 否 |
| POST | /api/v1/i18n/switch | 切换语言 | 否 |
| GET | /api/v1/i18n/current | 获取当前语言 | 否 |
| GET | /api/v1/i18n/validate/{lang} | 验证语言是否支持 | 否 |

### 5.2 使用示例

```bash
# 获取支持的语言列表
GET /api/v1/i18n/languages

# 响应
{
    "code": 200,
    "data": [
        {"code": "zh_CN", "name": "简体中文"},
        {"code": "en_US", "name": "English"},
        {"code": "zh_TW", "name": "繁體中文"},
        {"code": "ja_JP", "name": "日本語"},
        {"code": "ko_KR", "name": "한국어"}
    ]
}

# 切换语言
POST /api/v1/i18n/switch?lang=en_US

# 获取当前语言
GET /api/v1/i18n/current

# 验证语言是否支持
GET /api/v1/i18n/validate/ja_JP
```

---

## 6. 多语言可配置化

### 6.1 数据库配置

支持的语言列表存储在 `sys_global_config` 表中：

| 配置类型 | 配置键 | 配置值 | 说明 |
|----------|--------|--------|------|
| i18n | supported.languages | zh_CN,en_US,zh_TW,ja_JP,ko_KR | 支持的语言列表 |
| i18n | default.language | zh_CN | 系统默认语言 |

### 6.2 前端集成

前端可以通过以下方式切换语言：

```javascript
// 切换到英文
fetch('/api/v1/i18n/switch?lang=en_US', { method: 'POST' })
  .then(() => location.reload());

// 获取当前语言
fetch('/api/v1/i18n/current')
  .then(res => res.json())
  .then(data => console.log(data.code, data.name));
```

### 6.3 Cookie机制

语言设置通过Cookie持久化：

- Cookie名称：`locale`
- 过期时间：7天
- 作用域：/

---

## 7. 使用示例

### 7.1 基础使用

```java
@Service
public class UserService {

    public UserVO getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(404,
                I18nMessageUtil.getMessage("user.not.found"));
        }
        return convertToVO(user);
    }
}
```

### 7.2 带参数的消息

```java
// 消息文件
user.create.success=用户 {0} 创建成功

// 代码
String message = I18nMessageUtil.getMessage("user.create.success", "admin");
// 结果: 用户 admin 创建成功
```

### 7.3 指定语言

```java
// 获取中文消息
String message = I18nMessageUtil.getMessage("user.not.found", Locale.CHINA);

// 获取英文消息
String message = I18nMessageUtil.getMessage("user.not.found", Locale.US);
```

### 7.4 业务异常中使用

```java
// 角色不存在
throw new BusinessException(404, I18nMessageUtil.getMessage("role.not.found"));

// 角色编码已存在
throw new BusinessException(400, I18nMessageUtil.getMessage("role.code.exists"));

// 权限不足
throw new BusinessException(403, I18nMessageUtil.getMessage("permission.denied"));
```

---

## 8. 消息分类

### 8.1 用户相关消息

| 消息键 | 中文 | 英文 |
|---------|------|------|
| user.not.found | 用户不存在 | User not found |
| user.username.exists | 用户名已存在 | Username already exists |
| user.password.error | 密码错误 | Incorrect password |
| user.assign.roles.success | 角色分配成功 | Role assigned successfully |
| user.assign.org.success | 组织分配成功 | Organization assigned successfully |
| user.password.updated | 密码更新成功 | Password updated successfully |
| user.password.mismatch | 两次输入的密码不匹配 | Passwords do not match |

### 8.2 角色相关消息

| 消息键 | 中文 | 英文 |
|---------|------|------|
| role.not.found | 角色不存在 | Role not found |
| role.create.success | 角色创建成功 | Role created successfully |
| role.update.success | 角色更新成功 | Role updated successfully |
| role.delete.success | 角色删除成功 | Role deleted successfully |
| role.code.exists | 角色编码已存在 | Role code already exists |
| role.in.use | 角色使用中无法删除 | Role is in use and cannot be deleted |

### 8.3 权限相关消息

| 消息键 | 中文 | 英文 |
|---------|------|------|
| permission.denied | 无权限访问 | Permission denied |
| permission.not.found | 权限不存在 | Permission not found |
| permission.create.success | 权限创建成功 | Permission created successfully |
| permission.update.success | 权限更新成功 | Permission updated successfully |
| permission.delete.success | 权限删除成功 | Permission deleted successfully |

### 8.4 组织相关消息

| 消息键 | 中文 | 英文 |
|---------|------|------|
| org.not.found | 组织不存在 | Organization not found |
| org.create.success | 组织创建成功 | Organization created successfully |
| org.update.success | 组织更新成功 | Organization updated successfully |
| org.delete.success | 组织删除成功 | Organization deleted successfully |
| org.code.exists | 组织编码已存在 | Organization code already exists |
| org.has.children | 组织存在下级无法删除 | Organization has children and cannot be deleted |

---

## 9. 扩展消息

### 9.1 添加新语言

1. 在`i18n`目录下创建新的属性文件
2. 文件命名规则：`messages_{locale}.properties`
3. 例如：`messages_ja_JP.properties`（日语）

### 9.2 添加新消息

```properties
# 在消息文件中添加
module.new.message=新消息内容
```

### 9.3 配置支持的语言

1. 在 `sys_global_config` 表中添加新的配置
2. 更新 `supported.languages` 配置值，添加新的语言代码

---

## 10. 常见问题

### 10.1 消息找不到

检查消息文件是否存在，以及消息key是否正确。

### 10.2 中文乱码

确保文件编码为UTF-8。

### 10.3 动态切换语言

通过Cookie或请求参数指定语言：

```java
// 从Cookie获取语言
Cookie[] cookies = request.getCookies();
if (cookies != null) {
    for (Cookie cookie : cookies) {
        if ("locale".equals(cookie.getName())) {
            Locale locale = Locale.forLanguageTag(cookie.getValue());
            // 使用该语言
        }
    }
}
```

### 10.4 消息文件热加载

配置 `ReloadableResourceBundleMessageSource` 可以实现消息文件的热加载，无需重启应用。
