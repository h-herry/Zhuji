package com.zhuji.common.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || isEmpty(pattern)) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime parse(String dateStr) {
        if (isEmpty(dateStr)) {
            return null;
        }
        return LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER);
    }

    public static LocalDateTime parse(String dateStr, String pattern) {
        if (isEmpty(dateStr) || isEmpty(pattern)) {
            return null;
        }
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
