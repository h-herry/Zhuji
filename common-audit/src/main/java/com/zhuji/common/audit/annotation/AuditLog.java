package com.zhuji.common.audit.annotation;

import java.lang.annotation.*;

/**
 * 操作审计日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 操作类型
     */
    String type() default "OTHER";

    /**
     * 是否保存请求参数
     */
    boolean saveRequest() default true;

    /**
     * 是否保存响应结果
     */
    boolean saveResponse() default false;
}
