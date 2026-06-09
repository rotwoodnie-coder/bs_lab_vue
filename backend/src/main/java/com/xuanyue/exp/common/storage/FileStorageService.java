package com.xuanyue.exp.common.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface FileStorageService {

    Map<String, Object> upload(MultipartFile file) throws IOException;

    boolean deleteByUrl(String fileUrl);
}
