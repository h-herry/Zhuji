# 文件服务 (file-service)

## 1. 模块概述

文件服务提供统一的文件上传、下载、管理功能，支持本地存储和云存储（OSS），提供文件元数据管理。

### 1.1 主要功能

- **文件上传**：支持多种文件类型
- **文件下载**：支持断点续传
- **文件管理**：文件列表、删除
- **存储抽象**：支持本地和云存储

### 1.2 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 基础框架 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| Redis | 7.x | 缓存 |

---

## 2. 数据库设计

### 2.1 文件表 (file_info)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| file_name | VARCHAR(255) | 文件名 |
| file_path | VARCHAR(500) | 文件路径 |
| file_size | BIGINT | 文件大小 |
| file_type | VARCHAR(50) | 文件类型 |
| mime_type | VARCHAR(100) | MIME类型 |
| storage_type | VARCHAR(20) | 存储类型 |
| bucket_name | VARCHAR(100) | 存储桶名称 |
| file_key | VARCHAR(255) | 文件唯一标识 |
| user_id | BIGINT | 上传用户ID |
| status | TINYINT | 状态 |
| create_time | DATETIME | 创建时间 |

---

## 3. API接口

### 3.1 文件管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/files/upload | 上传文件 |
| GET | /api/v1/files/{id} | 获取文件信息 |
| GET | /api/v1/files/{id}/download | 下载文件 |
| GET | /api/v1/files | 分页查询文件 |
| DELETE | /api/v1/files/{id} | 删除文件 |

### 3.2 请求响应示例

**上传文件响应**
```json
{
    "code": 200,
    "data": {
        "id": 1,
        "fileName": "document.pdf",
        "fileKey": "2024/05/21/abc123.pdf",
        "fileSize": 1024000,
        "mimeType": "application/pdf",
        "url": "/api/v1/files/1/download"
    }
}
```

---

## 4. 文件类型限制

| 类型 | 允许扩展名 | 最大大小 |
|------|-----------|----------|
| 图片 | jpg, jpeg, png, gif, bmp | 10MB |
| 文档 | doc, docx, pdf, txt, xls, xlsx | 50MB |
| 视频 | mp4, avi, mov, wmv | 500MB |
| 压缩包 | zip, rar, 7z | 100MB |
| 其他 | * | 50MB |

---

## 5. 使用示例

### 5.1 上传文件

```java
@PostMapping("/upload")
public ApiResponse<FileVO> upload(@RequestParam("file") MultipartFile file) {
    FileVO result = fileService.uploadFile(file);
    return ApiResponse.success(result);
}
```

### 5.2 下载文件

```java
@GetMapping("/{id}/download")
public void download(@PathVariable Long id, HttpServletResponse response) {
    fileService.downloadFile(id, response);
}
```

---

## 6. 存储配置

### 6.1 本地存储

```yaml
file:
  storage:
    type: local
    local:
      path: /data/files
      base-url: http://localhost:8080/files
```

### 6.2 OSS存储

```yaml
file:
  storage:
    type: oss
    oss:
      endpoint: oss-cn-beijing.aliyuncs.com
      access-key: xxx
      secret-key: xxx
      bucket: zhuji-files
```

---

## 7. 注意事项

1. **文件大小**：需配置nginx上传大小限制
2. **文件安全**：敏感文件需加密存储
3. **存储清理**：定期清理无用文件
