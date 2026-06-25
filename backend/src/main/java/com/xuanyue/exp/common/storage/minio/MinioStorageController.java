package com.xuanyue.exp.common.storage.minio;

import com.xuanyue.exp.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
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
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/minio/files")
public class MinioStorageController {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageController.class);

    private final MinioStorageService minioStorageService;

    public MinioStorageController(MinioStorageService minioStorageService) {
        this.minioStorageService = minioStorageService;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(minioStorageService.checkHealth());
    }

    /**
     * 管理端媒体预览（含 HTML 模拟器）：从 MinIO 流式输出，不依赖 Nginx /media/ 反代。
     */
    @GetMapping("/preview")
    public void preview(@RequestParam("url") String url, HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(url)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "url required");
            return;
        }
        String trimmed = url.trim();
        try (InputStream in = minioStorageService.getObjectStream(trimmed)) {
            if (in == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "media not found");
                return;
            }
            response.setContentType(guessContentType(trimmed));
            response.setHeader("Cache-Control", "private, max-age=3600");
            StreamUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception ex) {
            String accessible = minioStorageService.resolveAccessibleUrl(trimmed);
            if (!StringUtils.hasText(accessible)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "media not found");
                return;
            }
            response.sendRedirect(accessible);
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> upload(@RequestPart("file") MultipartFile file,
                                                   @RequestParam(value = "originalFilename", required = false) String originalFilename) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail(400, "请选择要上传的文件");
        }
        String displayName = StringUtils.hasText(originalFilename)
                ? originalFilename.trim()
                : (file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        log.info("MinIO upload start: name={}, size={} bytes, contentType={}",
                displayName, file.getSize(), file.getContentType());
        try {
            Map<String, Object> result = minioStorageService.upload(file, originalFilename);
            log.info("MinIO upload ok: name={}, object={}", displayName, result.get("objectName"));
            return ApiResponse.success(result);
        } catch (IOException ex) {
            log.error("MinIO upload IO failed: name={}, size={}", displayName, file.getSize(), ex);
            throw new RuntimeException("文件上传失败：" + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            log.error("MinIO upload failed: name={}, size={}", displayName, file.getSize(), ex);
            throw ex;
        }
    }

    @DeleteMapping("/delete")
    public ApiResponse<Void> deleteByUrl(@RequestParam("url") String url) {
        minioStorageService.deleteByUrl(url);
        return ApiResponse.success(null);
    }

    private static String guessContentType(String url) {
        String lower = url.toLowerCase();
        if (lower.contains(".html") || lower.contains(".htm") || lower.contains(".sim")) {
            return "text/html;charset=UTF-8";
        }
        if (lower.contains(".css")) {
            return "text/css;charset=UTF-8";
        }
        if (lower.contains(".js")) {
            return "application/javascript;charset=UTF-8";
        }
        if (lower.contains(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        }
        if (lower.contains(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        }
        if (lower.contains(".webp")) {
            return "image/webp";
        }
        if (lower.contains(".svg")) {
            return "image/svg+xml";
        }
        if (lower.contains(".mp4")) {
            return "video/mp4";
        }
        if (lower.contains(".webm")) {
            return "video/webm";
        }
        if (lower.contains(".pdf")) {
            return MediaType.APPLICATION_PDF_VALUE;
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
