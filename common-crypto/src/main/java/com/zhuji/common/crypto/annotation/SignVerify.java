package com.zhuji.common.crypto.annotation;

import java.lang.annotation.*;

/**
 * 接口签名校验注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SignVerify {

    /**
     * 签名参数名
     */
    String signParam() default "sign";

    /**
     * 时间戳参数名
     */
    String timestampParam() default "timestamp";

    /**
     * 时间戳有效时间（秒）
     */
    long validTime() default 300;

    /**
     * 是否校验AppKey
     */
    boolean checkAppKey() default true;
}
