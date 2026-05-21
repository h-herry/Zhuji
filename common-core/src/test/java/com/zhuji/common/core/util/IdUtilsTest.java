package com.zhuji.common.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IdUtils 工具类测试
 */
@DisplayName("IdUtils 工具类测试")
class IdUtilsTest {

    @Test
    @DisplayName("测试 nextId 方法")
    void testNextId() {
        long id1 = IdUtils.nextId();
        long id2 = IdUtils.nextId();
        assertTrue(id1 > 0);
        assertTrue(id2 > 0);
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("测试 uuid 方法")
    void testUuid() {
        String uuid1 = IdUtils.uuid();
        String uuid2 = IdUtils.uuid();
        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertEquals(32, uuid1.length());
        assertEquals(36, uuid2.length());
        assertNotEquals(uuid1, uuid2);
    }

    @Test
    @DisplayName("测试 simpleUUID 方法")
    void testSimpleUUID() {
        String uuid = IdUtils.simpleUUID();
        assertNotNull(uuid);
        assertEquals(32, uuid.length());
        assertFalse(uuid.contains("-"));
    }

    @Test
    @DisplayName("测试 randomUUID 方法")
    void testRandomUUID() {
        String uuid = IdUtils.randomUUID();
        assertNotNull(uuid);
        assertEquals(36, uuid.length());
        assertTrue(uuid.contains("-"));
    }

    @Test
    @DisplayName("测试生成ID的唯一性")
    void testIdUniqueness() {
        long[] ids = new long[100];
        for (int i = 0; i < 100; i++) {
            ids[i] = IdUtils.nextId();
        }
        for (int i = 0; i < 100; i++) {
            for (int j = i + 1; j < 100; j++) {
                assertNotEquals(ids[i], ids[j], "ID不唯一: " + ids[i] + " 和 " + ids[j]);
            }
        }
    }

    @Test
    @DisplayName("测试生成UUID的唯一性")
    void testUuidUniqueness() {
        String[] uuids = new String[100];
        for (int i = 0; i < 100; i++) {
            uuids[i] = IdUtils.uuid();
        }
        for (int i = 0; i < 100; i++) {
            for (int j = i + 1; j < 100; j++) {
                assertNotEquals(uuids[i], uuids[j], "UUID不唯一: " + uuids[i] + " 和 " + uuids[j]);
            }
        }
    }
}
