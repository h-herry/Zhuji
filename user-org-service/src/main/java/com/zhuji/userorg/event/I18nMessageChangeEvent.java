package com.zhuji.userorg.event;

import org.springframework.context.ApplicationEvent;

public class I18nMessageChangeEvent extends ApplicationEvent {
    public I18nMessageChangeEvent(Object source) {
        super(source);
    }
}
