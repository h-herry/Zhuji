package com.zhuji.file.controller;

import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

@Tag(name = "文件管理", description = "文件上传、下载、删除接口")
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = fileService.upload(file);
        boolean success = (Boolean) result.get("success");
        if (success) {
            return ApiResponse.success(result);
        } else {
            return ApiResponse.error(500, (String) result.get("message"));
        }
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{fileId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String fileId) {
        try {
            InputStream inputStream = fileService.download(fileId);
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                    .body(resource);
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> delete(@PathVariable String fileId) {
        boolean success = fileService.delete(fileId);
        if (success) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error(404, "文件不存在");
        }
    }

    @Operation(summary = "获取文件信息")
    @GetMapping("/{fileId}")
    public ApiResponse<Map<String, Object>> getFileInfo(@PathVariable String fileId) {
        Map<String, Object> result = fileService.getFileInfo(fileId);
        boolean success = (Boolean) result.get("success");
        if (success) {
            return ApiResponse.success(result);
        } else {
            return ApiResponse.error(404, (String) result.get("message"));
        }
    }
}