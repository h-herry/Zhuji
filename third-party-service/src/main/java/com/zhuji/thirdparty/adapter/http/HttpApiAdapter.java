package com.zhuji.thirdparty.adapter.http;

import com.alibaba.fastjson2.JSON;
import com.zhuji.common.core.enums.ErrorCode;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.thirdparty.adapter.BaseApiAdapter;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HttpApiAdapter extends BaseApiAdapter<HttpRequest, HttpResponse> {

    private static final Logger log = LoggerFactory.getLogger(HttpApiAdapter.class);

    @Autowired(required = false)
    private HttpClient httpClient;

    public HttpApiAdapter() {
        if (this.httpClient == null) {
            this.httpClient = HttpClients.createDefault();
        }
    }

    @Override
    public String getProviderName() {
        return "HTTP";
    }

    @Override
    protected HttpResponse doCall(HttpRequest request) {
        log.info("HTTP API调用: method={}, url={}", request.getMethod(), request.getUrl());

        try {
            return executeHttpRequest(request);
        } catch (IOException e) {
            log.error("HTTP请求失败: url={}, error={}", request.getUrl(), e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                    "HTTP请求失败: " + e.getMessage());
        }
    }

    private HttpResponse executeHttpRequest(HttpRequest request) throws IOException {
        HttpResponse response = new HttpResponse();

        switch (request.getMethod().toUpperCase()) {
            case "GET":
                executeGet(request, response);
                break;
            case "POST":
                executePost(request, response);
                break;
            case "PUT":
                executePut(request, response);
                break;
            case "DELETE":
                executeDelete(request, response);
                break;
            default:
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "不支持的HTTP方法: " + request.getMethod());
        }

        return response;
    }

    private void executeGet(HttpRequest request, HttpResponse response) throws IOException {
        HttpGet httpGet = new HttpGet(request.getUrl());
        addHeaders(httpGet, request);

        httpClient.execute(httpGet, (ClassicHttpResponse httpResponse) -> {
            parseResponse(httpResponse, response);
            return null;
        });
    }

    private void executePost(HttpRequest request, HttpResponse response) throws IOException {
        HttpPost httpPost = new HttpPost(request.getUrl());
        addHeaders(httpPost, request);
        if (request.getBody() != null) {
            String jsonBody = JSON.toJSONString(request.getBody());
            httpPost.setEntity(new StringEntity(jsonBody));
        }

        httpClient.execute(httpPost, (ClassicHttpResponse httpResponse) -> {
            parseResponse(httpResponse, response);
            return null;
        });
    }

    private void executePut(HttpRequest request, HttpResponse response) throws IOException {
        HttpPut httpPut = new HttpPut(request.getUrl());
        addHeaders(httpPut, request);
        if (request.getBody() != null) {
            String jsonBody = JSON.toJSONString(request.getBody());
            httpPut.setEntity(new StringEntity(jsonBody));
        }

        httpClient.execute(httpPut, (ClassicHttpResponse httpResponse) -> {
            parseResponse(httpResponse, response);
            return null;
        });
    }

    private void executeDelete(HttpRequest request, HttpResponse response) throws IOException {
        HttpDelete httpDelete = new HttpDelete(request.getUrl());
        addHeaders(httpDelete, request);

        httpClient.execute(httpDelete, (ClassicHttpResponse httpResponse) -> {
            parseResponse(httpResponse, response);
            return null;
        });
    }

    private void addHeaders(org.apache.hc.client5.http.classic.methods.HttpUriRequestBase httpRequest, HttpRequest request) {
        if (request.getHeaders() != null) {
            request.getHeaders().forEach(httpRequest::setHeader);
        }
        httpRequest.setHeader("Content-Type", "application/json");
        httpRequest.setHeader("Accept", "application/json");
    }

    private void parseResponse(ClassicHttpResponse httpResponse, HttpResponse response) {
        response.setStatusCode(httpResponse.getCode());
        try {
            String body = EntityUtils.toString(httpResponse.getEntity());
            response.setBody(body);
            response.setSuccess(httpResponse.getCode() >= 200 && httpResponse.getCode() < 300);
            log.info("HTTP响应: status={}, body={}", httpResponse.getCode(), body);
        } catch (ParseException e) {
            log.error("解析响应失败: error={}", e.getMessage());
            response.setSuccess(false);
        } catch (IOException e) {
            log.error("读取响应失败: error={}", e.getMessage());
            response.setSuccess(false);
        }
    }
}