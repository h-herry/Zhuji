package com.zhuji.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("wf_process_instance")
public class ProcessInstance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long processDefinitionId;

    private String processCode;

    private String businessKey;

    private String businessType;

    private Integer status;

    private String currentNodeCode;

    private String currentNodeName;

    private String startUserId;

    private String startUserName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    public ProcessInstance() {
    }

    private ProcessInstance(Builder builder) {
        this.id = builder.id;
        this.processDefinitionId = builder.processDefinitionId;
        this.processCode = builder.processCode;
        this.businessKey = builder.businessKey;
        this.businessType = builder.businessType;
        this.status = builder.status;
        this.currentNodeCode = builder.currentNodeCode;
        this.currentNodeName = builder.currentNodeName;
        this.startUserId = builder.startUserId;
        this.startUserName = builder.startUserName;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
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

    public Long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(Long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCurrentNodeCode() {
        return currentNodeCode;
    }

    public void setCurrentNodeCode(String currentNodeCode) {
        this.currentNodeCode = currentNodeCode;
    }

    public String getCurrentNodeName() {
        return currentNodeName;
    }

    public void setCurrentNodeName(String currentNodeName) {
        this.currentNodeName = currentNodeName;
    }

    public String getStartUserId() {
        return startUserId;
    }

    public void setStartUserId(String startUserId) {
        this.startUserId = startUserId;
    }

    public String getStartUserName() {
        return startUserName;
    }

    public void setStartUserName(String startUserName) {
        this.startUserName = startUserName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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
        private Long processDefinitionId;
        private String processCode;
        private String businessKey;
        private String businessType;
        private Integer status;
        private String currentNodeCode;
        private String currentNodeName;
        private String startUserId;
        private String startUserName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String createBy;
        private LocalDateTime createTime;
        private String updateBy;
        private LocalDateTime updateTime;
        private Integer deleted;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder processDefinitionId(Long processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
            return this;
        }

        public Builder processCode(String processCode) {
            this.processCode = processCode;
            return this;
        }

        public Builder businessKey(String businessKey) {
            this.businessKey = businessKey;
            return this;
        }

        public Builder businessType(String businessType) {
            this.businessType = businessType;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder currentNodeCode(String currentNodeCode) {
            this.currentNodeCode = currentNodeCode;
            return this;
        }

        public Builder currentNodeName(String currentNodeName) {
            this.currentNodeName = currentNodeName;
            return this;
        }

        public Builder startUserId(String startUserId) {
            this.startUserId = startUserId;
            return this;
        }

        public Builder startUserName(String startUserName) {
            this.startUserName = startUserName;
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
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

        public ProcessInstance build() {
            return new ProcessInstance(this);
        }
    }
}