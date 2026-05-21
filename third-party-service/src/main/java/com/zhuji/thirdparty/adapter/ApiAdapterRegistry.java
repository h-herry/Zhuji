package com.zhuji.thirdparty.adapter;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApiAdapterRegistry {

    private static final Logger log = LoggerFactory.getLogger(ApiAdapterRegistry.class);

    private final List<ApiAdapter> adapters;
    private final Map<String, ApiAdapter> adapterMap = new HashMap<>();

    public ApiAdapterRegistry(List<ApiAdapter> adapters) {
        this.adapters = adapters;
    }

    @PostConstruct
    public void init() {
        for (ApiAdapter adapter : adapters) {
            String providerName = adapter.getProviderName();
            adapterMap.put(providerName.toLowerCase(), adapter);
            log.info("注册API适配器: provider={}", providerName);
        }
        log.info("共注册 {} 个API适配器", adapterMap.size());
    }

    public ApiAdapter getAdapter(String provider) {
        ApiAdapter adapter = adapterMap.get(provider.toLowerCase());
        if (adapter == null) {
            throw new IllegalArgumentException("未找到provider对应的适配器: " + provider);
        }
        return adapter;
    }

    public boolean hasAdapter(String provider) {
        return adapterMap.containsKey(provider.toLowerCase());
    }

    public Map<String, ApiAdapter> getAllAdapters() {
        return new HashMap<>(adapterMap);
    }
}