package com.xuanyue.exp.common.storage.minio;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import io.minio.GetPresignedObjectUrlArgs;
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
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        if (file.getSize() > properties.getMaxFileSize()) {
            throw new RuntimeException("文件不能超过" + properties.getMaxFileSize() + "字节");
        }
        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = getFileExtension(originalName);
        String dateFolder = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String objectName = buildObjectName(dateFolder, UUID.randomUUID().toString().replace("-", ""), ext);
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    io.minio.PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new RuntimeException("MinIO上传失败", e);
        }
        System.out.println("objectName: " + objectName);
        String fileUrl = buildFileUrl(objectName);
        String previewUrl = buildPreviewUrl(objectName);
        System.out.println("objectName: " + objectName);
        System.out.println("fileUrl: " + fileUrl);
        System.out.println("previewUrl: " + previewUrl);
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
        String prefix = "/";//normalizeUrlPrefix(properties.getUrlPrefix());
        if (!StringUtils.hasText(prefix)) {
            return objectName;
        }
        if (prefix.endsWith("/")) {
            return prefix + objectName;
        }
        return prefix + "/" + objectName;
    }

    @Override
    public String buildPreviewUrl(String objectName) {
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
            return buildFileUrl(objectName);
        }
    }

    private String resolveObjectName(String fileUrl) {
        String prefix = normalizeUrlPrefix(properties.getUrlPrefix());
        String result = fileUrl.trim();
        if (StringUtils.hasText(prefix) && result.startsWith(prefix)) {
            result = result.substring(prefix.length());
        }
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
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


    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName == null ? -1 : fileName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex >= fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
