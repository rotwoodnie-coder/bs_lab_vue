package com.xuanyue.exp.edu.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.edu.entity.SubjectGroup;
import com.xuanyue.exp.edu.entity.SubjectGroupResearcher;
import com.xuanyue.exp.edu.repository.SubjectGroupResearcherRepository;
import com.xuanyue.exp.edu.repository.SubjectGroupRepository;
import com.xuanyue.exp.edu.service.SubjectGroupService;
import com.xuanyue.exp.data.service.DataDictService;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubjectGroupServiceImpl implements SubjectGroupService {

    private final SubjectGroupRepository repository;
    private final SubjectGroupResearcherRepository researcherRepository;
    private final DataDictService dataDictService;
    private final SysUserRepository sysUserRepository;

    public SubjectGroupServiceImpl(SubjectGroupRepository repository, SubjectGroupResearcherRepository researcherRepository, DataDictService dataDictService, SysUserRepository sysUserRepository) {
        this.repository = repository;
        this.researcherRepository = researcherRepository;
        this.dataDictService = dataDictService;
        this.sysUserRepository = sysUserRepository;
    }

    @Override
    public PageResult<?> page(int pageNum, int pageSize, String keyword, String currentRootOrgId) {
        String kw = asString(keyword);
        String rootOrgId = asString(currentRootOrgId);
        List<SubjectGroup> rows = repository.findAll(Sort.by(Sort.Direction.DESC, "createTime")).stream()
                //.filter(item -> !StringUtils.hasText(rootOrgId) || belongsToRootOrg(item, rootOrgId))
                .filter(item -> !StringUtils.hasText(kw)
                        || containsIgnoreCase(item.getGroupName(), kw)
                        || containsIgnoreCase(item.getComments(), kw)
                        || containsIgnoreCase(item.getSubjectId(), kw))
                .collect(Collectors.toList());
        List<Map<String, Object>> views = rows.stream().map(this::toView).collect(Collectors.toList());
        Map<String, Long> researcherCountMap = researcherRepository.findAll().stream().collect(Collectors.groupingBy(SubjectGroupResearcher::getGroupId, Collectors.counting()));
        views.forEach(view -> view.put("researcherCount", researcherCountMap.getOrDefault(asString(view.get("groupId")), 0L)));
        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, views.size());
        int toIndex = Math.min(fromIndex + safePageSize, views.size());
        return new PageResult<>(views.size(), views.subList(fromIndex, toIndex));
    }

    @Override
    public Object get(String groupId) {
        SubjectGroup entity = repository.findById(groupId).orElseThrow(() -> new RuntimeException("记录不存在"));
        return toView(entity);
    }

    @Override
    @Transactional
    public void create(Map<String, Object> payload, String currentUserId, String currentRootOrgId) {
        SubjectGroup entity = new SubjectGroup();
        entity.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        applyPayload(entity, payload, currentUserId, currentRootOrgId);
        Date now = new Date();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void update(String groupId, Map<String, Object> payload, String currentUserId, String currentRootOrgId) {
        SubjectGroup entity = repository.findById(groupId).orElseThrow(() -> new RuntimeException("记录不存在"));
        applyPayload(entity, payload, currentUserId, currentRootOrgId);
        entity.setUpdateTime(new Date());
        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(String groupId, String currentRootOrgId) {
        SubjectGroup entity = repository.findById(groupId).orElseThrow(() -> new RuntimeException("记录不存在"));
        if (StringUtils.hasText(currentRootOrgId) && !currentRootOrgId.equals(entity.getCreateUserId())) {
            throw new RuntimeException("只能删除本机构创建的教研组");
        }
        repository.deleteById(groupId);
    }

    private void applyPayload(SubjectGroup entity, Map<String, Object> payload, String currentUserId, String currentRootOrgId) {
        entity.setGroupName(requireText(payload.get("groupName"), "教研组名称不能为空"));
        entity.setSubjectId(requireText(payload.get("subjectId"), "所属学科不能为空"));
        entity.setStatus(defaultStatus(payload.get("status")));
        entity.setComments(asString(payload.get("comments")));
        entity.setCreateUserId(StringUtils.hasText(currentUserId) ? currentUserId : entity.getCreateUserId());
        entity.setUpdateUserId(StringUtils.hasText(currentUserId) ? currentUserId : entity.getUpdateUserId());
    }

    private Map<String, Object> toView(SubjectGroup entity) {
        Map<String, Object> view = new HashMap<>();
        view.put("groupId", entity.getGroupId());
        view.put("groupName", entity.getGroupName());
        view.put("subjectId", entity.getSubjectId());
        view.put("subjectName", resolveName("data_school_subject", entity.getSubjectId()));
        view.put("status", entity.getStatus());
        view.put("comments", entity.getComments());
        view.put("createUserId", entity.getCreateUserId());
        view.put("createUserName", resolveUserName(entity.getCreateUserId()));
        view.put("createTime", entity.getCreateTime());
        view.put("updateUserId", entity.getUpdateUserId());
        view.put("updateUserName", resolveUserName(entity.getUpdateUserId()));
        view.put("updateTime", entity.getUpdateTime());
        return view;
    }

    private String resolveUserName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "-";
        }
        return sysUserRepository.findById(userId)
                .map(user -> {
                    if (StringUtils.hasText(user.getUserName())) return user.getUserName();
                    if (StringUtils.hasText(user.getLoginName())) return user.getLoginName();
                    return userId;
                })
                .orElse(userId);
    }

    private String resolveName(String type, String id) {
        try {
            Object value = dataDictService.get(type, id);
            if (value instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) value;
                Object name = map.get("subject_name");
                if (name == null) name = map.get("subjectName");
                if (name == null) name = map.get("name");
                return name == null ? id : String.valueOf(name);
            }
        } catch (Exception ignored) {
        }
        return id;
    }

    private boolean belongsToRootOrg(SubjectGroup entity, String rootOrgId) {
        if (!StringUtils.hasText(rootOrgId)) return true;
        if (rootOrgId.equals(entity.getCreateUserId())) return true;
        if (!StringUtils.hasText(entity.getCreateUserId())) return false;
        return sysUserRepository.findById(entity.getCreateUserId())
                .map(user -> rootOrgId.equals(user.getRootOrgId()))
                .orElse(false);
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String defaultStatus(Object value) {
        String status = asString(value);
        return StringUtils.hasText(status) ? status : "y";
    }

    private String requireText(Object value, String message) {
        String text = asString(value);
        if (!StringUtils.hasText(text)) {
            throw new RuntimeException(message);
        }
        return text;
    }
}
