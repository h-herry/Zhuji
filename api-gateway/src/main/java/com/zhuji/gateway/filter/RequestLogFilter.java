package com.zhuji.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put("traceId", traceId);

        final String method = exchange.getRequest().getMethod().name();
        final String path = exchange.getRequest().getPath().toString();
        final String finalTraceId = traceId;
        String remoteAddr = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

        log.info("[Gateway] 请求开始 - {} {} - IP: {} - TraceId: {}",
                method, path, remoteAddr, traceId);

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    int statusCode = exchange.getResponse().getStatusCode().value();
                    log.info("[Gateway] 请求完成 - {} {} - Status: {} - TraceId: {}",
                            method, path, statusCode, finalTraceId);
                    MDC.remove("traceId");
                })
                .doOnError(error -> {
                    log.error("[Gateway] 请求异常 - {} {} - Error: {} - TraceId: {}",
                            method, path, error.getMessage(), finalTraceId);
                    MDC.remove("traceId");
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}