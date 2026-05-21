package com.zhuji.common.task.annotation;

import java.lang.annotation.*;

/**
 * 定时任务日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ScheduledLog {

    /**
     * 任务名称
     */
    String name() default "";

    /**
     * 是否记录执行时间
     */
    boolean recordTime() default true;
}
