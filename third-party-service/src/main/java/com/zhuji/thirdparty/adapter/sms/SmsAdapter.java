package com.zhuji.thirdparty.adapter.sms;

import com.zhuji.thirdparty.adapter.BaseApiAdapter;
import org.springframework.stereotype.Component;

@Component
public class SmsAdapter extends BaseApiAdapter<SmsRequest, SmsResponse> {

    @Override
    public String getProviderName() {
        return "SMS";
    }

    @Override
    protected SmsResponse doCall(SmsRequest request) {
        log.info("发送短信: provider={}, phone={}, template={}",
                getProviderName(), request.getPhone(), request.getTemplateCode());

        SmsResponse response = new SmsResponse();
        response.setSuccess(true);
        response.setMessageId("SMS_" + System.currentTimeMillis());
        response.setPhone(request.getPhone());
        response.setStatus("DELIVERED");

        return response;
    }
}