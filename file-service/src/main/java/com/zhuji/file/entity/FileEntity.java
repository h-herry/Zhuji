package com.zhuji.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("sys_file")
public class FileEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String originalName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String storageType;
    private String bucketName;
    private String path;
    private Long userId;
    private Integer status;
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Integer deleted;

    public FileEntity() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String fileName;
        private String originalName;
        private String fileType;
        private Long fileSize;
        private String fileUrl;
        private String storageType;
        private String bucketName;
        private String path;
        private Long userId;
        private Integer status;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private Integer deleted;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder originalName(String originalName) {
            this.originalName = originalName;
            return this;
        }

        public Builder fileType(String fileType) {
            this.fileType = fileType;
            return this;
        }

        public Builder fileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder fileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
            return this;
        }

        public Builder storageType(String storageType) {
            this.storageType = storageType;
            return this;
        }

        public Builder bucketName(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder deleted(Integer deleted) {
            this.deleted = deleted;
            return this;
        }

        public FileEntity build() {
            FileEntity fileEntity = new FileEntity();
            fileEntity.id = this.id;
            fileEntity.fileName = this.fileName;
            fileEntity.originalName = this.originalName;
            fileEntity.fileType = this.fileType;
            fileEntity.fileSize = this.fileSize;
            fileEntity.fileUrl = this.fileUrl;
            fileEntity.storageType = this.storageType;
            fileEntity.bucketName = this.bucketName;
            fileEntity.path = this.path;
            fileEntity.userId = this.userId;
            fileEntity.status = this.status;
            fileEntity.createTime = this.createTime;
            fileEntity.updateTime = this.updateTime;
            fileEntity.deleted = this.deleted;
            return fileEntity;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}