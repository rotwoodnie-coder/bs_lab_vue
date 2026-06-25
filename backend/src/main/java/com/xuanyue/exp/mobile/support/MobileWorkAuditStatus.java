package com.xuanyue.exp.mobile.support;

import org.springframework.util.StringUtils;

/**
 * 学生作品审核状态（统一基于 exp_msg.status）。
 *
 * <pre>
 *   draft 草稿（进行中，未提交）
 *   t     待审核（已提交，等待老师/校管理员审核）
 *   y     通过（审核通过，可在首页/公开墙展示）
 *   n     不通过（审核驳回，仅作者可见）
 * </pre>
 *
 * 老师实验走教研员审核（同样使用 exp_msg.status 的 t→y/n），此处仅服务于学生作品口径。
 */
public final class MobileWorkAuditStatus {

    public static final String DRAFT = "draft";
    public static final String PENDING = "t";
    public static final String APPROVED = "y";
    public static final String REJECTED = "n";

    private MobileWorkAuditStatus() {
    }

    public static boolean isApproved(String status) {
        return APPROVED.equals(safe(status));
    }

    public static boolean isPending(String status) {
        return PENDING.equals(safe(status));
    }

    public static boolean isRejected(String status) {
        return REJECTED.equals(safe(status));
    }

    public static boolean isDraft(String status) {
        return DRAFT.equals(safe(status));
    }

    /** 仅作者本人可见的状态（草稿/待审核/未通过）；通过(y)对所有人可见 */
    public static boolean isAuthorOnlyVisible(String status) {
        String s = safe(status);
        return DRAFT.equals(s) || PENDING.equals(s) || REJECTED.equals(s);
    }

    /**
     * 教师评分结果映射到审核结论：
     * 不合格(fail) → 驳回(n)；其余合格成绩(excellent/good/pass) → 通过(y)。
     */
    public static String fromRating(String rating) {
        String r = safe(rating).toLowerCase();
        return "fail".equals(r) ? REJECTED : APPROVED;
    }

    /** DTO reviewStatus 代码：与前端徽标判断一致（reviewed/pending/rejected/draft） */
    public static String reviewStatusCode(String status) {
        String s = safe(status);
        if (APPROVED.equals(s)) return "reviewed";
        if (REJECTED.equals(s)) return "rejected";
        if (DRAFT.equals(s)) return "draft";
        return "pending";
    }

    /** 展示文案 */
    public static String reviewStatusLabel(String status) {
        String s = safe(status);
        if (APPROVED.equals(s)) return "已通过";
        if (REJECTED.equals(s)) return "未通过";
        if (DRAFT.equals(s)) return "草稿";
        return "审核中";
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
