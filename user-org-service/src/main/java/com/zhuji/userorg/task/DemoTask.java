package com.zhuji.userorg.task;

import com.zhuji.common.task.annotation.ScheduledLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 示例定时任务
 */
@Slf4j
@Component
public class DemoTask {

    /**
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    @ScheduledLog(name = "每分钟执行的任务")
    public void perMinuteTask() {
        log.info("执行每分钟一次的任务...");
    }

    /**
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    @ScheduledLog(name = "每5分钟执行的任务")
    public void perFiveMinutesTask() {
        log.info("执行每5分钟一次的任务...");
    }

    /**
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @ScheduledLog(name = "每天凌晨2点执行的任务")
    public void dailyTask() {
        log.info("执行每天凌晨2点的任务...");
    }
}
