package com.zhuji.userorg.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.common.i18n.util.I18nMessageUtil;
import com.zhuji.userorg.entity.UserPasswordHistory;
import com.zhuji.userorg.mapper.UserPasswordHistoryMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PasswordHistoryService {

    private final UserPasswordHistoryMapper passwordHistoryMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.password.history-count:5}")
    private int passwordHistoryCount;

    public PasswordHistoryService(UserPasswordHistoryMapper passwordHistoryMapper,
                                  PasswordEncoder passwordEncoder) {
        this.passwordHistoryMapper = passwordHistoryMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public void validatePasswordNotInHistory(Long userId, String newPassword) {
        List<UserPasswordHistory> historyList = passwordHistoryMapper.selectRecentByUserId(userId, passwordHistoryCount);

        for (UserPasswordHistory history : historyList) {
            if (passwordEncoder.matches(newPassword, history.getPassword())) {
                throw new BusinessException(400,
                    I18nMessageUtil.getMessage("user.password.in.history", passwordHistoryCount));
            }
        }
    }

    @Transactional
    public void savePasswordHistory(Long userId, String encodedPassword) {
        UserPasswordHistory history = new UserPasswordHistory();
        history.setUserId(userId);
        history.setPassword(encodedPassword);
        passwordHistoryMapper.insert(history);

        cleanOldPasswordHistory(userId);
    }

    private void cleanOldPasswordHistory(Long userId) {
        List<UserPasswordHistory> historyList = passwordHistoryMapper.selectList(
            new LambdaQueryWrapper<UserPasswordHistory>()
                .eq(UserPasswordHistory::getUserId, userId)
                .orderByDesc(UserPasswordHistory::getCreateTime)
        );

        if (historyList.size() > passwordHistoryCount) {
            List<Long> idsToDelete = historyList.stream()
                .skip(passwordHistoryCount)
                .map(UserPasswordHistory::getId)
                .toList();

            passwordHistoryMapper.deleteBatchIds(idsToDelete);
        }
    }
}
