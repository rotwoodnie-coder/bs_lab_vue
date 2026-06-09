package com.xuanyue.exp.edu.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.edu.entity.TeacherSubject;
import com.xuanyue.exp.edu.repository.TeacherSubjectRepository;
import com.xuanyue.exp.edu.service.TeacherSubjectService;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.data.domain.Sort;
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
public class TeacherSubjectServiceImpl implements TeacherSubjectService {

    private final SysUserRepository sysUserRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    public TeacherSubjectServiceImpl(SysUserRepository sysUserRepository, TeacherSubjectRepository teacherSubjectRepository) {
        this.sysUserRepository = sysUserRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
    }

    @Override
    public PageResult<?> page(int pageNum, int pageSize, String keyword, String currentRootOrgId) {
        String kw = asString(keyword);
        String rootOrgId = asString(currentRootOrgId);
        List<SysUser> teachers = sysUserRepository.findAll(Sort.by(Sort.Direction.ASC, "userName")).stream()
                .filter(user -> StringUtils.hasText(rootOrgId) && rootOrgId.equals(user.getRootOrgId()))
                .filter(user -> "teacher".equalsIgnoreCase(asString(user.getUserRoleId())) || "Teacher".equals(asString(user.getUserRoleId())))
                .filter(user -> "y".equalsIgnoreCase(asString(user.getStatus())))
                .filter(user -> !StringUtils.hasText(kw)
                        || containsIgnoreCase(user.getUserName(), kw)
                        || containsIgnoreCase(user.getLoginName(), kw)
                        || containsIgnoreCase(user.getUserNickName(), kw))
                .collect(Collectors.toList());

        List<Map<String, Object>> records = teachers.stream().map(this::toView).collect(Collectors.toList());
        Map<String, List<TeacherSubject>> subjectMap = teacherSubjectRepository.findAll().stream()
                .collect(Collectors.groupingBy(TeacherSubject::getTeacherId));
        records.forEach(record -> {
            List<TeacherSubject> teacherSubjects = subjectMap.getOrDefault(asString(record.get("userId")), new ArrayList<>());
            record.put("subjectIds", teacherSubjects.stream().map(TeacherSubject::getSubjectId).collect(Collectors.toList()));
            record.put("subjectCount", teacherSubjects.size());
            record.put("teacherSubjectStatus", teacherSubjects.isEmpty() ? null : teacherSubjects.get(0).getStatus());
        });
        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, records.size());
        int toIndex = Math.min(fromIndex + safePageSize, records.size());
        return new PageResult<>(records.size(), records.subList(fromIndex, toIndex));
    }

    @Override
    public Object get(String teacherId) {
        SysUser user = sysUserRepository.findById(teacherId).orElseThrow(() -> new RuntimeException("教师不存在"));
        Map<String, Object> view = toView(user);
        List<TeacherSubject> teacherSubjects = teacherSubjectRepository.findByTeacherId(teacherId);
        view.put("subjectIds", teacherSubjects.stream().map(TeacherSubject::getSubjectId).collect(Collectors.toList()));
        view.put("teacherSubjectStatus", teacherSubjects.isEmpty() ? null : teacherSubjects.get(0).getStatus());
        return view;
    }

    @Override
    @Transactional
    public void save(String teacherId, Map<String, Object> payload, String currentUserId, String currentRootOrgId) {
        SysUser user = sysUserRepository.findById(teacherId).orElseThrow(() -> new RuntimeException("教师不存在"));
        if (!StringUtils.hasText(currentRootOrgId) || !currentRootOrgId.equals(user.getRootOrgId())) {
            throw new RuntimeException("只能为同校教师分配授课关系");
        }
        if (!"teacher".equalsIgnoreCase(asString(user.getUserRoleId())) && !"Teacher".equals(asString(user.getUserRoleId()))) {
            throw new RuntimeException("只能为教师分配授课关系");
        }
        if (!"y".equalsIgnoreCase(asString(user.getStatus()))) {
            throw new RuntimeException("只能为启用状态教师分配授课关系");
        }
        List<String> subjectIds = toStringList(payload.get("subjectIds"));
        if (subjectIds.isEmpty()) {
            String single = asString(payload.get("subjectId"));
            if (StringUtils.hasText(single)) {
                subjectIds.add(single);
            }
        }
        if (subjectIds.isEmpty()) {
            throw new RuntimeException("请选择学科");
        }
        String status = defaultStatus(payload.get("status"));

        teacherSubjectRepository.deleteByTeacherId(teacherId);
        Date now = new Date();
        for (String subjectId : subjectIds) {
            TeacherSubject entity = new TeacherSubject();
            entity.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            entity.setTeacherId(teacherId);
            entity.setSubjectId(subjectId);
            entity.setStatus(status);
            entity.setCreateTime(now);
            entity.setCreateUserId(currentUserId);
            entity.setUpdateTime(now);
            entity.setUpdateUserId(currentUserId);
            teacherSubjectRepository.save(entity);
        }
    }

    private Map<String, Object> toView(SysUser user) {
        Map<String, Object> view = new HashMap<>();
        view.put("userId", user.getUserId());
        view.put("userName", user.getUserName());
        view.put("loginName", user.getLoginName());
        view.put("status", user.getStatus());
        view.put("rootOrgId", user.getRootOrgId());
        view.put("userRoleId", user.getUserRoleId());
        view.put("userNickName", user.getUserNickName());
        return view;
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

    private List<String> toStringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                String text = asString(item);
                if (StringUtils.hasText(text)) {
                    result.add(text);
                }
            }
        }
        return result;
    }

    private String requireText(Object value, String message) {
        String text = asString(value);
        if (!StringUtils.hasText(text)) {
            throw new RuntimeException(message);
        }
        return text;
    }
}
