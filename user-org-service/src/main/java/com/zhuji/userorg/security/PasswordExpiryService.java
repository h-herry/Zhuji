package com.zhuji.userorg.security;

import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PasswordExpiryService {

    @Value("${security.password.expiry-days:90}")
    private int passwordExpiryDays;

    @Value("${security.password.expiry-warning-days:7}")
    private int passwordExpiryWarningDays;

    public void checkPasswordExpiry(User user) {
        if (user.getPasswordUpdateTime() == null) {
            return;
        }

        long daysSinceUpdate = ChronoUnit.DAYS.between(
            user.getPasswordUpdateTime(),
            LocalDateTime.now()
        );

        if (daysSinceUpdate > passwordExpiryDays) {
            throw new BusinessException(403, I18nMessageUtil.getMessage("user.password.expired"));
        }
    }

    public Integer getPasswordExpiryWarning(User user) {
        if (user.getPasswordUpdateTime() == null) {
            return null;
        }

        long daysSinceUpdate = ChronoUnit.DAYS.between(
            user.getPasswordUpdateTime(),
            LocalDateTime.now()
        );

        int remainingDays = (int) (passwordExpiryDays - daysSinceUpdate);

        if (remainingDays > 0 && remainingDays <= passwordExpiryWarningDays) {
            return remainingDays;
        }

        return null;
    }

    public int getPasswordExpiryDays() {
        return passwordExpiryDays;
    }
}
