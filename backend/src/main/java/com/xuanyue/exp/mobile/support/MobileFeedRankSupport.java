package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.mobile.dto.HomeFeedItem;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页瀑布流：三类 eligible 资源全部参与展示；年级 Chip 为过滤条件。
 */
public final class MobileFeedRankSupport {

    private static final double W_GRADE = 0.40;
    private static final double W_SCOPE = 0.25;
    private static final double W_RECENCY = 0.20;
    private static final double W_QUALITY = 0.10;
    private static final double W_FEATURED = 0.05;
    /** 首页年级 Chip 选中时，匹配年级的额外加分（非过滤） */
    private static final double W_GRADE_CHIP = 0.12;
    private static final double RECENCY_HALF_LIFE_DAYS = 14.0;

    private static final Map<String, String> GRADE_TO_BAND = new HashMap<>();

    static {
        GRADE_TO_BAND.put("g1", "g12");
        GRADE_TO_BAND.put("g2", "g12");
        GRADE_TO_BAND.put("g3", "g34");
        GRADE_TO_BAND.put("g4", "g34");
        GRADE_TO_BAND.put("g5", "g56");
        GRADE_TO_BAND.put("g6", "g56");
    }

    private MobileFeedRankSupport() {
    }

    /**
     * 对全部候选条目排序，返回与输入等长的列表（仅顺序不同）。
     *
     * @param gradeChipIds 年级 Chip 对应 g1~g6 列表；null 表示「全部」不加 Chip 加分
     */
    public static List<HomeFeedItem> sort(List<RankedFeedEntry> entries,
                                          MobileFeedViewerProfile viewer,
                                          List<String> gradeChipIds) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<ScoredEntry> scored = new ArrayList<>(entries.size());
        for (RankedFeedEntry entry : entries) {
            if (entry == null || entry.getItem() == null) {
                continue;
            }
            FeedRankMeta meta = entry.getMeta() != null ? entry.getMeta() : new FeedRankMeta();
            double score = scoreEntry(meta, viewer);
            score += gradeChipBonus(meta, gradeChipIds);
            scored.add(new ScoredEntry(entry.getItem(), score, meta.getPublishTime()));
        }
        scored.sort(Comparator
                .comparingDouble(ScoredEntry::getScore).reversed()
                .thenComparing(ScoredEntry::getPublishTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(e -> e.getItem().getId(), Comparator.nullsLast(String::compareTo)));
        List<HomeFeedItem> result = new ArrayList<>(scored.size());
        for (ScoredEntry s : scored) {
            result.add(s.getItem());
        }
        return result;
    }

    /** @deprecated 使用 {@link #sort} */
    @Deprecated
    public static List<HomeFeedItem> rank(List<RankedFeedEntry> entries,
                                          MobileFeedViewerProfile viewer,
                                          List<String> gradeChipIds) {
        return sort(entries, viewer, gradeChipIds);
    }

    private static double gradeChipBonus(FeedRankMeta meta, List<String> gradeChipIds) {
        if (gradeChipIds == null || gradeChipIds.isEmpty()) {
            return 0;
        }
        if ("work".equals(meta.getSource())) {
            if (StringUtils.hasText(meta.getSchoolGradeId())
                    && gradeChipIds.contains(meta.getSchoolGradeId().trim())) {
                return W_GRADE_CHIP;
            }
            return 0;
        }
        if ("simulator".equals(meta.getSource())) {
            return 0;
        }
        if (meta.getSchoolGradeIds() != null) {
            for (String id : meta.getSchoolGradeIds()) {
                if (StringUtils.hasText(id) && gradeChipIds.contains(id.trim())) {
                    return W_GRADE_CHIP;
                }
            }
        }
        return 0;
    }

    private static double scoreEntry(FeedRankMeta meta, MobileFeedViewerProfile viewer) {
        double gradeScore = gradeScore(meta, viewer);
        double scopeScore = scopeScore(meta, viewer);
        double recencyScore = recencyScore(meta.getPublishTime());
        double qualityScore = qualityScore(meta);
        double featuredBoost = meta.isFeatured() ? 1.0 : 0.0;
        return W_GRADE * gradeScore
                + W_SCOPE * scopeScore
                + W_RECENCY * recencyScore
                + W_QUALITY * qualityScore
                + W_FEATURED * featuredBoost;
    }

    private static double gradeScore(FeedRankMeta meta, MobileFeedViewerProfile viewer) {
        String viewerGrade = viewer.getSchoolGradeId();
        if (!StringUtils.hasText(viewerGrade)) {
            return "simulator".equals(meta.getSource()) ? 0.5 : 0.35;
        }
        String vg = viewerGrade.trim();
        if ("simulator".equals(meta.getSource())) {
            return 0.5;
        }
        if ("work".equals(meta.getSource())) {
            return gradeMatchScore(vg, meta.getSchoolGradeId());
        }
        if (meta.getSchoolGradeIds() == null || meta.getSchoolGradeIds().isEmpty()) {
            return 0.2;
        }
        double best = 0.2;
        for (String gid : meta.getSchoolGradeIds()) {
            best = Math.max(best, gradeMatchScore(vg, gid));
        }
        return best;
    }

    private static double gradeMatchScore(String viewerGrade, String contentGrade) {
        if (!StringUtils.hasText(contentGrade)) {
            return 0.2;
        }
        String cg = contentGrade.trim();
        if (viewerGrade.equals(cg)) {
            return 1.0;
        }
        if (sameBand(viewerGrade, cg)) {
            return 0.85;
        }
        if (isAdjacentGrade(viewerGrade, cg)) {
            return 0.6;
        }
        return 0.2;
    }

    private static boolean sameBand(String a, String b) {
        String bandA = GRADE_TO_BAND.get(a);
        String bandB = GRADE_TO_BAND.get(b);
        return bandA != null && bandA.equals(bandB);
    }

    private static boolean isAdjacentGrade(String a, String b) {
        int la = gradeLevel(a);
        int lb = gradeLevel(b);
        if (la <= 0 || lb <= 0) {
            return false;
        }
        return Math.abs(la - lb) == 1;
    }

    private static int gradeLevel(String gradeId) {
        if (!StringUtils.hasText(gradeId) || gradeId.length() < 2) {
            return -1;
        }
        try {
            return Integer.parseInt(gradeId.substring(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double scopeScore(FeedRankMeta meta, MobileFeedViewerProfile viewer) {
        if (!viewer.isLoggedIn()) {
            return 0.35;
        }
        if (viewer.isTeacher()) {
            if ("work".equals(meta.getSource())
                    && StringUtils.hasText(meta.getClassOrgId())
                    && viewer.getTeacherClassOrgIds().contains(meta.getClassOrgId().trim())) {
                return 1.0;
            }
            if (StringUtils.hasText(meta.getRootOrgId())
                    && meta.getRootOrgId().equals(viewer.getRootOrgId())) {
                return 0.7;
            }
            return 0.3;
        }
        if ("work".equals(meta.getSource()) && StringUtils.hasText(meta.getClassOrgId())
                && meta.getClassOrgId().equals(viewer.getClassOrgId())) {
            return 1.0;
        }
        if (StringUtils.hasText(meta.getRootOrgId())
                && meta.getRootOrgId().equals(viewer.getRootOrgId())) {
            return 0.5;
        }
        return 0.25;
    }

    private static double recencyScore(Date publishTime) {
        if (publishTime == null) {
            return 0.25;
        }
        long ms = System.currentTimeMillis() - publishTime.getTime();
        double days = Math.max(0, ms / 86_400_000.0);
        return Math.exp(-days / RECENCY_HALF_LIFE_DAYS);
    }

    private static double qualityScore(FeedRankMeta meta) {
        int signal = Math.max(0, meta.getEngagementScore());
        if (signal <= 0) {
            return 0.35;
        }
        return Math.min(1.0, Math.log1p(signal) / Math.log1p(50));
    }

    public static final class FeedRankMeta {
        private String source;
        private String schoolGradeId;
        private List<String> schoolGradeIds = Collections.emptyList();
        private String classOrgId;
        private String rootOrgId;
        private Date publishTime;
        private boolean featured;
        private int engagementScore;

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getSchoolGradeId() { return schoolGradeId; }
        public void setSchoolGradeId(String schoolGradeId) { this.schoolGradeId = schoolGradeId; }
        public List<String> getSchoolGradeIds() { return schoolGradeIds; }
        public void setSchoolGradeIds(List<String> schoolGradeIds) {
            this.schoolGradeIds = schoolGradeIds != null ? schoolGradeIds : Collections.emptyList();
        }
        public String getClassOrgId() { return classOrgId; }
        public void setClassOrgId(String classOrgId) { this.classOrgId = classOrgId; }
        public String getRootOrgId() { return rootOrgId; }
        public void setRootOrgId(String rootOrgId) { this.rootOrgId = rootOrgId; }
        public Date getPublishTime() { return publishTime; }
        public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
        public boolean isFeatured() { return featured; }
        public void setFeatured(boolean featured) { this.featured = featured; }
        public int getEngagementScore() { return engagementScore; }
        public void setEngagementScore(int engagementScore) { this.engagementScore = engagementScore; }
    }

    public static final class RankedFeedEntry {
        private final HomeFeedItem item;
        private final FeedRankMeta meta;

        public RankedFeedEntry(HomeFeedItem item, FeedRankMeta meta) {
            this.item = item;
            this.meta = meta;
        }

        public HomeFeedItem getItem() { return item; }
        public FeedRankMeta getMeta() { return meta; }
    }

    private static final class ScoredEntry {
        private final HomeFeedItem item;
        private final double score;
        private final Date publishTime;

        private ScoredEntry(HomeFeedItem item, double score, Date publishTime) {
            this.item = item;
            this.score = score;
            this.publishTime = publishTime;
        }

        public HomeFeedItem getItem() { return item; }
        public double getScore() { return score; }
        public Date getPublishTime() { return publishTime; }
    }
}
