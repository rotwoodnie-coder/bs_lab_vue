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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        Pageable pageable = PageRequest.of(Math.max((query.getPageNum() == null ? 1 : query.getPageNum()) - 1, 0), query.getPageSize() == null ? 10 : query.getPageSize());
        String keyword = StringUtils.hasText(query.getKeyword()) ? query.getKeyword().trim() : null;
        String status = StringUtils.hasText(query.getStatus()) ? query.getStatus().trim() : null;

        List<ExpSimulator> all;
        if (StringUtils.hasText(keyword) && StringUtils.hasText(status)) {
            all = repository.findBySimulatorNameContainingOrCommentsContainingAndStatus(keyword, keyword, status);
        } else if (StringUtils.hasText(keyword)) {
            all = repository.findBySimulatorNameContainingOrCommentsContaining(keyword, keyword);
        } else if (StringUtils.hasText(status)) {
            all = repository.findByStatus(status);
        } else {
            all = repository.findAll();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<ExpSimulatorListItem> list = new ArrayList<ExpSimulatorListItem>();
        Map<String, String> userNameMap = loadUserNameMap(all);
        if (start <= end) {
            for (ExpSimulator simulator : all.subList(start, end)) {
                list.add(toItem(simulator, userNameMap.get(simulator.getCreateUserId())));
            }
        }
         PageResult<ExpSimulatorListItem> page= new PageResult<ExpSimulatorListItem>(all.size(), list);
         List<ExpSimulatorListItem> records = page.getRecords();
         for (ExpSimulatorListItem record : records) {
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

    private ExpSimulatorListItem toItem(ExpSimulator simulator, String createUserName) {
        return new ExpSimulatorListItem(simulator.getSimulatorId(), simulator.getSimulatorName(), simulator.getSubjectId(), simulator.getCoverImageUrl(), simulator.getSimulatorUrl(), simulator.getComments(), simulator.getStatus(), createUserName);
    }
}
