package com.zhuji.common.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DateUtils 工具类测试
 */
@DisplayName("DateUtils 工具类测试")
class DateUtilsTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    @DisplayName("测试 format 方法")
    void testFormat() {
        LocalDateTime now = LocalDateTime.now();
        String formatted = DateUtils.format(now);
        assertNotNull(formatted);
        assertEquals(19, formatted.length());
    }

    @Test
    @DisplayName("测试 format 方法带格式")
    void testFormatWithPattern() {
        LocalDateTime now = LocalDateTime.now();
        String formatted = DateUtils.format(now, "yyyy-MM-dd");
        assertNotNull(formatted);
        assertEquals(10, formatted.length());
    }

    @Test
    @DisplayName("测试 parse 方法")
    void testParse() {
        String dateStr = "2024-01-01 12:00:00";
        LocalDateTime date = DateUtils.parse(dateStr);
        assertNotNull(date);
        assertEquals(2024, date.getYear());
        assertEquals(1, date.getMonthValue());
        assertEquals(1, date.getDayOfMonth());
        assertEquals(12, date.getHour());
        assertEquals(0, date.getMinute());
        assertEquals(0, date.getSecond());
    }

    @Test
    @DisplayName("测试 parse 方法带格式")
    void testParseWithPattern() {
        String dateStr = "2024-01-01";
        LocalDateTime date = DateUtils.parse(dateStr, "yyyy-MM-dd");
        assertNotNull(date);
        assertEquals(2024, date.getYear());
        assertEquals(1, date.getMonthValue());
        assertEquals(1, date.getDayOfMonth());
    }

    @Test
    @DisplayName("测试 isAfter 方法")
    void testIsAfter() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusDays(1);
        assertTrue(DateUtils.isAfter(now, past));
        assertFalse(DateUtils.isAfter(past, now));
        assertFalse(DateUtils.isAfter(now, now));
    }

    @Test
    @DisplayName("测试 isBefore 方法")
    void testIsBefore() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusDays(1);
        assertTrue(DateUtils.isBefore(past, now));
        assertFalse(DateUtils.isBefore(now, past));
        assertFalse(DateUtils.isBefore(now, now));
    }

    @Test
    @DisplayName("测试 addDays 方法")
    void testAddDays() {
        LocalDateTime date = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        LocalDateTime result = DateUtils.addDays(date, 5);
        assertEquals(2024, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(6, result.getDayOfMonth());
    }

    @Test
    @DisplayName("测试 addHours 方法")
    void testAddHours() {
        LocalDateTime date = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        LocalDateTime result = DateUtils.addHours(date, 3);
        assertEquals(2024, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(15, result.getHour());
    }

    @Test
    @DisplayName("测试 addMinutes 方法")
    void testAddMinutes() {
        LocalDateTime date = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        LocalDateTime result = DateUtils.addMinutes(date, 30);
        assertEquals(2024, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(12, result.getHour());
        assertEquals(30, result.getMinute());
    }

    @Test
    @DisplayName("测试 toDate 方法")
    void testToDate() {
        LocalDateTime now = LocalDateTime.now();
        Date date = DateUtils.toDate(now);
        assertNotNull(date);
    }

    @Test
    @DisplayName("测试 toLocalDateTime 方法")
    void testToLocalDateTime() {
        Date date = new Date();
        LocalDateTime localDateTime = DateUtils.toLocalDateTime(date);
        assertNotNull(localDateTime);
    }
}
