package com.zhuji.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("wf_process_task")
public class ProcessTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long processInstanceId;

    private String processCode;

    private String businessKey;

    private String taskCode;

    private String taskName;

    private String nodeCode;

    private String nodeName;

    private String assignee;

    private String assigneeName;

    private Integer status;

    private String action;

    private String comment;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    public ProcessTask() {
    }

    private ProcessTask(Builder builder) {
        this.id = builder.id;
        this.processInstanceId = builder.processInstanceId;
        this.processCode = builder.processCode;
        this.businessKey = builder.businessKey;
        this.taskCode = builder.taskCode;
        this.taskName = builder.taskName;
        this.nodeCode = builder.nodeCode;
        this.nodeName = builder.nodeName;
        this.assignee = builder.assignee;
        this.assigneeName = builder.assigneeName;
        this.status = builder.status;
        this.action = builder.action;
        this.comment = builder.comment;
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

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
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

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
        private Long processInstanceId;
        private String processCode;
        private String businessKey;
        private String taskCode;
        private String taskName;
        private String nodeCode;
        private String nodeName;
        private String assignee;
        private String assigneeName;
        private Integer status;
        private String action;
        private String comment;
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

        public Builder processInstanceId(Long processInstanceId) {
            this.processInstanceId = processInstanceId;
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

        public Builder taskCode(String taskCode) {
            this.taskCode = taskCode;
            return this;
        }

        public Builder taskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder nodeCode(String nodeCode) {
            this.nodeCode = nodeCode;
            return this;
        }

        public Builder nodeName(String nodeName) {
            this.nodeName = nodeName;
            return this;
        }

        public Builder assignee(String assignee) {
            this.assignee = assignee;
            return this;
        }

        public Builder assigneeName(String assigneeName) {
            this.assigneeName = assigneeName;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
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

        public ProcessTask build() {
            return new ProcessTask(this);
        }
    }
}