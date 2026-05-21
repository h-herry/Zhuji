package com.zhuji.userorg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuji.common.core.exception.BusinessException;
import com.zhuji.userorg.entity.I18nMessage;
import com.zhuji.userorg.mapper.I18nMessageMapper;
import com.zhuji.userorg.service.I18nMessageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class I18nMessageServiceImpl extends ServiceImpl<I18nMessageMapper, I18nMessage> implements I18nMessageService {

    private final Map<String, Map<String, String>> messageCache = new ConcurrentHashMap<>();

    @Autowired
    private I18nMessageMapper i18nMessageMapper;

    @PostConstruct
    public void init() {
        refreshMessageCache();
    }

    @Override
    public String getMessage(String messageKey, String locale) {
        if (!StringUtils.hasText(locale)) {
            locale = LocaleContextHolder.getLocale().toString();
        }

        Map<String, String> localeMessages = messageCache.get(locale);
        if (localeMessages != null && localeMessages.containsKey(messageKey)) {
            return localeMessages.get(messageKey);
        }

        String dbMessage = i18nMessageMapper.selectMessageByKeyAndLocale(messageKey, locale);
        if (StringUtils.hasText(dbMessage)) {
            return dbMessage;
        }

        return null;
    }

    @Override
    public String getMessage(String messageKey, String locale, Object... args) {
        String message = getMessage(messageKey, locale);
        if (message != null && args != null && args.length > 0) {
            return MessageFormat.format(message, args);
        }
        return message;
    }

    @Override
    public Map<String, String> getMessagesByLocale(String locale) {
        Map<String, String> cachedMessages = messageCache.get(locale);
        if (cachedMessages != null) {
            return new HashMap<>(cachedMessages);
        }

        List<I18nMessage> messages = i18nMessageMapper.selectByLocale(locale);
        Map<String, String> result = new HashMap<>();
        for (I18nMessage message : messages) {
            result.put(message.getMessageKey(), message.getMessageValue());
        }
        messageCache.put(locale, result);
        return result;
    }

    @Override
    public Map<String, String> getMessagesByModule(String locale, String module) {
        LambdaQueryWrapper<I18nMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(I18nMessage::getLocale, locale)
                .eq(I18nMessage::getModule, module)
                .eq(I18nMessage::getIsActive, 1);

        List<I18nMessage> messages = list(wrapper);
        Map<String, String> result = new HashMap<>();
        for (I18nMessage message : messages) {
            result.put(message.getMessageKey(), message.getMessageValue());
        }
        return result;
    }

    @Override
    public Page<I18nMessage> pageMessages(int page, int size, String messageKey, String locale, String module) {
        LambdaQueryWrapper<I18nMessage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(messageKey)) {
            wrapper.like(I18nMessage::getMessageKey, messageKey);
        }
        if (StringUtils.hasText(locale)) {
            wrapper.eq(I18nMessage::getLocale, locale);
        }
        if (StringUtils.hasText(module)) {
            wrapper.eq(I18nMessage::getModule, module);
        }
        wrapper.orderByDesc(I18nMessage::getCreateTime);

        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateMessage(I18nMessage message) {
        if (message.getId() == null) {
            LambdaQueryWrapper<I18nMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(I18nMessage::getMessageKey, message.getMessageKey())
                    .eq(I18nMessage::getLocale, message.getLocale());

            I18nMessage existing = getOne(wrapper);
            if (existing != null) {
                throw new BusinessException("该语言的消息键已存在");
            }

            message.setCreateTime(LocalDateTime.now());
            message.setUpdateTime(LocalDateTime.now());
            message.setIsActive(1);
            message.setDeleted(0);
        } else {
            message.setUpdateTime(LocalDateTime.now());
        }

        boolean result = saveOrUpdate(message);
        if (result) {
            refreshMessageCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Long id) {
        boolean result = removeById(id);
        if (result) {
            refreshMessageCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteMessages(List<Long> ids) {
        boolean result = removeByIds(ids);
        if (result) {
            refreshMessageCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleMessage(Long id, Integer isActive) {
        I18nMessage message = getById(id);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        message.setIsActive(isActive);
        message.setUpdateTime(LocalDateTime.now());
        boolean result = updateById(message);
        if (result) {
            refreshMessageCache();
        }
        return result;
    }

    @Override
    public List<I18nMessage> getAllActiveMessages() {
        LambdaQueryWrapper<I18nMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(I18nMessage::getIsActive, 1);
        return list(wrapper);
    }

    @Override
    public void refreshMessageCache() {
        log.info("刷新多语言消息缓存...");
        messageCache.clear();

        LambdaQueryWrapper<I18nMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(I18nMessage::getIsActive, 1);
        List<I18nMessage> allMessages = i18nMessageMapper.selectList(wrapper);

        for (I18nMessage message : allMessages) {
            String locale = message.getLocale();
            messageCache.computeIfAbsent(locale, k -> new HashMap<>());
            messageCache.get(locale).put(message.getMessageKey(), message.getMessageValue());
        }

        log.info("多语言消息缓存刷新完成，共加载 {} 条消息", allMessages.size());
    }
}
