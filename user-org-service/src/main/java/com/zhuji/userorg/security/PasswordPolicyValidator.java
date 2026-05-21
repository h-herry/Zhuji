package com.zhuji.userorg.security;

import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordPolicyValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 32;
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("user.password.empty"));
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("user.password.length"));
        }

        if (!HAS_UPPERCASE.matcher(password).find()) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("user.password.uppercase"));
        }

        if (!HAS_LOWERCASE.matcher(password).find()) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("user.password.lowercase"));
        }

        if (!HAS_DIGIT.matcher(password).find()) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("user.password.digit"));
        }

        if (!HAS_SPECIAL.matcher(password).find()) {
            throw new BusinessException(400, I18nMessageUtil.getMessage("user.password.special"));
        }
    }
}
