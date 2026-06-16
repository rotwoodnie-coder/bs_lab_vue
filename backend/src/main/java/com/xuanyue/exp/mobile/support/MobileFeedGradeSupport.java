package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.mobile.support.MobileFeedRankSupport.FeedRankMeta;
import com.xuanyue.exp.mobile.support.MobileFeedRankSupport.RankedFeedEntry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页 / 模拟实验列表共用的年级 Chip 解析与匹配。
 */
@Component
public class MobileFeedGradeSupport {

    private static final Map<String, List<String>> GRADE_FILTER_MAP = new LinkedHashMap<>();

    static {
        GRADE_FILTER_MAP.put("g12", Arrays.asList("g1", "g2"));
        GRADE_FILTER_MAP.put("g34", Arrays.asList("g3", "g4"));
        GRADE_FILTER_MAP.put("g56", Arrays.asList("g5", "g6"));
    }

    public List<String> resolveGradeIds(String gradeKey) {
        if (!StringUtils.hasText(gradeKey) || "all".equalsIgnoreCase(gradeKey.trim())) {
            return null;
        }
        return GRADE_FILTER_MAP.get(gradeKey.trim());
    }

    public boolean isGradeFilterActive(String gradeKey) {
        List<String> ids = resolveGradeIds(gradeKey);
        return ids != null && !ids.isEmpty();
    }

    public boolean matchesGradeIds(FeedRankMeta meta, List<String> gradeIds) {
        if (meta == null || gradeIds == null || gradeIds.isEmpty()) {
            return true;
        }
        if ("work".equals(meta.getSource())) {
            return StringUtils.hasText(meta.getSchoolGradeId())
                    && gradeIds.contains(meta.getSchoolGradeId().trim());
        }
        if ("simulator".equals(meta.getSource())) {
            if (meta.getSchoolGradeIds() != null) {
                for (String id : meta.getSchoolGradeIds()) {
                    if (StringUtils.hasText(id) && gradeIds.contains(id.trim())) {
                        return true;
                    }
                }
            }
            return false;
        }
        if (meta.getSchoolGradeIds() != null) {
            for (String id : meta.getSchoolGradeIds()) {
                if (StringUtils.hasText(id) && gradeIds.contains(id.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<RankedFeedEntry> filterEntries(List<RankedFeedEntry> entries, List<String> gradeIds) {
        if (entries == null || entries.isEmpty() || gradeIds == null || gradeIds.isEmpty()) {
            return entries != null ? entries : Collections.emptyList();
        }
        List<RankedFeedEntry> matched = new java.util.ArrayList<>(entries.size());
        for (RankedFeedEntry entry : entries) {
            if (entry != null && entry.getMeta() != null
                    && matchesGradeIds(entry.getMeta(), gradeIds)) {
                matched.add(entry);
            }
        }
        return matched;
    }
}
