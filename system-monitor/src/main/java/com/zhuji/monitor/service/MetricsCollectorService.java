package com.zhuji.monitor.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MetricsCollectorService {

    private final MeterRegistry meterRegistry;

    public MetricsCollectorService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Map<String, Object> collectSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

        metrics.put("memory.heap.used", heapUsage.getUsed());
        metrics.put("memory.heap.committed", heapUsage.getCommitted());
        metrics.put("memory.heap.max", heapUsage.getMax());
        metrics.put("memory.nonHeap.used", nonHeapUsage.getUsed());
        metrics.put("memory.nonHeap.committed", nonHeapUsage.getCommitted());

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        metrics.put("thread.count", threadMXBean.getThreadCount());
        metrics.put("thread.peak.count", threadMXBean.getPeakThreadCount());
        metrics.put("thread.daemon.count", threadMXBean.getDaemonThreadCount());

        Runtime runtime = Runtime.getRuntime();
        metrics.put("runtime.free.memory", runtime.freeMemory());
        metrics.put("runtime.total.memory", runtime.totalMemory());
        metrics.put("runtime.max.memory", runtime.maxMemory());
        metrics.put("runtime.available.processors", runtime.availableProcessors());

        return metrics;
    }

    public void recordMethodExecutionTime(String methodName, long durationMs) {
        Timer timer = meterRegistry.timer("method.execution.time", "method", methodName);
        timer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void incrementCounter(String name, String... tags) {
        meterRegistry.counter(name, Tags.of(tags)).increment();
    }

    public void recordGauge(String name, Number value, String... tags) {
        meterRegistry.gauge(name, Tags.of(tags), value);
    }
}