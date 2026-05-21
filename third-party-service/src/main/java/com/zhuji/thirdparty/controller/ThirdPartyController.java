package com.zhuji.thirdparty.controller;

import com.zhuji.thirdparty.dto.ApiResponse;
import com.zhuji.thirdparty.service.ThirdPartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "第三方集成API")
@RestController
@RequestMapping("/api/v1/thirdparty")
public class ThirdPartyController {

    private final ThirdPartyService thirdPartyService;

    public ThirdPartyController(ThirdPartyService thirdPartyService) {
        this.thirdPartyService = thirdPartyService;
    }

    @Operation(summary = "通用API调用")
    @PostMapping("/call")
    public ApiResponse callApi(@RequestParam String provider,
                               @RequestParam String method,
                               @RequestBody(required = false) Object payload) {
        return thirdPartyService.callApi(provider, method, payload);
    }

    @Operation(summary = "发送短信")
    @PostMapping("/sms/send")
    public ApiResponse sendSms(@RequestParam String phone,
                               @RequestParam String templateCode,
                               @RequestParam(required = false) String templateParam) {
        return thirdPartyService.callSmsApi(phone, templateCode, templateParam);
    }

    @Operation(summary = "HTTP GET请求")
    @GetMapping("/http/get")
    public ApiResponse httpGet(@RequestParam String url) {
        return thirdPartyService.callHttpApi("GET", url, null);
    }

    @Operation(summary = "HTTP POST请求")
    @PostMapping("/http/post")
    public ApiResponse httpPost(@RequestParam String url,
                                @RequestBody(required = false) Object payload) {
        return thirdPartyService.callHttpApi("POST", url, payload);
    }

    @Operation(summary = "HTTP PUT请求")
    @PutMapping("/http/put")
    public ApiResponse httpPut(@RequestParam String url,
                               @RequestBody(required = false) Object payload) {
        return thirdPartyService.callHttpApi("PUT", url, payload);
    }

    @Operation(summary = "HTTP DELETE请求")
    @DeleteMapping("/http/delete")
    public ApiResponse httpDelete(@RequestParam String url) {
        return thirdPartyService.callHttpApi("DELETE", url, null);
    }

    @Operation(summary = "获取支持的提供商列表")
    @GetMapping("/providers")
    public ApiResponse getProviders() {
        return ApiResponse.success(new String[]{"SMS", "HTTP"});
    }
}