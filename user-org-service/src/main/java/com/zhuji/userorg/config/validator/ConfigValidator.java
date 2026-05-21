package com.zhuji.userorg.config.validator;

public interface ConfigValidator {
    boolean supports(String configKey);
    void validate(String configKey, String configValue);
}
