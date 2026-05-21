package com.zhuji.common.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StringUtils 工具类测试
 */
@DisplayName("StringUtils 工具类测试")
class StringUtilsTest {

    @Test
    @DisplayName("测试 isEmpty 方法")
    void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("test"));
    }

    @Test
    @DisplayName("测试 isBlank 方法")
    void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertFalse(StringUtils.isBlank("test"));
    }

    @Test
    @DisplayName("测试 isNotEmpty 方法")
    void testIsNotEmpty() {
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("test"));
    }

    @Test
    @DisplayName("测试 isNotBlank 方法")
    void testIsNotBlank() {
        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank(" "));
        assertTrue(StringUtils.isNotBlank("test"));
    }

    @Test
    @DisplayName("测试 trim 方法")
    void testTrim() {
        assertEquals("", StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim(" "));
        assertEquals("test", StringUtils.trim(" test "));
    }

    @Test
    @DisplayName("测试 equals 方法")
    void testEquals() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "test"));
        assertFalse(StringUtils.equals("test", null));
        assertTrue(StringUtils.equals("test", "test"));
        assertFalse(StringUtils.equals("test", "Test"));
    }

    @Test
    @DisplayName("测试 equalsIgnoreCase 方法")
    void testEqualsIgnoreCase() {
        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "test"));
        assertFalse(StringUtils.equalsIgnoreCase("test", null));
        assertTrue(StringUtils.equalsIgnoreCase("test", "test"));
        assertTrue(StringUtils.equalsIgnoreCase("test", "Test"));
    }

    @Test
    @DisplayName("测试 startsWith 方法")
    void testStartsWith() {
        assertTrue(StringUtils.startsWith("test", "te"));
        assertFalse(StringUtils.startsWith("test", "TE"));
        assertFalse(StringUtils.startsWith(null, "test"));
        assertFalse(StringUtils.startsWith("test", null));
    }

    @Test
    @DisplayName("测试 endsWith 方法")
    void testEndsWith() {
        assertTrue(StringUtils.endsWith("test", "st"));
        assertFalse(StringUtils.endsWith("test", "ST"));
        assertFalse(StringUtils.endsWith(null, "test"));
        assertFalse(StringUtils.endsWith("test", null));
    }

    @Test
    @DisplayName("测试 contains 方法")
    void testContains() {
        assertTrue(StringUtils.contains("test", "es"));
        assertFalse(StringUtils.contains("test", "ES"));
        assertFalse(StringUtils.contains(null, "test"));
        assertFalse(StringUtils.contains("test", null));
    }

    @Test
    @DisplayName("测试 join 方法")
    void testJoin() {
        assertEquals("", StringUtils.join(null, ","));
        assertEquals("a,b,c", StringUtils.join(new String[]{"a", "b", "c"}, ","));
        assertEquals("a", StringUtils.join(new String[]{"a"}, ","));
    }

    @Test
    @DisplayName("测试 lowerCase 方法")
    void testLowerCase() {
        assertNull(StringUtils.lowerCase(null));
        assertEquals("", StringUtils.lowerCase(""));
        assertEquals("test", StringUtils.lowerCase("TEST"));
    }

    @Test
    @DisplayName("测试 upperCase 方法")
    void testUpperCase() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("", StringUtils.upperCase(""));
        assertEquals("TEST", StringUtils.upperCase("test"));
    }
}
