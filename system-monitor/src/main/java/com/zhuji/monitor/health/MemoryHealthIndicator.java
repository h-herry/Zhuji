package com.zhuji.monitor.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

@Component
public class MemoryHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

        long heapUsed = heapUsage.getUsed();
        long heapMax = heapUsage.getMax();
        double heapUsagePercent = (double) heapUsed / heapMax * 100;

        Health.Builder builder = new Health.Builder()
                .withDetail("heap.used", formatBytes(heapUsed))
                .withDetail("heap.max", formatBytes(heapMax))
                .withDetail("heap.usage.percent", String.format("%.2f%%", heapUsagePercent))
                .withDetail("nonHeap.used", formatBytes(nonHeapUsage.getUsed()));

        if (heapUsagePercent > 90) {
            return builder.down()
                    .withDetail("status", "CRITICAL - Memory usage is above 90%")
                    .build();
        } else if (heapUsagePercent > 75) {
            return builder.up()
                    .withDetail("status", "WARNING - Memory usage is above 75%")
                    .build();
        }

        return builder.up().build();
    }

    private String formatBytes(long bytes) {
        long mb = bytes / (1024 * 1024);
        return mb + " MB";
    }
}