package com.zhuji.userorg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuji.userorg.entity.I18nMessage;

import java.util.List;
import java.util.Map;

public interface I18nMessageService extends IService<I18nMessage> {

    String getMessage(String messageKey, String locale);

    String getMessage(String messageKey, String locale, Object... args);

    Map<String, String> getMessagesByLocale(String locale);

    Map<String, String> getMessagesByModule(String locale, String module);

    Page<I18nMessage> pageMessages(int page, int size, String messageKey, String locale, String module);

    boolean saveOrUpdateMessage(I18nMessage message);

    boolean deleteMessage(Long id);

    boolean batchDeleteMessages(List<Long> ids);

    boolean toggleMessage(Long id, Integer isActive);

    List<I18nMessage> getAllActiveMessages();

    void refreshMessageCache();
}
