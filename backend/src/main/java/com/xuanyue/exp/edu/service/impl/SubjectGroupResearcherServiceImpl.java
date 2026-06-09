package com.xuanyue.exp.edu.service.impl;

import com.xuanyue.exp.edu.entity.SubjectGroup;
import com.xuanyue.exp.edu.entity.SubjectGroupResearcher;
import com.xuanyue.exp.edu.repository.SubjectGroupResearcherRepository;
import com.xuanyue.exp.edu.repository.SubjectGroupRepository;
import com.xuanyue.exp.edu.service.SubjectGroupResearcherService;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubjectGroupResearcherServiceImpl implements SubjectGroupResearcherService {

    private final SubjectGroupRepository groupRepository;
    private final SubjectGroupResearcherRepository researcherRepository;
    private final SysUserRepository sysUserRepository;

    public SubjectGroupResearcherServiceImpl(SubjectGroupRepository groupRepository,
                                             SubjectGroupResearcherRepository researcherRepository,
                                             SysUserRepository sysUserRepository) {
        this.groupRepository = groupRepository;
        this.researcherRepository = researcherRepository;
        this.sysUserRepository = sysUserRepository;
    }

    @Override
    public Object get(String groupId) {
        List<SubjectGroupResearcher> rows = researcherRepository.findByGroupId(groupId);
        List<Map<String, Object>> records = rows.stream().map(item -> {
            Map<String, Object> view = new HashMap<>();
            view.put("seqId", item.getSeqId());
            view.put("groupId", item.getGroupId());
            view.put("researcherUserId", item.getResearcherUserId());
            SysUser user = sysUserRepository.findById(item.getResearcherUserId()).orElse(null);
            view.put("researcherUserName", user == null ? item.getResearcherUserId() : (StringUtils.hasText(user.getUserName()) ? user.getUserName() : user.getLoginName()));
            return view;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", records.size());
        return result;
    }

    @Override
    @Transactional
    public void save(String groupId, Map<String, Object> payload, String currentUserId, String currentRootOrgId) {
        SubjectGroup group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("记录不存在"));
        if (StringUtils.hasText(currentRootOrgId) && !belongsToRootOrg(group, currentRootOrgId)) {
            throw new RuntimeException("只能为本机构教研组分配教研员");
        }
        List<String> researcherUserIds = toStringList(payload.get("researcherUserIds"));
        if (researcherUserIds.isEmpty()) {
            throw new RuntimeException("请选择教研员");
        }

        List<SysUser> researchers = sysUserRepository.findAll().stream()
                .filter(user -> researcherUserIds.contains(user.getUserId()))
                .filter(user -> "y".equalsIgnoreCase(asString(user.getStatus())))
                .filter(user -> "researcher".equalsIgnoreCase(asString(user.getUserRoleId())) || "Researcher".equals(asString(user.getUserRoleId())))
                .collect(Collectors.toList());
        if (researchers.size() != researcherUserIds.size()) {
            throw new RuntimeException("包含无效的教研员账号");
        }

        researcherRepository.deleteByGroupId(groupId);
        Date now = new Date();
        for (String researcherUserId : researcherUserIds) {
            SubjectGroupResearcher entity = new SubjectGroupResearcher();
            entity.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            entity.setGroupId(groupId);
            entity.setResearcherUserId(researcherUserId);
            entity.setCreateUserId(currentUserId);
            entity.setCreateTime(now);
            researcherRepository.save(entity);
        }
    }

    private boolean belongsToRootOrg(SubjectGroup group, String rootOrgId) {
        if (!StringUtils.hasText(rootOrgId)) return true;
        if (StringUtils.hasText(group.getCreateUserId()) && group.getCreateUserId().equals(rootOrgId)) return true;
        return sysUserRepository.findById(group.getCreateUserId())
                .map(user -> rootOrgId.equals(user.getRootOrgId()))
                .orElse(false);
    }

    private List<String> toStringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                String text = asString(item);
                if (StringUtils.hasText(text)) result.add(text);
            }
        }
        return result;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}
