package com.zhuji.thirdparty.dto;

import jakarta.validation.constraints.NotBlank;

public class ApiRequest {

    @NotBlank(message = "provider不能为空")
    private String provider;

    @NotBlank(message = "method不能为空")
    private String method;

    private String endpoint;

    private Object payload;

    private String format;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}