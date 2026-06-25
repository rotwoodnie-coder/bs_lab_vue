package com.xuanyue.exp.mobile.support;

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 作业截止日期：未填写时默认发布当日 +7 天，统一存 yyyy-MM-dd。
 */
public final class MobileHomeworkDateSupport {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int DEFAULT_DAYS_AFTER_ASSIGN = 7;

    private MobileHomeworkDateSupport() {
    }

    public static String resolveRequireDate(String raw) {
        if (StringUtils.hasText(raw)) {
            return normalizeToDateString(raw.trim());
        }
        return LocalDate.now(ZONE).plusDays(DEFAULT_DAYS_AFTER_ASSIGN).format(DATE);
    }

    static String normalizeToDateString(String raw) {
        if (raw.length() >= 10 && raw.charAt(4) == '-' && raw.charAt(7) == '-') {
            String datePart = raw.substring(0, 10);
            try {
                return LocalDate.parse(datePart, DATE).format(DATE);
            } catch (DateTimeParseException ignored) {
                // fall through
            }
        }
        try {
            return LocalDate.parse(raw, DATE).format(DATE);
        } catch (DateTimeParseException e) {
            return LocalDate.now(ZONE).plusDays(DEFAULT_DAYS_AFTER_ASSIGN).format(DATE);
        }
    }
}
