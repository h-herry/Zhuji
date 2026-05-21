package com.zhuji.monitor.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;

@Component
public class ThreadHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        int peakThreadCount = threadMXBean.getPeakThreadCount();
        int threadCount = threadMXBean.getThreadCount();
        int daemonThreadCount = threadMXBean.getDaemonThreadCount();
        long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();

        long[] threadIds = threadMXBean.getAllThreadIds();
        int blockedCount = 0;
        int waitingCount = 0;

        for (long threadId : threadIds) {
            ThreadInfo info = threadMXBean.getThreadInfo(threadId);
            if (info != null) {
                if (info.getThreadState() == Thread.State.BLOCKED) {
                    blockedCount++;
                } else if (info.getThreadState() == Thread.State.WAITING ||
                           info.getThreadState() == Thread.State.TIMED_WAITING) {
                    waitingCount++;
                }
            }
        }

        return Health.up()
                .withDetail("thread.count", threadCount)
                .withDetail("peak.thread.count", peakThreadCount)
                .withDetail("daemon.thread.count", daemonThreadCount)
                .withDetail("total.started.thread.count", totalStartedThreadCount)
                .withDetail("blocked.thread.count", blockedCount)
                .withDetail("waiting.thread.count", waitingCount)
                .build();
    }
}