package com.xuanyue.exp.edu.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.edu.entity.TeacherClass;
import com.xuanyue.exp.edu.repository.TeacherClassRepository;
import com.xuanyue.exp.edu.service.TeacherClassService;
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
public class TeacherClassServiceImpl implements TeacherClassService {

    private final SysUserRepository sysUserRepository;
    private final TeacherClassRepository teacherClassRepository;

    public TeacherClassServiceImpl(SysUserRepository sysUserRepository, TeacherClassRepository teacherClassRepository) {
        this.sysUserRepository = sysUserRepository;
        this.teacherClassRepository = teacherClassRepository;
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
        Map<String, List<TeacherClass>> classMap = teacherClassRepository.findAll().stream()
                .collect(Collectors.groupingBy(TeacherClass::getTeacherId));
        records.forEach(record -> {
            String userId = asString(record.get("userId"));
            List<TeacherClass> teacherClasses = classMap.getOrDefault(userId, new ArrayList<>());
            List<String> classIds = teacherClasses.stream().map(TeacherClass::getClassId).collect(Collectors.toList());
            record.put("classIds", classIds);
            record.put("classCount", classIds.size());
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
        List<TeacherClass> teacherClasses = teacherClassRepository.findByTeacherId(teacherId);
        view.put("classIds", teacherClasses.stream().map(TeacherClass::getClassId).collect(Collectors.toList()));
        return view;
    }

    @Override
    @Transactional
    public void save(String teacherId, Map<String, Object> payload, String currentUserId, String currentRootOrgId) {
        SysUser user = sysUserRepository.findById(teacherId).orElseThrow(() -> new RuntimeException("教师不存在"));
        if (!StringUtils.hasText(currentRootOrgId) || !currentRootOrgId.equals(user.getRootOrgId())) {
            throw new RuntimeException("只能为同校教师分配授课班级");
        }
        if (!"teacher".equalsIgnoreCase(asString(user.getUserRoleId())) && !"Teacher".equals(asString(user.getUserRoleId()))) {
            throw new RuntimeException("只能为教师分配授课班级");
        }
        if (!"y".equalsIgnoreCase(asString(user.getStatus()))) {
            throw new RuntimeException("只能为启用状态教师分配授课班级");
        }
        List<String> classIds = toStringList(payload.get("classIds"));
        if (classIds.isEmpty()) {
            String single = asString(payload.get("classId"));
            if (StringUtils.hasText(single)) classIds.add(single);
        }
        if (classIds.isEmpty()) {
            throw new RuntimeException("请选择班级");
        }
        teacherClassRepository.deleteByTeacherId(teacherId);
        Date now = new Date();
        for (String classId : classIds) {
            TeacherClass entity = new TeacherClass();
            entity.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            entity.setTeacherId(teacherId);
            entity.setClassId(classId);
            teacherClassRepository.save(entity);
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

    private String asString(Object value) { return value == null ? null : String.valueOf(value).trim(); }
    private List<String> toStringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List) for (Object item : (List<?>) value) { String text = asString(item); if (StringUtils.hasText(text)) result.add(text); }
        return result;
    }
}
