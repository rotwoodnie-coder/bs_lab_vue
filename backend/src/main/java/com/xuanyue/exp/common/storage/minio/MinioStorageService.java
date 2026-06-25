package com.xuanyue.exp.common.storage.minio;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface MinioStorageService {

    Map<String, Object> upload(MultipartFile file) throws IOException;

    Map<String, Object> upload(MultipartFile file, String originalFilename) throws IOException;

    boolean deleteByUrl(String fileUrl);
    String buildPreviewUrl(String fileUrlOrObjectName);
    String resolveAccessibleUrl(String fileUrl);
    String normalizeStorageKey(String fileUrl);

    /** 获取 MinIO 对象的输入流（用于后端代理流式传输） */
    InputStream getObjectStream(String fileUrl) throws Exception;

    String getBucket();

    /** 探测 MinIO 是否可达（上传前诊断用） */
    Map<String, Object> checkHealth();
}
