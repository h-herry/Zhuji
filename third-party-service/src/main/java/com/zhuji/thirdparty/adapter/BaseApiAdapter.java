package com.zhuji.thirdparty.adapter;

import com.zhuji.common.core.enums.ErrorCode;
import com.zhuji.common.core.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public abstract class BaseApiAdapter<TRequest, TResponse> implements ApiAdapter<TRequest, TResponse> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected static final int DEFAULT_MAX_RETRIES = 3;
    protected static final long DEFAULT_RETRY_DELAY_MS = 1000;

    @Override
    public TResponse call(TRequest request) {
        return callWithRetry(request, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY_MS);
    }

    protected TResponse callWithRetry(TRequest request, int maxRetries, long delayMs) {
        BusinessException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return executeCall(request);
            } catch (Exception e) {
                lastException = new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        "API调用失败: " + e.getMessage());
                log.warn("API调用失败 (尝试 {}/{}): provider={}, error={}",
                        attempt, maxRetries, getProviderName(), e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw lastException;
                    }
                }
            }
        }

        throw lastException;
    }

    protected TResponse executeCall(TRequest request) {
        return doCall(request);
    }

    protected abstract TResponse doCall(TRequest request);

    protected <T> T executeWithFallback(Supplier<T> supplier, T fallbackValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("执行失败，使用默认值: error={}", e.getMessage());
            return fallbackValue;
        }
    }
}