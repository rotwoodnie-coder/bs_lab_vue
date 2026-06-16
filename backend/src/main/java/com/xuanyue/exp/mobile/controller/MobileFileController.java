package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.mobile.support.MobileMediaUrlSupport;
import com.xuanyue.exp.mobile.support.MobileMinioKeySupport;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/files")
public class MobileFileController {

    private final MinioStorageService minioStorageService;

    public MobileFileController(MinioStorageService minioStorageService) {
        this.minioStorageService = minioStorageService;
    }

    /** 移动端文件上传（MinIO，唯一写入入口） */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> upload(@RequestPart("file") MultipartFile file) throws IOException {
        Map<String, Object> uploaded = minioStorageService.upload(file);
        Map<String, Object> data = new HashMap<>(uploaded);
        data.putIfAbsent("url", uploaded.get("fileUrl"));
        data.put("storage", "minio");
        return ApiResponse.success(data);
    }

    /**
     * MinIO 媒体预览：302 跳转到预签名 URL。
     * 当客户端无法直连 MinIO 时，可将 img/video src 指向此接口。
     */
    @GetMapping("/preview")
    public void preview(@RequestParam("url") String url, HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(url)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "url required");
            return;
        }
        String accessible = MobileMediaUrlSupport.resolve(minioStorageService, url.trim());
        if (!StringUtils.hasText(accessible)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "media not found");
            return;
        }
        response.sendRedirect(accessible);
    }

    /** 返回可访问 URL（JSON），供前端拼接或调试 */
    @GetMapping("/resolve")
    public ApiResponse<Map<String, String>> resolve(@RequestParam("url") String url) {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("url required");
        }
        String accessible = MobileMediaUrlSupport.resolve(minioStorageService, url.trim());
        Map<String, String> data = new HashMap<>();
        data.put("url", accessible);
        data.put("storage", "minio");
        return ApiResponse.success(data);
    }

    /** 按 MinIO 存储 key / URL 删除对象 */
    @DeleteMapping("/delete")
    public ApiResponse<Void> delete(@RequestParam("url") String url) {
        String storageKey = MobileMinioKeySupport.requireStorageKey(minioStorageService, url);
        minioStorageService.deleteByUrl(storageKey);
        return ApiResponse.success(null);
    }

    /** @deprecated 请使用 {@link #upload(MultipartFile)} */
    @PostMapping(value = "/minio-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> uploadToMinio(@RequestPart("file") MultipartFile file) throws IOException {
        return upload(file);
    }

    /** @deprecated 请使用 {@link #delete(String)} */
    @DeleteMapping("/minio-delete")
    public ApiResponse<Void> deleteFromMinio(@RequestParam("url") String url) {
        return delete(url);
    }
}
