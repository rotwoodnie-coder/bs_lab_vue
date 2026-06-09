package com.xuanyue.exp.common.storage.minio;

import com.xuanyue.exp.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/minio/files")
public class MinioStorageController {

    private final MinioStorageService minioStorageService;

    public MinioStorageController(MinioStorageService minioStorageService) {
        this.minioStorageService = minioStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> upload(@RequestPart("file") MultipartFile file) throws IOException {
        return ApiResponse.success(minioStorageService.upload(file));
    }

    @DeleteMapping("/delete")
    public ApiResponse<Void> deleteByUrl(@RequestParam("url") String url) {
        minioStorageService.deleteByUrl(url);
        return ApiResponse.success(null);
    }
}
