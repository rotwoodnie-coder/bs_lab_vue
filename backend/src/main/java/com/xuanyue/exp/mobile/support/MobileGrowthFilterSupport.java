package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.mobile.entity.MbGrowthEvent;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 成长档案 · 展示方案过滤（时间范围 + 内容类型）
 */
public final class MobileGrowthFilterSupport {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private MobileGrowthFilterSupport() {
    }

    public static Date resolveRangeStartDate(String rangeKey) {
        LocalDate start = resolveRangeStart(rangeKey, LocalDate.now(ZONE));
        if (start == null) {
            return null;
        }
        return Date.from(start.atStartOfDay(ZONE).toInstant());
    }

    public static LocalDate resolveRangeStart(String rangeKey, LocalDate today) {
        if (today == null) {
            today = LocalDate.now(ZONE);
        }
        String key = StringUtils.hasText(rangeKey) ? rangeKey.trim().toLowerCase() : "term";
        if ("all".equals(key)) {
            return null;
        }
        if ("year".equals(key)) {
            int y = today.getYear();
            if (today.getMonthValue() >= 9) {
                return LocalDate.of(y, 9, 1);
            }
            return LocalDate.of(y - 1, 9, 1);
        }
        // term：9–1 月属秋季学期；2–8 月属春季学期
        int month = today.getMonthValue();
        int year = today.getYear();
        if (month >= 9) {
            return LocalDate.of(year, 9, 1);
        }
        if (month >= 2) {
            return LocalDate.of(year, 2, 1);
        }
        return LocalDate.of(year - 1, 9, 1);
    }

    public static boolean isAfterRangeStart(MbGrowthEvent event, LocalDate rangeStart) {
        if (rangeStart == null || event == null || event.getSortTime() == null) {
            return true;
        }
        LocalDate eventDay = event.getSortTime().toInstant().atZone(ZONE).toLocalDate();
        return !eventDay.isBefore(rangeStart);
    }

    public static Set<String> resolveAllowedSourceTypes(List<String> contentKeys) {
        if (contentKeys == null || contentKeys.isEmpty()) {
            return new HashSet<String>(Arrays.asList("task", "work", "badge", "quiz"));
        }
        Set<String> allowed = new HashSet<String>();
        for (String key : contentKeys) {
            if (!StringUtils.hasText(key)) {
                continue;
            }
            switch (key.trim().toLowerCase()) {
                case "exp":
                    allowed.add("task");
                    break;
                case "work":
                    allowed.add("work");
                    break;
                case "badge":
                    allowed.add("badge");
                    break;
                case "quiz":
                    allowed.add("quiz");
                    break;
                case "rank":
                    allowed.add("rank");
                    break;
                default:
                    break;
            }
        }
        return allowed;
    }

    public static boolean matchesContentFilter(MbGrowthEvent event, Set<String> allowedSourceTypes) {
        if (allowedSourceTypes == null || allowedSourceTypes.isEmpty()) {
            return false;
        }
        if (event == null) {
            return false;
        }
        if (!StringUtils.hasText(event.getSourceType())) {
            // 兼容旧 demo 数据（无 source_type）
            return true;
        }
        return allowedSourceTypes.contains(event.getSourceType().trim().toLowerCase());
    }

    public static List<MbGrowthEvent> filterEvents(List<MbGrowthEvent> events,
                                                   List<String> contentKeys,
                                                   String rangeKey) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate rangeStart = resolveRangeStart(rangeKey, LocalDate.now(ZONE));
        Set<String> allowed = resolveAllowedSourceTypes(contentKeys);
        return events.stream()
                .filter(e -> isAfterRangeStart(e, rangeStart))
                .filter(e -> matchesContentFilter(e, allowed))
                .collect(Collectors.toList());
    }
}
