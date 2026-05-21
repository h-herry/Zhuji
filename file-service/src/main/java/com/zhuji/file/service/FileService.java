package com.zhuji.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

public interface FileService {
    Map<String, Object> upload(MultipartFile file);
    InputStream download(String fileId);
    boolean delete(String fileId);
    Map<String, Object> getFileInfo(String fileId);
}