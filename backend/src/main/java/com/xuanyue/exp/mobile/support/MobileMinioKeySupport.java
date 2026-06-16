package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import org.springframework.util.StringUtils;

/**
 * 移动端写入侧 MinIO 存储 key 规范化与校验。
 * <p>
 * 原则：新上传/新保存一律存 MinIO object key（{@code /yyyyMMdd/uuid.ext}），
 * 拒绝 legacy Tomcat {@code /uploads/} 与本机地址。
 */
public final class MobileMinioKeySupport {

    private MobileMinioKeySupport() {
    }

    /**
     * 将客户端传入的 URL / key 规范化为存库格式：{@code /objectKey}。
     */
    public static String normalizeForStorage(MinioStorageService minio, String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("blob:")) {
            throw new IllegalArgumentException("文件尚未上传至 MinIO");
        }
        if (MobileMediaUrlSupport.isLegacyLocalMediaUrl(trimmed)) {
            throw new IllegalArgumentException("不支持旧版本地文件地址，请重新上传");
        }

        String objectKey;
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            objectKey = minio.normalizeStorageKey(trimmed);
        } else {
            objectKey = trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
            while (objectKey.startsWith("/")) {
                objectKey = objectKey.substring(1);
            }
        }

        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("无效的 MinIO 文件地址");
        }
        if (objectKey.startsWith("http://") || objectKey.startsWith("https://")) {
            throw new IllegalArgumentException("无法解析 MinIO 对象 key");
        }
        return "/" + objectKey;
    }

    /**
     * 校验并规范化；用于作品附件、头像等写入。
     */
    public static String requireStorageKey(MinioStorageService minio, String raw) {
        String key = normalizeForStorage(minio, raw);
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("文件地址不能为空");
        }
        return key;
    }

    /** 是否可作为 MinIO 读路径（含历史 legacy，由 {@link MobileMediaUrlSupport} 桥接） */
    public static boolean isReadableStorageReference(String raw) {
        return StringUtils.hasText(raw) && !raw.trim().startsWith("blob:");
    }
}
