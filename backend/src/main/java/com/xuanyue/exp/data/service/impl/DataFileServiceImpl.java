package com.xuanyue.exp.data.service.impl;

import com.xuanyue.exp.data.entity.DataFile;
import com.xuanyue.exp.data.entity.DataFileLog;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.data.repository.DataFileRepository;
import com.xuanyue.exp.data.repository.DataFileLogRepository;
import com.xuanyue.exp.data.service.DataFileService;
import com.xuanyue.exp.system.repository.SysUserRepository;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DataFileServiceImpl implements DataFileService {

    private final DataFileRepository dataFileRepository;
    private final DataFileLogRepository dataFileLogRepository;
    private final SysUserRepository sysUserRepository;
    private final MinioStorageService minioStorageService;

    public DataFileServiceImpl(DataFileRepository dataFileRepository, SysUserRepository sysUserRepository,DataFileLogRepository dataFileLogRepository, MinioStorageService minioStorageService) {
        this.dataFileRepository = dataFileRepository;
        this.sysUserRepository = sysUserRepository;
        this.dataFileLogRepository = dataFileLogRepository;
        this.minioStorageService = minioStorageService;
    }

    @Override
    public PageResult<?> list(String keyword, String status, String isPublic, String fileTypeId, String currentUserId, boolean publicMode, int pageNum, int pageSize) {
        String kw = asString(keyword);
        String st = asString(status);
        String pub = asString(isPublic);
        String typeId = asString(fileTypeId);
        String ownerId = asString(currentUserId);
        Map<String, String> userNameMap = loadUserNameMap();
        List<Map<String, Object>> records = dataFileRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime")).stream()
                .filter(item -> !StringUtils.hasText(kw)
                        || containsIgnoreCase(item.getFileName(), kw)
                        || containsIgnoreCase(item.getFileTag(), kw)
                        || containsIgnoreCase(item.getFileExt(), kw))
                .filter(item -> !StringUtils.hasText(st) || st.equals(item.getStatus()))
                .filter(item -> !StringUtils.hasText(pub) || pub.equals(item.getIsPublic()))
                .filter(item -> !StringUtils.hasText(typeId) || typeId.equals(item.getFileTypeId()))
                .filter(item -> {
                    if (publicMode) {
                        return true;
                    }
                    boolean isPublicItem = "y".equalsIgnoreCase(item.getIsPublic()) || "1".equals(String.valueOf(item.getIsPublic()));
                    boolean isOwnerItem = !StringUtils.hasText(ownerId) || ownerId.equals(item.getOwnerUserId());
                    return isPublicItem || isOwnerItem;
                })
                .map(item -> toView(item, userNameMap))
                .collect(Collectors.toList());
        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, records.size());
        int toIndex = Math.min(fromIndex + safePageSize, records.size());
        PageResult<Map<String, Object>> page = new PageResult<>(records.size(), records.subList(fromIndex, toIndex));
         buildPreviewUrlMap(page);
         return page;
    }

    @Override
    public PageResult<?> listVideos(String keyword, String fileTypeId, String currentUserId, int pageNum, int pageSize) {
        return list(keyword, "y", null, fileTypeId, currentUserId, false, pageNum, pageSize);
    }

    @Override
    public PageResult<?> listAll(String keyword, String status, String isPublic, String fileTypeId, String ownerUserId, int pageNum, int pageSize) {
        List<Map<String, Object>> records = dataFileRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime")).stream()
                .filter(item -> !StringUtils.hasText(asString(keyword))
                        || containsIgnoreCase(item.getFileName(), asString(keyword))
                        || containsIgnoreCase(item.getFileTag(), asString(keyword))
                        || containsIgnoreCase(item.getFileExt(), asString(keyword)))
                .filter(item -> !StringUtils.hasText(asString(ownerUserId)) || asString(ownerUserId).equals(item.getOwnerUserId()))
                .filter(item -> !StringUtils.hasText(asString(status)) || asString(status).equals(item.getStatus()))
                .filter(item -> !StringUtils.hasText(asString(isPublic)) || asString(isPublic).equals(item.getIsPublic()))
                .filter(item -> !StringUtils.hasText(asString(fileTypeId)) || asString(fileTypeId).equals(item.getFileTypeId()))
                .map(item -> toView(item, loadUserNameMap()))
                .collect(Collectors.toList());
        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, records.size());
        int toIndex = Math.min(fromIndex + safePageSize, records.size());
        PageResult<Map<String, Object>> page = new PageResult<>(records.size(), records.subList(fromIndex, toIndex));
         buildPreviewUrlMap(page);
         return page;
    }    

    private void buildPreviewUrlMap(PageResult<?> opager) {
        for(Object item : opager.getRecords()) {
            try {
                Map<String, Object> oMap = (Map<String, Object>) item;
                if(oMap.containsKey("fileUrl")) {
                    oMap.put("previewUrl", minioStorageService.buildPreviewUrl(oMap.get("fileUrl").toString()));
                }   
                if(oMap.containsKey("coverImageUrl")) {
                    oMap.put("coverImagePreviewUrl", minioStorageService.buildPreviewUrl(oMap.get("coverImageUrl").toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object get(String id) {
        return dataFileRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
    }

    @Override
    public void create(Map<String, Object> payload, String currentUserId) {
        DataFile item = new DataFile();
        item.setFileId(UUID.randomUUID().toString().replace("-", ""));
        applyPayload(item, payload, currentUserId);
        Date now = new Date();
        item.setOwnerUserId(currentUserId);
        item.setCreateUserId(currentUserId);
        item.setCreateTime(now);
        item.setUpdateTime(now);
        dataFileRepository.save(item);

        //log
        DataFileLog log = new DataFileLog();
        log.setLogId(UUID.randomUUID().toString().replace("-", ""));
        log.setFileId(item.getFileId());
        log.setLogType("create"); //create, update, delete
        log.setLogTypeName("创建");
        log.setLogUserId(currentUserId);
        log.setLogUserName(sysUserRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("用户不存在")).getUserName());
        log.setLogTime(now);
        dataFileLogRepository.save(log);
    }

    @Override
    public void update(String id, Map<String, Object> payload, String currentUserId) {
        DataFile item = dataFileRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        String oldIsPublic = item.getIsPublic();
        applyPayload(item, payload, currentUserId);
        dataFileRepository.save(item);

        //log
        String newIsPublic = item.getIsPublic();
        DataFileLog log = new DataFileLog();
        log.setLogId(UUID.randomUUID().toString().replace("-", ""));
        log.setFileId(item.getFileId());
        if(oldIsPublic.equals(newIsPublic)) {
            log.setLogType("update"); //create, update, delete
            log.setLogTypeName("更新");
        } else {
            if(item.getIsPublic().equals("y")) {
                log.setLogType("public");
                log.setLogTypeName("公开");
            } else {
                log.setLogType("unpublic");
                log.setLogTypeName("待公开");
            }
        }
        log.setLogUserId(currentUserId);
        log.setLogUserName(sysUserRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("用户不存在")).getUserName());
        log.setLogTime(new Date());
        dataFileLogRepository.save(log);
    }

    @Override
    public void updatePublic(String id, Map<String, Object> payload, String currentUserId) {
        DataFile item = dataFileRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        String oldIsPublic = item.getIsPublic();
        item.setIsPublic(defaultPublic(payload.get("isPublic")));
        dataFileRepository.save(item);

        //log
        String newIsPublic = item.getIsPublic();
        DataFileLog log = new DataFileLog();
        log.setLogId(UUID.randomUUID().toString().replace("-", ""));
        log.setFileId(item.getFileId());
        if(oldIsPublic.equals(newIsPublic)) {
            log.setLogType("update"); //create, update, delete
            log.setLogTypeName("更新");
        } else {
            if(item.getIsPublic().equals("y")) {
                log.setLogType("public");
                log.setLogTypeName("公开");
            } else {
                log.setLogType("unpublic");
                log.setLogTypeName("待公开");
            }
        }
        log.setLogUserId(currentUserId);
        log.setLogUserName(sysUserRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("用户不存在")).getUserName());
        log.setLogTime(new Date());
        dataFileLogRepository.save(log);
    }

    @Override
    public void delete(String id) {
        dataFileRepository.deleteById(id);
    }

    private void applyPayload(DataFile item, Map<String, Object> payload, String currentUserId) {
        item.setFileName(asString(payload.get("fileName")));
        item.setFileTag(asString(payload.get("fileTag")));
        item.setFileUrl(asString(payload.get("fileUrl")));
        item.setFileTypeId(asString(payload.get("fileTypeId")));
        item.setStatus(defaultStatus(payload.get("status")));
        item.setUpdateUserId(StringUtils.hasText(currentUserId) ? currentUserId : item.getUpdateUserId());
        item.setUpdateTime(new Date());
        item.setCoverImageUrl(asString(payload.get("coverImageUrl")));
        item.setFileSize(asLong(payload.get("fileSize")));
        item.setFileExt(resolveFileExt(payload.get("fileExt"), item.getFileName(), item.getFileUrl()));
        item.setIsPublic(defaultPublic(payload.get("isPublic")));
        item.setComments(asString(payload.get("comments")));
    }

    private Map<String, String> loadUserNameMap() {
        return sysUserRepository.findAll().stream().collect(Collectors.toMap(SysUser::getUserId, user -> {
            String userName = user.getUserName();
            if (!StringUtils.hasText(userName)) {
                userName = user.getLoginName();
            }
            return StringUtils.hasText(userName) ? userName : user.getUserId();
        }, (a, b) -> a, HashMap::new));
    }

    private Map<String, Object> toView(DataFile item, Map<String, String> userNameMap) {
        Map<String, Object> view = new HashMap<String, Object>();
        view.put("fileId", item.getFileId());
        view.put("fileName", item.getFileName());
        view.put("fileTag", item.getFileTag());
        view.put("fileUrl", item.getFileUrl());
        view.put("fileTypeId", item.getFileTypeId());
        view.put("status", item.getStatus());
        view.put("ownerUserId", userNameMap.getOrDefault(item.getOwnerUserId(), item.getOwnerUserId()));
        view.put("coverImageUrl", item.getCoverImageUrl());
        view.put("fileSize", item.getFileSize());
        view.put("fileExt", item.getFileExt());
        view.put("isPublic", item.getIsPublic());
        view.put("createTime", item.getCreateTime());
        view.put("updateTime", item.getUpdateTime());
        view.put("comments", item.getComments());
        return view;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private String resolveFileExt(Object explicitExt, String fileName, String fileUrl) {
        String ext = asString(explicitExt);
        if (StringUtils.hasText(ext)) {
            return ext.startsWith(".") ? ext.substring(1) : ext;
        }
        String source = StringUtils.hasText(fileName) ? fileName : fileUrl;
        if (!StringUtils.hasText(source) || !source.contains(".")) {
            return null;
        }
        return source.substring(source.lastIndexOf('.') + 1).toLowerCase();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Long asLong(Object value) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String defaultStatus(Object value) {
        String status = asString(value);
        return StringUtils.hasText(status) ? status : "y";
    }

    private String defaultPublic(Object value) {
        String v = asString(value);
        return StringUtils.hasText(v) ? v : "n";
    }
}
