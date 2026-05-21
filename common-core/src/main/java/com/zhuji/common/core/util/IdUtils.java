package com.zhuji.common.core.util;

import java.util.concurrent.atomic.AtomicLong;

public class IdUtils {
    private static final long EPOCH = 1600000000000L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_ID = 1;
    private static final long DATACENTER_ID = 1;
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static volatile long LAST_TIMESTAMP = -1L;

    public static synchronized Long generateId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < LAST_TIMESTAMP) {
            throw new RuntimeException("Clock moved backwards");
        }

        if (timestamp == LAST_TIMESTAMP) {
            long sequence = SEQUENCE.incrementAndGet() & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(LAST_TIMESTAMP);
            }
        } else {
            SEQUENCE.set(0);
        }

        LAST_TIMESTAMP = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT) |
               (DATACENTER_ID << DATACENTER_ID_SHIFT) |
               (WORKER_ID << WORKER_ID_SHIFT) |
               SEQUENCE.get();
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
