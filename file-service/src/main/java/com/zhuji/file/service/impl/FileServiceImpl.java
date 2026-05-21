package com.zhuji.file.service.impl;

import com.zhuji.file.entity.FileEntity;
import com.zhuji.file.mapper.FileMapper;
import com.zhuji.file.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileMapper fileMapper;

    public FileServiceImpl(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    @Value("${file.storage.local.path:./uploads}")
    private String localStoragePath;

    @Override
    public Map<String, Object> upload(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            String originalName = file.getOriginalFilename();
            String extension = getFileExtension(originalName);
            String fileName = UUID.randomUUID().toString() + extension;

            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadPath = Paths.get(localStoragePath, datePath);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            String fileUrl = "/api/v1/files/download/" + fileName;

            FileEntity fileEntity = FileEntity.builder()
                    .fileName(fileName)
                    .originalName(originalName)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileUrl(fileUrl)
                    .storageType("local")
                    .path(datePath + "/" + fileName)
                    .status(1)
                    .build();

            fileMapper.insert(fileEntity);

            result.put("success", true);
            result.put("fileId", fileEntity.getId());
            result.put("fileName", fileName);
            result.put("originalName", originalName);
            result.put("fileUrl", fileUrl);
            result.put("fileSize", file.getSize());

            log.info("文件上传成功: {}", fileName);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "文件上传失败");
        }

        return result;
    }

    @Override
    public InputStream download(String fileId) {
        try {
            FileEntity fileEntity = fileMapper.selectById(Long.parseLong(fileId));
            if (fileEntity == null) {
                throw new RuntimeException("文件不存在");
            }

            Path filePath = Paths.get(localStoragePath, fileEntity.getPath());
            return new FileInputStream(filePath.toFile());
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败");
        }
    }

    @Override
    public boolean delete(String fileId) {
        try {
            FileEntity fileEntity = fileMapper.selectById(Long.parseLong(fileId));
            if (fileEntity == null) {
                return false;
            }

            Path filePath = Paths.get(localStoragePath, fileEntity.getPath());
            Files.deleteIfExists(filePath);

            fileMapper.deleteById(Long.parseLong(fileId));
            log.info("文件删除成功: {}", fileEntity.getFileName());
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getFileInfo(String fileId) {
        Map<String, Object> result = new HashMap<>();

        FileEntity fileEntity = fileMapper.selectById(Long.parseLong(fileId));
        if (fileEntity == null) {
            result.put("success", false);
            result.put("message", "文件不存在");
            return result;
        }

        result.put("success", true);
        result.put("fileId", fileEntity.getId());
        result.put("fileName", fileEntity.getFileName());
        result.put("originalName", fileEntity.getOriginalName());
        result.put("fileType", fileEntity.getFileType());
        result.put("fileSize", fileEntity.getFileSize());
        result.put("fileUrl", fileEntity.getFileUrl());
        result.put("createTime", fileEntity.getCreateTime());

        return result;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}