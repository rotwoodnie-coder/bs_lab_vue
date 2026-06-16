package com.xuanyue.exp.mobile.support;



import com.xuanyue.exp.common.storage.minio.MinioStorageService;

import org.springframework.util.StringUtils;



import java.util.regex.Matcher;

import java.util.regex.Pattern;



/**

 * 移动端媒体 URL 统一解析（与管理端 previewUrl + fileUrl 语义一致，逻辑独立维护）。

 */

public final class MobileMediaUrlSupport {



    private static final Pattern HTML_SRC_ATTR = Pattern.compile(

            "(src\\s*=\\s*[\"'])(?!https?://)([^\"']+)([\"'])",

            Pattern.CASE_INSENSITIVE);



    private static final Pattern PRESIGNED_QUERY = Pattern.compile("X-Amz-", Pattern.CASE_INSENSITIVE);



    private MobileMediaUrlSupport() {

    }



    public static String resolve(MinioStorageService minio, String fileUrl) {

        if (!StringUtils.hasText(fileUrl)) {

            return null;

        }

        String trimmed = fileUrl.trim();



        if (isLegacyLocalMediaUrl(trimmed)) {

            for (String objectKey : legacyObjectKeyCandidates(minio, trimmed)) {

                String presigned = tryPresign(minio, objectKey);

                if (StringUtils.hasText(presigned)) {

                    return presigned;

                }

            }

            return null;

        }



        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {

            if (PRESIGNED_QUERY.matcher(trimmed).find()) {

                return trimmed;

            }

            String objectKey = safeNormalizeKey(minio, trimmed);

            if (StringUtils.hasText(objectKey)) {

                String presigned = tryPresign(minio, objectKey);

                if (StringUtils.hasText(presigned)) {

                    return presigned;

                }

            }

            // 非 presigned http URL（含 legacy 地址）不可达 → 返回 null

            if (isLegacyLocalMediaUrl(trimmed)) {

                return null;

            }

            return trimmed;

        }



        String presigned = tryPresign(minio, trimmed);

        if (StringUtils.hasText(presigned)) {

            return presigned;

        }

        try {

            String accessible = minio.resolveAccessibleUrl(trimmed);

            if (StringUtils.hasText(accessible)

                    && (accessible.startsWith("http://") || accessible.startsWith("https://"))) {

                return accessible;

            }

            return null;

        } catch (Exception e) {

            return null;

        }

    }



    /** 旧版本地/Tomcat 上传地址，手机端不可达 */

    public static boolean isLegacyLocalMediaUrl(String url) {

        if (!StringUtils.hasText(url)) {

            return false;

        }

        String lower = url.trim().toLowerCase();

        return lower.contains("127.0.0.1")

                || lower.contains("localhost")

                || lower.contains("/uploads/");

    }



    /** 多个候选 URL 中优先选用 MinIO 可解析地址，跳过 legacy 本地上传 */

    public static String pickBestMediaUrl(String... candidates) {

        String legacyFallback = null;

        if (candidates == null) {

            return null;

        }

        for (String candidate : candidates) {

            if (!StringUtils.hasText(candidate)) {

                continue;

            }

            String value = candidate.trim();

            if (isLegacyLocalMediaUrl(value)) {

                if (legacyFallback == null) {

                    legacyFallback = value;

                }

                continue;

            }

            return value;

        }

        return legacyFallback;

    }



    /** 富文本内嵌图片/资源：将非 http(s) 的 src 替换为可访问 URL */

    public static String enrichRichText(MinioStorageService minio, String html) {

        if (!StringUtils.hasText(html)) {

            return html;

        }

        Matcher matcher = HTML_SRC_ATTR.matcher(html);

        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {

            String resolved = resolve(minio, matcher.group(2));

            matcher.appendReplacement(buffer,

                    Matcher.quoteReplacement(matcher.group(1) + resolved + matcher.group(3)));

        }

        matcher.appendTail(buffer);

        return buffer.toString();

    }



    private static String tryPresign(MinioStorageService minio, String fileUrlOrKey) {

        try {

            String presigned = minio.buildPreviewUrl(fileUrlOrKey);

            if (StringUtils.hasText(presigned)

                    && (presigned.startsWith("http://") || presigned.startsWith("https://"))) {

                return presigned;

            }

        } catch (Exception ignored) {

            // fall through

        }

        return null;

    }



    private static String safeNormalizeKey(MinioStorageService minio, String fileUrl) {

        try {

            return minio.normalizeStorageKey(fileUrl);

        } catch (Exception e) {

            return null;

        }

    }



    /** legacy Tomcat /uploads/ 路径 → MinIO object key 候选（迁移后常保留相对路径） */

    private static String[] legacyObjectKeyCandidates(MinioStorageService minio, String legacyUrl) {

        java.util.LinkedHashSet<String> keys = new java.util.LinkedHashSet<>();

        String normalized = safeNormalizeKey(minio, legacyUrl);

        if (StringUtils.hasText(normalized)) {

            keys.add(normalized);

        }

        String uploadRelative = extractLegacyUploadRelativePath(legacyUrl);

        if (StringUtils.hasText(uploadRelative)) {

            keys.add(uploadRelative);

            keys.add("uploads/" + uploadRelative);

        }

        return keys.toArray(new String[0]);

    }



    private static String extractLegacyUploadRelativePath(String url) {

        if (!StringUtils.hasText(url)) {

            return null;

        }

        String lower = url.trim().toLowerCase();

        int idx = lower.indexOf("/uploads/");

        if (idx < 0) {

            return null;

        }

        String path = url.trim().substring(idx + "/uploads/".length());

        int query = path.indexOf('?');

        if (query >= 0) {

            path = path.substring(0, query);

        }

        int hash = path.indexOf('#');

        if (hash >= 0) {

            path = path.substring(0, hash);

        }

        while (path.startsWith("/")) {

            path = path.substring(1);

        }

        return StringUtils.hasText(path) ? path : null;

    }

}


