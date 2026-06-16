package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.system.entity.SysUser;
import org.springframework.util.StringUtils;

public final class MobileAvatarSupport {

    private MobileAvatarSupport() {
    }

    public static String resolveUserAvatarUrl(MinioStorageService minio, SysUser user) {
        if (user == null) {
            return null;
        }
        return resolveUserAvatarUrl(minio, user.getUserLogo());
    }

    public static String resolveUserAvatarUrl(MinioStorageService minio, String userLogo) {
        if (!StringUtils.hasText(userLogo)) {
            return null;
        }
        try {
            return MobileMediaUrlSupport.resolve(minio, userLogo);
        } catch (Exception e) {
            return userLogo;
        }
    }

    public static String initialOf(String name, String fallback) {
        if (!StringUtils.hasText(name)) {
            return fallback;
        }
        return name.trim().substring(0, 1);
    }

    public static String gradClassForRole(String roleTag) {
        if (!StringUtils.hasText(roleTag)) {
            return "avatar-grad-warm";
        }
        String role = roleTag.trim().toLowerCase();
        if (role.contains("teacher")) {
            return "avatar-grad-ocean";
        }
        if (role.contains("parent")) {
            return "avatar-grad-cool";
        }
        return "avatar-grad-warm";
    }
}
