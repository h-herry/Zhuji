package com.zhuji.thirdparty.dto;

import java.time.LocalDateTime;

public class ApiResponse {

    private boolean success;
    private Integer code;
    private String message;
    private Object data;
    private Long costTime;
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public static ApiResponse success(Object data) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage("Success");
        response.setData(data);
        return response;
    }

    public static ApiResponse success(Object data, Long costTime) {
        ApiResponse response = success(data);
        response.setCostTime(costTime);
        return response;
    }

    public static ApiResponse error(int code, String message) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Long getCostTime() {
        return costTime;
    }

    public void setCostTime(Long costTime) {
        this.costTime = costTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}