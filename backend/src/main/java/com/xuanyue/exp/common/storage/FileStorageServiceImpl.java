package com.xuanyue.exp.common.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final FileStorageProperties properties;

    public FileStorageServiceImpl(FileStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, Object> upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        String dateFolder = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Path uploadDir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize().resolve(dateFolder);
        Files.createDirectories(uploadDir);
        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        //String safeName = UUID.randomUUID().toString().replace("-", "") + "_" + originalName;
        String safeName = UUID.randomUUID().toString().replace("-", "") + "." + getFileExtension(originalName);
        Path target = uploadDir.resolve(safeName);
        file.transferTo(target.toFile());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("fileUrl", "/" + dateFolder + "/" + safeName);
        //result.put("fileUrl", normalizeUrl(properties.getUrlPrefix()) + "/" + dateFolder + "/" + safeName);
        result.put("fileName", originalName);
        result.put("filePath", target.toString());
        return result;
    }

    @Override
    public boolean deleteByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return false;
        }
        String prefix = normalizeUrl(properties.getUrlPrefix());
        String relative = fileUrl.startsWith(prefix) ? fileUrl.substring(prefix.length()) : fileUrl;
        if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        Path target = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize().resolve(relative);
        try {
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            return false;
        }
    }

    private String normalizeUrl(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return "/uploads";
        }
        String strprefix = prefix;
        if (prefix.startsWith("/")) {
            strprefix = prefix;
        }else if(prefix.startsWith("http://")) {
            strprefix = prefix;
        }else if(prefix.startsWith("https://")) {
            strprefix = prefix;
        }else{
            strprefix = "/" + prefix;
        }
        return strprefix;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
