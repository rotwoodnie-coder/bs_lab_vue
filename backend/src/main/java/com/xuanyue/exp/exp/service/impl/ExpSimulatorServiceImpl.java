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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        String gradeKey = query == null ? null : query.getGradeKey();
        List<String> gradeIds = resolveGradeIds(gradeKey);
        Set<String> allowedByGrade = null;
        if (gradeIds != null && !gradeIds.isEmpty()) {
            String gradeStatus = StringUtils.hasText(status) ? status : "y";
            allowedByGrade = new HashSet<>(repository.findIdsByStatusAndGradeIds(gradeStatus, gradeIds));
        }

        List<ExpSimulator> filtered = repository.findAll();
        final Set<String> gradeAllowed = allowedByGrade;
        filtered = filtered.stream()
                .filter(item -> !StringUtils.hasText(keyword)
                        || containsTextIgnoreCase(item.getSimulatorName(), keyword)
                        || containsTextIgnoreCase(item.getComments(), keyword))
                .filter(item -> !StringUtils.hasText(status) || status.equals(item.getStatus()))
                .filter(item -> !StringUtils.hasText(subjectId) || subjectId.equals(item.getSubjectId()))
                .filter(item -> gradeAllowed == null || gradeAllowed.contains(item.getSimulatorId()))
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
            String coverImageUrl = record.getCoverImageUrl();
            if (StringUtils.hasText(coverImageUrl)) {
                record.setCoverImagePreviewUrl(minioStorageService.buildPreviewUrl(coverImageUrl));
            }
            String simulatorUrl = record.getSimulatorUrl();
            record.setSimulatorPreviewUrl(simulatorUrl);
            if (StringUtils.hasText(simulatorUrl) && !simulatorUrl.startsWith("http")) {
                record.setSimulatorPreviewUrl(minioStorageService.buildPreviewUrl(simulatorUrl));
            }
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
        return toItem(simulator, createUserName);
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
        simulator.setSimulatorName(simulatorName);
        simulator.setSubjectId(subjectId);
        simulator.setCoverImageUrl(coverImageUrl);
        simulator.setSimulatorUrl(simulatorUrl);
        simulator.setComments(comments);
        simulator.setStatus(StringUtils.hasText(status) ? status : "y");
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

    private List<String> resolveGradeIds(String gradeKey) {
        if (!StringUtils.hasText(gradeKey) || "all".equalsIgnoreCase(gradeKey.trim())) {
            return null;
        }
        switch (gradeKey.trim()) {
            case "g12":
                return Arrays.asList("g1", "g2");
            case "g34":
                return Arrays.asList("g3", "g4");
            case "g56":
                return Arrays.asList("g5", "g6");
            default:
                return null;
        }
    }

    private ExpSimulatorListItem toItem(ExpSimulator simulator, String createUserName) {
        return new ExpSimulatorListItem(simulator.getSimulatorId(), simulator.getSimulatorName(), simulator.getSubjectId(), simulator.getCoverImageUrl(), simulator.getSimulatorUrl(), simulator.getComments(), simulator.getStatus(), createUserName);
    }
}
