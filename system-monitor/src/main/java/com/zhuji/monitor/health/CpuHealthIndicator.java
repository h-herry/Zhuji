package com.zhuji.monitor.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

@Component
public class CpuHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        int availableProcessors = osMXBean.getAvailableProcessors();
        double systemLoadAverage = osMXBean.getSystemLoadAverage();
        String osName = osMXBean.getName();
        String osVersion = osMXBean.getVersion();
        String osArch = osMXBean.getArch();

        long uptime = runtimeMXBean.getUptime();
        String vmName = runtimeMXBean.getVmName();
        String vmVersion = runtimeMXBean.getVmVersion();
        String vmVendor = runtimeMXBean.getVmVendor();

        return Health.up()
                .withDetail("available.processors", availableProcessors)
                .withDetail("system.load.average", String.format("%.2f", systemLoadAverage))
                .withDetail("os.name", osName)
                .withDetail("os.version", osVersion)
                .withDetail("os.arch", osArch)
                .withDetail("uptime", formatUptime(uptime))
                .withDetail("vm.name", vmName)
                .withDetail("vm.version", vmVersion)
                .withDetail("vm.vendor", vmVendor)
                .build();
    }

    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%d days, %d hours, %d minutes", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, seconds % 60);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
}