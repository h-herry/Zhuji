package com.zhuji.userorg.config.validator;

import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class NumberConfigValidator implements ConfigValidator {

    private static final List<String> NUMBER_KEYS = Arrays.asList(
        "user.max.login.count",
        "session.timeout"
    );

    @Override
    public boolean supports(String configKey) {
        return NUMBER_KEYS.stream().anyMatch(configKey::startsWith);
    }

    @Override
    public void validate(String configKey, String configValue) {
        try {
            int value = Integer.parseInt(configValue);
            if (value <= 0) {
                throw new BusinessException(400, I18nMessageUtil.getMessage("config.value.invalid"));
            }
        } catch (NumberFormatException e) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("config.value.invalid"));
        }
    }
}
