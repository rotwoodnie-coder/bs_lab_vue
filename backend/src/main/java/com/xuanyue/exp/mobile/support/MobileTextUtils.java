package com.xuanyue.exp.mobile.support;

import org.springframework.util.StringUtils;

public final class MobileTextUtils {

    private MobileTextUtils() {
    }

    public static String toPlainOneLine(String raw, int maxLen) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String plain = raw.replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (!StringUtils.hasText(plain)) {
            return null;
        }
        if (maxLen > 0 && plain.length() > maxLen) {
            return plain.substring(0, maxLen) + "…";
        }
        return plain;
    }
}
