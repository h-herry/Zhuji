package com.zhuji.thirdparty.service;

import com.zhuji.thirdparty.dto.ApiResponse;

public interface ThirdPartyService {

    ApiResponse callApi(String provider, String method, Object payload);

    ApiResponse callSmsApi(String phone, String templateCode, String templateParam);

    ApiResponse callHttpApi(String method, String url, Object payload);
}