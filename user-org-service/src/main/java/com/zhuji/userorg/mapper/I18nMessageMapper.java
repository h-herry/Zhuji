package com.zhuji.userorg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuji.userorg.entity.I18nMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface I18nMessageMapper extends BaseMapper<I18nMessage> {

    @Select("SELECT * FROM sys_i18n_message WHERE locale = #{locale} AND is_active = 1 AND deleted = 0")
    List<I18nMessage> selectByLocale(@Param("locale") String locale);

    @Select("SELECT * FROM sys_i18n_message WHERE message_key = #{messageKey} AND is_active = 1 AND deleted = 0")
    List<I18nMessage> selectByMessageKey(@Param("messageKey") String messageKey);

    @Select("SELECT * FROM sys_i18n_message WHERE module = #{module} AND is_active = 1 AND deleted = 0")
    List<I18nMessage> selectByModule(@Param("module") String module);

    @Select("SELECT message_value FROM sys_i18n_message WHERE message_key = #{messageKey} AND locale = #{locale} AND is_active = 1 AND deleted = 0 LIMIT 1")
    String selectMessageByKeyAndLocale(@Param("messageKey") String messageKey, @Param("locale") String locale);
}
