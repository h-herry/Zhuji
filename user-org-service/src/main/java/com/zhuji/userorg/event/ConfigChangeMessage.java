package com.zhuji.userorg.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigChangeMessage implements Serializable {
    private Long userId;
    private String configKey;
    private String operation;
    private Long timestamp;
}
