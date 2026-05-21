package com.zhuji.userorg.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_i18n_message")
public class I18nMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("message_key")
    private String messageKey;

    @TableField("locale")
    private String locale;

    @TableField("message_value")
    private String messageValue;

    @TableField("module")
    private String module;

    @TableField("description")
    private String description;

    @TableField("is_active")
    private Integer isActive;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
