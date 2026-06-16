package com.xuanyue.exp.mobile.support;

import org.springframework.util.StringUtils;

/**
 * 作品审核状态展示与筛选（draft / pending / approved 等）。
 */
public final class MobileWorkReviewSupport {

    private MobileWorkReviewSupport() {
    }

    public static boolean isDraft(String recordStatus, String reviewStatus) {
        return "d".equals(recordStatus) || "draft".equalsIgnoreCase(safe(reviewStatus));
    }

    public static boolean isPubliclyVisible(String recordStatus, String reviewStatus) {
        if (!"y".equals(recordStatus)) {
            return false;
        }
        String rs = safe(reviewStatus);
        return "reviewed".equals(rs) || "approved".equals(rs);
    }

    public static boolean matchesReviewFilter(String recordStatus, String reviewStatus, String filter) {
        if (!StringUtils.hasText(filter) || "all".equalsIgnoreCase(filter.trim())) {
            return true;
        }
        String key = filter.trim().toLowerCase();
        if ("draft".equals(key)) {
            return isDraft(recordStatus, reviewStatus);
        }
        if ("pending".equals(key)) {
            return "y".equals(recordStatus) && "pending".equalsIgnoreCase(safe(reviewStatus));
        }
        if ("approved".equals(key)) {
            return isPubliclyVisible(recordStatus, reviewStatus);
        }
        if ("rejected".equals(key)) {
            return "rejected".equalsIgnoreCase(safe(reviewStatus));
        }
        return safe(reviewStatus).equalsIgnoreCase(key);
    }

    public static String resolveDisplayStatus(String recordStatus, String reviewStatus) {
        if (isDraft(recordStatus, reviewStatus)) {
            return "draft";
        }
        String rs = safe(reviewStatus);
        if ("reviewed".equals(rs) || "approved".equals(rs)) {
            return "approved";
        }
        if ("rejected".equals(rs)) {
            return "rejected";
        }
        if ("pending".equals(rs)) {
            return "pending";
        }
        return "pending";
    }

    public static String resolveLabel(String recordStatus, String reviewStatus) {
        switch (resolveDisplayStatus(recordStatus, reviewStatus)) {
            case "draft":
                return "草稿";
            case "approved":
                return "已通过";
            case "rejected":
                return "未通过";
            case "pending":
            default:
                return "待审核";
        }
    }

    public static String resolveBadgeClass(String recordStatus, String reviewStatus) {
        switch (resolveDisplayStatus(recordStatus, reviewStatus)) {
            case "draft":
                return "badge-slate";
            case "approved":
                return "badge-success";
            case "rejected":
                return "badge-danger";
            case "pending":
            default:
                return "badge-warning";
        }
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
