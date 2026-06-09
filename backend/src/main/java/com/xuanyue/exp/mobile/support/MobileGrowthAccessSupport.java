package com.xuanyue.exp.mobile.support;

import org.springframework.util.StringUtils;

/**
 * 成长档案可见性：展示方案 visibility 在 API 层强制。
 */
public final class MobileGrowthAccessSupport {

    private MobileGrowthAccessSupport() {
    }

    public static boolean canParentView(String visibilityKey) {
        if (!StringUtils.hasText(visibilityKey)) {
            return true;
        }
        String key = visibilityKey.trim().toLowerCase();
        return "parent".equals(key) || "class".equals(key);
    }

    public static boolean isSelfOnly(String visibilityKey) {
        return StringUtils.hasText(visibilityKey) && "self".equalsIgnoreCase(visibilityKey.trim());
    }
}
