package com.zhuji.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CompleteTaskRequest {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotBlank(message = "操作类型不能为空")
    private String action;

    private String comment;

    public CompleteTaskRequest() {
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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
}