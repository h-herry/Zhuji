package com.zhuji.thirdparty.service.impl;

import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.thirdparty.adapter.ApiAdapterRegistry;
import com.zhuji.thirdparty.adapter.http.HttpApiAdapter;
import com.zhuji.thirdparty.adapter.http.HttpRequest;
import com.zhuji.thirdparty.adapter.http.HttpResponse;
import com.zhuji.thirdparty.adapter.sms.SmsAdapter;
import com.zhuji.thirdparty.adapter.sms.SmsRequest;
import com.zhuji.thirdparty.adapter.sms.SmsResponse;
import com.zhuji.thirdparty.dto.ApiResponse;
import com.zhuji.thirdparty.service.ThirdPartyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ThirdPartyServiceImpl implements ThirdPartyService {

    private static final Logger log = LoggerFactory.getLogger(ThirdPartyServiceImpl.class);

    private final ApiAdapterRegistry adapterRegistry;

    public ThirdPartyServiceImpl(ApiAdapterRegistry adapterRegistry) {
        this.adapterRegistry = adapterRegistry;
    }

    @Override
    public ApiResponse callApi(String provider, String method, Object payload) {
        log.info("第三方API调用: provider={}, method={}", provider, method);
        long startTime = System.currentTimeMillis();

        try {
            if (!adapterRegistry.hasAdapter(provider)) {
                throw new BusinessException(404, I18nMessageUtil.getMessage("thirdparty.provider.not.found", provider));
            }

            Object result = null;
            switch (provider.toUpperCase()) {
                case "SMS":
                    result = callSms((SmsRequest) payload);
                    break;
                case "HTTP":
                    result = callHttp((HttpRequest) payload);
                    break;
                default:
                    throw new BusinessException(400, I18nMessageUtil.getMessage("thirdparty.provider.unsupported", provider));
            }

            long costTime = System.currentTimeMillis() - startTime;
            return ApiResponse.success(result, costTime);

        } catch (BusinessException e) {
            log.error("第三方API调用失败: provider={}, error={}", provider, e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("第三方API调用异常: provider={}, error={}", provider, e.getMessage());
            return ApiResponse.error(500, I18nMessageUtil.getMessage("thirdparty.call.failed", e.getMessage()));
        }
    }

    @Override
    public ApiResponse callSmsApi(String phone, String templateCode, String templateParam) {
        log.info("发送短信: phone={}, templateCode={}", phone, templateCode);
        long startTime = System.currentTimeMillis();

        try {
            SmsRequest request = new SmsRequest();
            request.setPhone(phone);
            request.setTemplateCode(templateCode);
            request.setTemplateParam(templateParam);

            SmsResponse response = callSms(request);
            long costTime = System.currentTimeMillis() - startTime;

            return ApiResponse.success(response, costTime);
        } catch (Exception e) {
            log.error("发送短信失败: error={}", e.getMessage());
            return ApiResponse.error(500, I18nMessageUtil.getMessage("thirdparty.sms.send.failed"));
        }
    }

    @Override
    public ApiResponse callHttpApi(String method, String url, Object payload) {
        log.info("HTTP API调用: method={}, url={}", method, url);
        long startTime = System.currentTimeMillis();

        try {
            HttpRequest request = new HttpRequest(method, url);
            request.setBody(payload);

            HttpResponse response = callHttp(request);
            long costTime = System.currentTimeMillis() - startTime;

            return ApiResponse.success(response, costTime);
        } catch (Exception e) {
            log.error("HTTP API调用失败: error={}", e.getMessage());
            return ApiResponse.error(500, I18nMessageUtil.getMessage("thirdparty.http.call.failed"));
        }
    }

    private SmsResponse callSms(SmsRequest request) {
        SmsAdapter adapter = (SmsAdapter) adapterRegistry.getAdapter("SMS");
        return adapter.call(request);
    }

    private HttpResponse callHttp(HttpRequest request) {
        HttpApiAdapter adapter = (HttpApiAdapter) adapterRegistry.getAdapter("HTTP");
        return adapter.call(request);
    }
}