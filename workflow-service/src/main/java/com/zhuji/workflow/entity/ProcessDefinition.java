package com.zhuji.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("wf_process_definition")
public class ProcessDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String processCode;

    private String processName;

    private String description;

    private Integer version;

    private Integer status;

    private String processXml;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    public ProcessDefinition() {
    }

    private ProcessDefinition(Builder builder) {
        this.id = builder.id;
        this.processCode = builder.processCode;
        this.processName = builder.processName;
        this.description = builder.description;
        this.version = builder.version;
        this.status = builder.status;
        this.processXml = builder.processXml;
        this.createBy = builder.createBy;
        this.createTime = builder.createTime;
        this.updateBy = builder.updateBy;
        this.updateTime = builder.updateTime;
        this.deleted = builder.deleted;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getProcessXml() {
        return processXml;
    }

    public void setProcessXml(String processXml) {
        this.processXml = processXml;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
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

    public static class Builder {
        private Long id;
        private String processCode;
        private String processName;
        private String description;
        private Integer version;
        private Integer status;
        private String processXml;
        private String createBy;
        private LocalDateTime createTime;
        private String updateBy;
        private LocalDateTime updateTime;
        private Integer deleted;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder processCode(String processCode) {
            this.processCode = processCode;
            return this;
        }

        public Builder processName(String processName) {
            this.processName = processName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder version(Integer version) {
            this.version = version;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder processXml(String processXml) {
            this.processXml = processXml;
            return this;
        }

        public Builder createBy(String createBy) {
            this.createBy = createBy;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder updateBy(String updateBy) {
            this.updateBy = updateBy;
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

        public ProcessDefinition build() {
            return new ProcessDefinition(this);
        }
    }
}