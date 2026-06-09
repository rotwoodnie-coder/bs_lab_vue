package com.xuanyue.exp.common.storage.minio;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface MinioStorageService {

    Map<String, Object> upload(MultipartFile file) throws IOException;

    boolean deleteByUrl(String fileUrl);
    String buildPreviewUrl(String objectName);
}
