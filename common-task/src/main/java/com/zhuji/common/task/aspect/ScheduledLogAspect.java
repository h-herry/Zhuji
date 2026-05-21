package com.zhuji.common.task.aspect;

import com.zhuji.common.task.annotation.ScheduledLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 定时任务日志切面
 */
@Slf4j
@Aspect
@Component
public class ScheduledLogAspect {

    @Around("@annotation(scheduledLog)")
    public Object around(ProceedingJoinPoint joinPoint, ScheduledLog scheduledLog) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        String taskName = scheduledLog.name().isEmpty() ? methodName : scheduledLog.name();

        long startTime = System.currentTimeMillis();
        log.info("定时任务开始执行: {}", taskName);

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            if (scheduledLog.recordTime()) {
                log.info("定时任务执行完成: {}, 耗时: {}ms", taskName, endTime - startTime);
            } else {
                log.info("定时任务执行完成: {}", taskName);
            }
            return result;
        } catch (Throwable e) {
            log.error("定时任务执行异常: {}", taskName, e);
            throw e;
        }
    }
}
