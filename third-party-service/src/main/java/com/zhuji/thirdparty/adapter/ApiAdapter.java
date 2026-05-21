package com.zhuji.thirdparty.adapter;

public interface ApiAdapter<TRequest, TResponse> {

    String getProviderName();

    TResponse call(TRequest request);

    default boolean isProviderAvailable() {
        return true;
    }
}