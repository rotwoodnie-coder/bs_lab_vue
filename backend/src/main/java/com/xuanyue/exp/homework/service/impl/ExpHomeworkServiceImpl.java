package com.xuanyue.exp.homework.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.edu.repository.TeacherClassRepository;
import com.xuanyue.exp.homework.entity.ExpHomework;
import com.xuanyue.exp.homework.repository.ExpHomeworkRepository;
import com.xuanyue.exp.homework.repository.ExpHomeworkStudentRepository;
import com.xuanyue.exp.homework.entity.ExpHomeworkStudent;
import com.xuanyue.exp.homework.service.ExpHomeworkService;
import com.xuanyue.exp.exp.entity.ExpSimulator;
import com.xuanyue.exp.exp.repository.ExpSimulatorRepository;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.system.repository.SysMsgRepository;
import com.xuanyue.exp.system.entity.SysMsg;
import com.xuanyue.exp.exp.entity.ExpMsg;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpHomeworkServiceImpl implements ExpHomeworkService {

    private final ExpHomeworkRepository repository;
    private final ExpSimulatorRepository expSimulatorRepository;
    private final SysUserRepository sysUserRepository;
    private final TeacherClassRepository teacherClassRepository;
    private final SysOrgRepository sysOrgRepository;
    private final ExpMsgRepository expMsgRepository;
    private final SysMsgRepository sysMsgRepository;
    private final ExpHomeworkStudentRepository expHomeworkStudentRepository;

    public ExpHomeworkServiceImpl(ExpHomeworkRepository repository,
                                  ExpSimulatorRepository expSimulatorRepository,
                                  SysUserRepository sysUserRepository,
                                  TeacherClassRepository teacherClassRepository,
                                  SysOrgRepository sysOrgRepository,
                                  ExpMsgRepository expMsgRepository,
                                  SysMsgRepository sysMsgRepository,
                                  ExpHomeworkStudentRepository expHomeworkStudentRepository) {
        this.repository = repository;
        this.expSimulatorRepository = expSimulatorRepository;
        this.sysUserRepository = sysUserRepository;
        this.teacherClassRepository = teacherClassRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.expMsgRepository = expMsgRepository;
        this.sysMsgRepository = sysMsgRepository;
        this.expHomeworkStudentRepository = expHomeworkStudentRepository;
    }

    @Override
    public PageResult<Map<String, Object>> page(String keyword, String teacherExpId, String teacherUserId, String classId, String requireDate, int pageNum, int pageSize) {
        String kw = asString(keyword);
        String expId = asString(teacherExpId);
        String userId = asString(teacherUserId);
        String clsId = asString(classId);
        String reqDate = asString(requireDate);

        List<ExpHomework> filtered = repository.findAll(Sort.by(Sort.Direction.DESC, "createTime")).stream()
                .filter(item -> !StringUtils.hasText(kw)
                        || containsIgnoreCase(item.getHomeworkId(), kw)
                        || containsIgnoreCase(item.getTeacherExpId(), kw)
                        || containsIgnoreCase(item.getTearcherUserId(), kw)
                        || containsIgnoreCase(item.getClassId(), kw)
                        || containsIgnoreCase(item.getRequireDate(), kw))
                .filter(item -> !StringUtils.hasText(expId) || expId.equals(item.getTeacherExpId()))
                .filter(item -> !StringUtils.hasText(userId) || userId.equals(item.getTearcherUserId()))
                .filter(item -> !StringUtils.hasText(clsId) || clsId.equals(item.getClassId()))
                .filter(item -> !StringUtils.hasText(reqDate) || reqDate.equals(item.getRequireDate()))
                .collect(Collectors.toList());

        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, filtered.size());
        int toIndex = Math.min(fromIndex + safePageSize, filtered.size());
        List<Map<String, Object>> records = filtered.subList(fromIndex, toIndex).stream()
                .map(this::toView)
                .collect(Collectors.toList());
       

         PageResult<Map<String, Object>> page = new PageResult<>(filtered.size(), records);
         List<Map<String, Object>> recordsnew =page.getRecords();
          recordsnew.forEach(record -> {
            String tempteacherExpId = asString(record.get("teacherExpId"));
            ExpMsg expMsg = StringUtils.hasText(tempteacherExpId) ? expMsgRepository.findById(tempteacherExpId).orElse(null) : null;
            if (expMsg != null) {
                record.put("teacherExpName", expMsg.getExpName());
            }
            String tempteacherUserId = asString(record.get("tearcherUserId"));
            SysUser teacherUser = StringUtils.hasText(tempteacherUserId) ? sysUserRepository.findById(tempteacherUserId).orElse(null) : null;
            if (teacherUser != null) {
                record.put("teacherUserName", teacherUser.getUserName());
            }
            String tempclassId = asString(record.get("classId"));
            SysOrg classOrg = StringUtils.hasText(tempclassId) ? sysOrgRepository.findById(tempclassId).orElse(null) : null;
            if (classOrg != null) {
                record.put("className", classOrg.getOrgName());
            }
        });
         return page;
    }

    @Override
    public ExpHomework get(String homeworkId) {
        return repository.findById(homeworkId).orElseThrow(() -> new RuntimeException("记录不存在"));
    }

    @Override
    public void create(Map<String, Object> payload) {
        ExpHomework item = new ExpHomework();
        item.setHomeworkId(UUID.randomUUID().toString().replace("-", ""));
        applyPayload(item, payload, true);
        validateUnique(item, null);
        repository.save(item);
    }

    @Override
    @Transactional
    public void update(String homeworkId, Map<String, Object> payload) {
        ExpHomework item = repository.findById(homeworkId).orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"c".equalsIgnoreCase(asString(item.getStatus()))) {
            throw new RuntimeException("只有待布置状态的作业才允许编辑");
        }
        applyPayload(item, payload, false);
        validateUnique(item, homeworkId);
        repository.save(item);
    }

    @Override
    @Transactional
    public void assign(String homeworkId) {
        ExpHomework item = repository.findById(homeworkId).orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"c".equalsIgnoreCase(asString(item.getStatus()))) {
            throw new RuntimeException("只有待布置状态的作业才能布置");
        }
        item.setStatus("y");
        repository.save(item);

        //实验信息
       ExpMsg expMsg = expMsgRepository.findById(item.getTeacherExpId()).orElse(null);
       String expName = expMsg != null ? expMsg.getExpName() : null;

        //生成学生作业及发送消息
        List<SysUser> studentList = sysUserRepository.findByUserOrgIdAndStatusAndUserRoleId(item.getClassId(),"y","Student");
        for(SysUser student : studentList) {
            ExpHomeworkStudent studentHomework = new ExpHomeworkStudent();
            studentHomework.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            studentHomework.setHomeworkId(homeworkId);
            studentHomework.setTeacherExpId(item.getTeacherExpId());
            studentHomework.setTeacherUserId(item.getTearcherUserId());
            studentHomework.setRequireDate(item.getRequireDate());
            studentHomework.setStudentUserId(student.getUserId());
            expHomeworkStudentRepository.save(studentHomework);

            //msg
            SysMsg msg = new SysMsg();
            msg.setMsgId(UUID.randomUUID().toString().replace("-", ""));
            msg.setReceiverUserId(student.getUserId());
            msg.setSenderUserId(item.getTearcherUserId());
            msg.setMsgTypeId("Msg_Homework");
            msg.setMsgContent(expName + "作业已布置，请及时完成，截止时间：" + item.getRequireDate());
            msg.setSendTime(new Date());
            msg.setReadTag("n");
            msg.setLinkId(studentHomework.getSeqId());
            sysMsgRepository.save(msg);
        }
    }

    @Override
    public void delete(String homeworkId) {
        repository.deleteById(homeworkId);
    }

    private void applyPayload(ExpHomework item, Map<String, Object> payload, boolean isCreate) {
        item.setTeacherExpId(asString(payload.get("teacherExpId")));
        item.setTearcherUserId(asString(payload.get("tearcherUserId"), payload.get("teacherUserId"), payload.get("teacher_user_id"), payload.get("tearcher_user_id")));
        item.setClassId(asString(payload.get("classId"), payload.get("class_id")));
        item.setRequireDate(asString(payload.get("requireDate"), payload.get("require_date")));
        item.setStatus(asString(payload.get("status"), payload.get("status")));
        if (isCreate) {
            item.setCreateTime(new Date());
        }
    }

    private void validateUnique(ExpHomework item, String excludeHomeworkId) {
        String teacherExpId = asString(item.getTeacherExpId());
        String tearcherUserId = asString(item.getTearcherUserId());
        String classId = asString(item.getClassId());
        if (!StringUtils.hasText(teacherExpId) || !StringUtils.hasText(tearcherUserId) || !StringUtils.hasText(classId)) {
            return;
        }
        boolean exists = repository.findAll().stream()
                .filter(row -> !StringUtils.hasText(excludeHomeworkId) || !excludeHomeworkId.equals(row.getHomeworkId()))
                .anyMatch(row -> teacherExpId.equals(asString(row.getTeacherExpId()))
                        && tearcherUserId.equals(asString(row.getTearcherUserId()))
                        && classId.equals(asString(row.getClassId())));
        if (exists) {
            throw new RuntimeException("同一教师、实验、班级的作业已存在，请勿重复添加");
        }
    }

    private Map<String, Object> toView(ExpHomework item) {
        Map<String, Object> view = new HashMap<>();
        view.put("homeworkId", item.getHomeworkId());
        view.put("teacherExpId", item.getTeacherExpId());
        view.put("tearcherUserId", item.getTearcherUserId());
        view.put("classId", item.getClassId());
        view.put("requireDate", item.getRequireDate());
        view.put("status", item.getStatus());
        view.put("createTime", item.getCreateTime());
        /*
        String teacherName = null;
        String expName = null;
        String className = null;
        String teacherUserId = asString(item.getTearcherUserId());
        String classId = asString(item.getClassId());
        String expId = asString(item.getTeacherExpId());

        if (StringUtils.hasText(teacherUserId)) {
            teacherName = sysUserRepository.findById(teacherUserId).map(SysUser::getUserName).orElse(null);
        }
        if (StringUtils.hasText(expId)) {
            expName = expSimulatorRepository.findById(expId).map(ExpSimulator::getSimulatorName).orElse(null);
        }
        if (StringUtils.hasText(classId)) {
            className = sysOrgRepository.findById(classId).map(SysOrg::getOrgName).orElse(null);
            if (!StringUtils.hasText(className)) {
                className = teacherClassRepository.findById(classId).map(TeacherClass::getClassId).orElse(null);
            }
        }
        if (!StringUtils.hasText(className) && StringUtils.hasText(classId)) {
            className = classId;
        }

        view.put("teacherName", teacherName);
        view.put("expName", expName);
        view.put("className", className);
        */
        return view;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private String asString(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (StringUtils.hasText(text)) {
                return text;
            }
        }
        return null;
    }
}
