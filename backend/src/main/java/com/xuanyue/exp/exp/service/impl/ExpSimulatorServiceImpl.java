package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.exp.dto.ExpSimulatorListItem;
import com.xuanyue.exp.exp.dto.ExpSimulatorPageQuery;
import com.xuanyue.exp.exp.dto.ExpSimulatorSaveRequest;
import com.xuanyue.exp.exp.dto.ExpSimulatorUpdateRequest;
import com.xuanyue.exp.exp.entity.ExpSimulator;
import com.xuanyue.exp.exp.repository.ExpSimulatorRepository;
import com.xuanyue.exp.exp.service.ExpSimulatorService;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.xuanyue.exp.common.storage.minio.MinioStorageService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpSimulatorServiceImpl implements ExpSimulatorService {

    private final ExpSimulatorRepository repository;
    private final SysUserRepository sysUserRepository;
    private final MinioStorageService minioStorageService;

    public ExpSimulatorServiceImpl(ExpSimulatorRepository repository, SysUserRepository sysUserRepository, MinioStorageService minioStorageService) {
        this.repository = repository;
        this.sysUserRepository = sysUserRepository;
        this.minioStorageService = minioStorageService;
    }

    @Override
    public PageResult<ExpSimulatorListItem> page(ExpSimulatorPageQuery query) {
        int pageNum = Math.max(query == null || query.getPageNum() == null ? 1 : query.getPageNum(), 1);
        int pageSize = Math.max(query == null || query.getPageSize() == null ? 10 : query.getPageSize(), 1);
        String keyword = StringUtils.hasText(query == null ? null : query.getKeyword()) ? query.getKeyword().trim() : null;
        String status = StringUtils.hasText(query == null ? null : query.getStatus()) ? query.getStatus().trim() : null;
        String subjectId = StringUtils.hasText(query == null ? null : query.getSubjectId()) ? query.getSubjectId().trim() : null;

        List<ExpSimulator> filtered = repository.findAll();
        filtered = filtered.stream()
                .filter(item -> !StringUtils.hasText(keyword)
                        || containsTextIgnoreCase(item.getSimulatorName(), keyword)
                        || containsTextIgnoreCase(item.getComments(), keyword))
                .filter(item -> !StringUtils.hasText(status) || status.equals(item.getStatus()))
                .filter(item -> !StringUtils.hasText(subjectId) || subjectId.equals(item.getSubjectId()))
                .sorted(Comparator.comparing(ExpSimulator::getSimulatorName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());

        Map<String, String> userNameMap = loadUserNameMap(filtered);
        int start = Math.min((pageNum - 1) * pageSize, filtered.size());
        int end = Math.min(start + pageSize, filtered.size());
        List<ExpSimulatorListItem> list = new ArrayList<ExpSimulatorListItem>();
        if (start < end) {
            for (ExpSimulator simulator : filtered.subList(start, end)) {
                list.add(toItem(simulator, userNameMap.get(simulator.getCreateUserId())));
            }
        }
        PageResult<ExpSimulatorListItem> page = new PageResult<ExpSimulatorListItem>(filtered.size(), list);
        for (ExpSimulatorListItem record : page.getRecords()) {
            enrichPreviewUrls(record);
        }
        return page;
    }


    @Override
    public ExpSimulatorListItem get(String simulatorId) {
        ExpSimulator simulator = repository.findById(simulatorId).orElseThrow(() -> new RuntimeException("模拟器不存在"));
        String createUserName = simulator.getCreateUserId();
        if (StringUtils.hasText(simulator.getCreateUserId())) {
            SysUser user = sysUserRepository.findById(simulator.getCreateUserId()).orElse(null);
            if (user != null) {
                createUserName = StringUtils.hasText(user.getUserName()) ? user.getUserName() : user.getLoginName();
            }
        }
        ExpSimulatorListItem item = toItem(simulator, createUserName);
        enrichPreviewUrls(item);
        return item;
    }

    private void enrichPreviewUrls(ExpSimulatorListItem record) {
        if (record == null) {
            return;
        }
        String coverImageUrl = record.getCoverImageUrl();
        if (StringUtils.hasText(coverImageUrl)) {
            record.setCoverImagePreviewUrl(minioStorageService.buildPreviewUrl(coverImageUrl));
        }
        String simulatorUrl = record.getSimulatorUrl();
        record.setSimulatorPreviewUrl(simulatorUrl);
        if (StringUtils.hasText(simulatorUrl) && !simulatorUrl.trim().toLowerCase().startsWith("http")) {
            record.setSimulatorPreviewUrl(minioStorageService.buildPreviewUrl(simulatorUrl));
        }
    }

    @Override
    public void create(ExpSimulatorSaveRequest request, String userId) {
        ExpSimulator simulator = new ExpSimulator();
        simulator.setSimulatorId(UUID.randomUUID().toString().replace("-", ""));
        fill(simulator, request.getSimulatorName(), request.getSubjectId(), request.getCoverImageUrl(), request.getSimulatorUrl(), request.getComments(), request.getStatus());
        simulator.setCreateUserId(userId);
        simulator.setUpdateUserId(userId);
        repository.save(simulator);
    }

    @Override
    public void update(String simulatorId, ExpSimulatorUpdateRequest request, String userId) {
        ExpSimulator simulator = repository.findById(simulatorId).orElseThrow(() -> new RuntimeException("模拟器不存在"));
        fill(simulator, request.getSimulatorName(), request.getSubjectId(), request.getCoverImageUrl(), request.getSimulatorUrl(), request.getComments(), request.getStatus());
        simulator.setUpdateUserId(userId);
        repository.save(simulator);
    }

    @Override
    public void delete(String simulatorId) {
        ExpSimulator simulator = repository.findById(simulatorId).orElseThrow(() -> new RuntimeException("模拟器不存在"));
        repository.delete(simulator);
    }

    private void fill(ExpSimulator simulator, String simulatorName, String subjectId, String coverImageUrl, String simulatorUrl, String comments, String status) {
        simulator.setSimulatorName(truncateText(simulatorName, 60));
        simulator.setSubjectId(subjectId);
        simulator.setCoverImageUrl(normalizeStorageUrl(coverImageUrl, "封面图片地址"));
        simulator.setSimulatorUrl(normalizeSimulatorUrl(simulatorUrl));
        simulator.setComments(truncateText(comments, 100));
        simulator.setStatus(StringUtils.hasText(status) ? status : "y");
    }

    private String normalizeSimulatorUrl(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new RuntimeException("模拟器URL不能为空");
        }
        String trimmed = raw.trim();
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            if (trimmed.length() > 200) {
                throw new RuntimeException("模拟器URL过长");
            }
            return trimmed;
        }
        return normalizeStorageUrl(trimmed, "模拟器URL");
    }

    private String normalizeStorageUrl(String raw, String fieldLabel) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String objectKey = minioStorageService.normalizeStorageKey(raw.trim());
        if (!StringUtils.hasText(objectKey)) {
            throw new RuntimeException(fieldLabel + "无效");
        }
        String stored = "/" + objectKey;
        if (stored.length() > 200) {
            throw new RuntimeException(fieldLabel + "过长，请重新上传");
        }
        return stored;
    }

    private String truncateText(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private Map<String, String> loadUserNameMap(List<ExpSimulator> list) {
        Map<String, String> userNameMap = new HashMap<String, String>();
        for (ExpSimulator simulator : list) {
            String userId = simulator.getCreateUserId();
            if (StringUtils.hasText(userId) && !userNameMap.containsKey(userId)) {
                SysUser user = sysUserRepository.findById(userId).orElse(null);
                userNameMap.put(userId, user == null ? userId : (StringUtils.hasText(user.getUserName()) ? user.getUserName() : user.getLoginName()));
            }
        }
        return userNameMap;
    }

    private boolean containsTextIgnoreCase(String source, String keyword) {
        return StringUtils.hasText(source) && StringUtils.hasText(keyword) && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private ExpSimulatorListItem toItem(ExpSimulator simulator, String createUserName) {
        return new ExpSimulatorListItem(simulator.getSimulatorId(), simulator.getSimulatorName(), simulator.getSubjectId(), simulator.getCoverImageUrl(), simulator.getSimulatorUrl(), simulator.getComments(), simulator.getStatus(), createUserName);
    }
}
