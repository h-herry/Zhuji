package com.zhuji.common.cache.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    String key();

    long expireTime() default 30000;

    long waitTime() default 10000;

    long sleepTime() default 100;

    String lockType() default "simple";
}