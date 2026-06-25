package com.xuanyue.exp.common.storage.minio;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.GetObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MinioStorageServiceImpl implements MinioStorageService, InitializingBean {

    private final MinioStorageProperties properties;
    private final io.minio.MinioClient minioClient;

    public MinioStorageServiceImpl(MinioStorageProperties properties) {
        this.properties = properties;
        this.minioClient = io.minio.MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ensureBucketExists();
    }

    @Override
    public Map<String, Object> upload(MultipartFile file) throws IOException {
        return upload(file, null);
    }

    @Override
    public Map<String, Object> upload(MultipartFile file, String originalFilename) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        if (file.getSize() > properties.getMaxFileSize()) {
            throw new RuntimeException("文件不能超过" + properties.getMaxFileSize() + "字节");
        }
        String originalName = resolveOriginalName(file, originalFilename);
        String ext = getFileExtension(originalName);
        String dateFolder = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String objectName = buildObjectName(dateFolder, UUID.randomUUID().toString().replace("-", ""), ext);
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    io.minio.PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(resolveContentType(file.getContentType(), originalName))
                            .build()
            );
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new RuntimeException("MinIO上传失败，请检查对象存储连接与配置：" + rootMessage(e), e);
        }
        String fileUrl = buildFileUrl(objectName);
        System.out.println("minio upload fileUrl: " + fileUrl);
        String previewUrl = buildPreviewUrl(objectName);
        System.out.println("minio upload previewUrl: " + previewUrl);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("fileUrl", fileUrl);
        result.put("previewUrl", previewUrl);
        result.put("fileName", originalName);
        result.put("objectName", objectName);
        return result;
    }

    @Override
    public boolean deleteByUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return false;
        }
        String objectName = resolveObjectName(fileUrl);
        try {
            minioClient.removeObject(io.minio.RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(io.minio.BucketExistsArgs.builder()
                .bucket(properties.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(io.minio.MakeBucketArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
        }
    }

    private String buildObjectName(String dateFolder, String fileKey, String ext) {
        

            String prefix = "";
            StringBuilder builder = new StringBuilder();
            if (StringUtils.hasText(prefix)) {
                builder.append(prefix).append('/');
            }
            builder.append(dateFolder).append('/').append(fileKey);
            if (StringUtils.hasText(ext)) {
                builder.append('.').append(ext);
            }
            return builder.toString();
    }

    
    private String buildFileUrl(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            return objectName;
        }
        return objectName.startsWith("/") ? objectName : "/" + objectName;
    }

    @Override
    public String buildPreviewUrl(String fileUrlOrObjectName) {
        if (!StringUtils.hasText(fileUrlOrObjectName)) {
            return fileUrlOrObjectName;
        }
        String trimmed = fileUrlOrObjectName.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        String objectName = resolveObjectName(trimmed);
        if (!StringUtils.hasText(objectName)) {
            return trimmed;
        }
        System.out.println("minio upload buildPreviewUrl: " + objectName);
        /*
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .expiry(7, java.util.concurrent.TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            return joinPublicUrl(normalizeUrlPrefix(properties.getUrlPrefix()), objectName);
        }*/
        String previewUrl = joinPublicUrl(normalizeUrlPrefix(properties.getUrlPrefix()), objectName);
        System.out.println("minio upload buildPreviewUrl: " + previewUrl);
        return previewUrl;
    }

    @Override
    public String resolveAccessibleUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return fileUrl;
        }
        String trimmed = fileUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        String objectName = resolveObjectName(trimmed);
        if (objectExists(objectName)) {
            String presigned = buildPreviewUrl(objectName);
            if (StringUtils.hasText(presigned)
                    && (presigned.startsWith("http://") || presigned.startsWith("https://"))) {
                return presigned;
            }
        }
        return joinPublicUrl(normalizeUrlPrefix(properties.getUrlPrefix()), objectName);
    }

    private boolean objectExists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String joinPublicUrl(String prefix, String path) {
        if (!StringUtils.hasText(path)) {
            return path;
        }
        if (!StringUtils.hasText(prefix)) {
            return path;
        }
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return prefix + normalizedPath;
    }

    @Override
    public String getBucket() {
        return properties.getBucket();
    }

    @Override
    public Map<String, Object> checkHealth() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("endpoint", properties.getEndpoint());
        result.put("bucket", properties.getBucket());
        try {
            boolean exists = minioClient.bucketExists(io.minio.BucketExistsArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
            result.put("reachable", true);
            result.put("bucketExists", exists);
            result.put("ok", exists);
            if (!exists) {
                result.put("message", "MinIO 可连接，但 bucket 不存在: " + properties.getBucket());
            }
        } catch (Exception e) {
            result.put("reachable", false);
            result.put("bucketExists", false);
            result.put("ok", false);
            result.put("message", rootMessage(e));
        }
        return result;
    }

    @Override
    public InputStream getObjectStream(String fileUrl) throws Exception {
        if (!StringUtils.hasText(fileUrl)) {
            return null;
        }
        String objectName = resolveObjectName(fileUrl);
        return minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(objectName)
                        .build()
        );
    }

    @Override
    public String normalizeStorageKey(String fileUrl) {
        return resolveObjectName(fileUrl);
    }

    private String resolveObjectName(String fileUrl) {
        String prefix = normalizeUrlPrefix(properties.getUrlPrefix());
        String result = fileUrl.trim();
        if (StringUtils.hasText(prefix) && result.startsWith(prefix)) {
            result = result.substring(prefix.length());
        }
        String relativePrefix = extractRelativePathFromPrefix(prefix);
        if (StringUtils.hasText(relativePrefix) && result.startsWith(relativePrefix)) {
            result = result.substring(relativePrefix.length());
        }
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    private String extractRelativePathFromPrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        String value = prefix.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            try {
                java.net.URI uri = java.net.URI.create(value);
                String path = uri.getPath();
                if (StringUtils.hasText(path)) {
                    return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                }
            } catch (Exception ignored) {
                // fall through
            }
        }
        return value.startsWith("/") ? value : "/" + value;
    }

    private String normalizeUrlPrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        String value = prefix.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }


    private String resolveOriginalName(MultipartFile file, String originalFilename) {
        if (StringUtils.hasText(originalFilename)) {
            return originalFilename.trim();
        }
        return file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName == null ? -1 : fileName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex >= fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    private String resolveContentType(String rawContentType, String fileName) {
        if (StringUtils.hasText(rawContentType)) {
            return rawContentType.trim();
        }
        String ext = getFileExtension(fileName == null ? "" : fileName).toLowerCase();
        if ("html".equals(ext) || "htm".equals(ext) || "sim".equals(ext)) {
            return "text/html";
        }
        if ("css".equals(ext)) {
            return "text/css";
        }
        if ("js".equals(ext)) {
            return "application/javascript";
        }
        return "application/octet-stream";
    }

    private String rootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() == null ? ex.getMessage() : root.getMessage();
    }
}
