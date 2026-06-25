package com.xuanyue.exp.edu.service.impl;

import com.xuanyue.exp.edu.entity.SubjectGroup;
import com.xuanyue.exp.edu.entity.SubjectGroupTeacher;
import com.xuanyue.exp.edu.repository.SubjectGroupRepository;
import com.xuanyue.exp.edu.repository.SubjectGroupTeacherRepository;
import com.xuanyue.exp.edu.service.SubjectGroupTeacherService;
import com.xuanyue.exp.edu.entity.TeacherSubject;
import com.xuanyue.exp.edu.repository.TeacherSubjectRepository;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysOrgRepository;
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
public class SubjectGroupTeacherServiceImpl implements SubjectGroupTeacherService {

    private final SubjectGroupRepository groupRepository;
    private final SubjectGroupTeacherRepository teacherRepository;
    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    public SubjectGroupTeacherServiceImpl(SubjectGroupRepository groupRepository,
                                          SubjectGroupTeacherRepository teacherRepository,
                                          SysUserRepository sysUserRepository,
                                          SysOrgRepository sysOrgRepository,
                                          TeacherSubjectRepository teacherSubjectRepository) {
        this.groupRepository = groupRepository;
        this.teacherRepository = teacherRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
    }

    @Override
    public Object get(String groupId) {
        List<SubjectGroupTeacher> rows = teacherRepository.findByGroupId(groupId);
        List<Map<String, Object>> records = rows.stream().map(item -> {
            Map<String, Object> view = new HashMap<>();
            view.put("seqId", item.getSeqId());
            view.put("groupId", item.getGroupId());
            view.put("teacherUserId", item.getTeacherUserId());
            SysUser user = sysUserRepository.findById(item.getTeacherUserId()).orElse(null);
            view.put("teacherUserName", user == null ? item.getTeacherUserId() : (StringUtils.hasText(user.getUserName()) ? user.getUserName() : user.getLoginName()));
            return view;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", records.size());
        return result;
    }

    @Override
    @Transactional
    public void save(String groupId, Object payload, String currentUserId, String currentRootOrgId) {
        SubjectGroup group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("记录不存在"));
        List<String> teacherUserIds = toStringList(payload);
        if (teacherUserIds.isEmpty()) {
            throw new RuntimeException("请选择教师");
        }

        List<SysUser> teachers = sysUserRepository.findAll().stream()
                .filter(user -> teacherUserIds.contains(user.getUserId()))
                .filter(user -> "y".equalsIgnoreCase(String.valueOf(user.getStatus())))
                .collect(Collectors.toList());
        if (teachers.size() != teacherUserIds.size()) {
            throw new RuntimeException("包含无效的教师账号");
        }

        teacherRepository.deleteByGroupId(groupId);
        Date now = new Date();
        for (String teacherUserId : teacherUserIds) {
            SubjectGroupTeacher entity = new SubjectGroupTeacher();
            entity.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            entity.setGroupId(groupId);
            entity.setTeacherUserId(teacherUserId);
            entity.setCreateUserId(currentUserId);
            entity.setCreateTime(now);
            teacherRepository.save(entity);
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
        if (value instanceof Map) {
            Object ids = ((Map<?, ?>) value).get("teacherUserIds");
            if (ids instanceof List) {
                for (Object item : (List<?>) ids) {
                    String text = asString(item);
                    if (StringUtils.hasText(text)) result.add(text);
                }
            }
        } else if (value instanceof List) {
            for (Object item : (List<?>) value) {
                String text = asString(item);
                if (StringUtils.hasText(text)) result.add(text);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> teacherAssignOptions(String subjectId) {
        List<SysOrg> orgs = sysOrgRepository.findAll();
        Map<String, String> orgNameMap = orgs.stream()
                .collect(Collectors.toMap(SysOrg::getOrgId, org -> StringUtils.hasText(org.getOrgName()) ? org.getOrgName() : org.getOrgId(), (a, b) -> a));

        String filterSubjectId = asString(subjectId);
        List<String> teacherIdsBySubject = teacherSubjectRepository.findAll().stream()
                .filter(item -> !StringUtils.hasText(filterSubjectId) || filterSubjectId.equals(asString(item.getSubjectId())))
                .map(TeacherSubject::getTeacherId)
                .distinct()
                .collect(Collectors.toList());

        return sysUserRepository.findAll().stream()
                .filter(user -> "y".equalsIgnoreCase(asString(user.getStatus())))
                .filter(user -> "teacher".equalsIgnoreCase(asString(user.getUserRoleId())))
                .filter(user -> !StringUtils.hasText(filterSubjectId) || teacherIdsBySubject.contains(user.getUserId()))
                .map(user -> {
                    Map<String, Object> view = new HashMap<>();
                    view.put("userId", user.getUserId());
                    view.put("userName", StringUtils.hasText(user.getUserName()) ? user.getUserName() : user.getLoginName());
                    view.put("rootOrgId", user.getRootOrgId());
                    view.put("rootOrgName", orgNameMap.getOrDefault(user.getRootOrgId(), user.getRootOrgId()));
                    return view;
                })
                .sorted((a, b) -> {
                    int orgCompare = String.valueOf(a.get("rootOrgName")).compareToIgnoreCase(String.valueOf(b.get("rootOrgName")));
                    if (orgCompare != 0) return orgCompare;
                    return String.valueOf(a.get("userName")).compareToIgnoreCase(String.valueOf(b.get("userName")));
                })
                .collect(Collectors.toList());
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}
