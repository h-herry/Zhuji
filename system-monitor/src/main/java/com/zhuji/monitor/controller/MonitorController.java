package com.zhuji.monitor.controller;

import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.monitor.service.MetricsCollectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "系统监控")
@RestController
@RequestMapping("/api/v1/monitor")
public class MonitorController {

    private final MetricsCollectorService metricsCollectorService;

    public MonitorController(MetricsCollectorService metricsCollectorService) {
        this.metricsCollectorService = metricsCollectorService;
    }

    @Operation(summary = "获取系统指标")
    @GetMapping("/metrics")
    public ApiResponse<Map<String, Object>> getMetrics() {
        return ApiResponse.success(metricsCollectorService.collectSystemMetrics());
    }

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> metrics = metricsCollectorService.collectSystemMetrics();
        return ApiResponse.success(metrics);
    }
}