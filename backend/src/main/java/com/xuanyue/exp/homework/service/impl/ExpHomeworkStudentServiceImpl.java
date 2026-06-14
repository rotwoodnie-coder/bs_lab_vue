package com.xuanyue.exp.homework.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.homework.entity.ExpHomeworkStudent;
import com.xuanyue.exp.homework.repository.ExpHomeworkStudentRepository;
import com.xuanyue.exp.homework.service.ExpHomeworkStudentService;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import com.xuanyue.exp.exp.repository.ExpLogRepository;
import com.xuanyue.exp.system.entity.SysUser; 
import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.entity.ExpLog;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpHomeworkStudentServiceImpl implements ExpHomeworkStudentService {

    private final ExpHomeworkStudentRepository repository;
    private final ExpMsgRepository expMsgRepository;
    private final SysUserRepository sysUserRepository;  
    private final ExpLogRepository logRepository;

    public ExpHomeworkStudentServiceImpl(ExpHomeworkStudentRepository repository, 
                          ExpMsgRepository expMsgRepository, 
                          SysUserRepository sysUserRepository,
                          ExpLogRepository logRepository) {
        this.repository = repository;
        this.expMsgRepository = expMsgRepository;
        this.sysUserRepository = sysUserRepository;
        this.logRepository = logRepository;
    }

    @Override
    public PageResult<ExpHomeworkStudent> page(String keyword, String homeworkId, String teacherExpId, String teacherUserId, String studentExpId, int pageNum, int pageSize) {
        String kw = asString(keyword);
        String hId = asString(homeworkId);
        String tExpId = asString(teacherExpId);
        String tUserId = asString(teacherUserId);
        String sExpId = asString(studentExpId);

        List<ExpHomeworkStudent> filtered = repository.findAll(Sort.by(Sort.Direction.DESC, "markTime")).stream()
                .filter(item -> !StringUtils.hasText(kw)
                        || containsIgnoreCase(item.getSeqId(), kw)
                        || containsIgnoreCase(item.getHomeworkId(), kw)
                        || containsIgnoreCase(item.getTeacherExpId(), kw)
                        || containsIgnoreCase(item.getTeacherUserId(), kw)
                        || containsIgnoreCase(item.getStudentExpId(), kw)
                        || containsIgnoreCase(item.getMarkComments(), kw)
                        || containsIgnoreCase(item.getMarkResult(), kw))
                .filter(item -> !StringUtils.hasText(hId) || hId.equals(item.getHomeworkId()))
                .filter(item -> !StringUtils.hasText(tExpId) || tExpId.equals(item.getTeacherExpId()))
                .filter(item -> !StringUtils.hasText(tUserId) || tUserId.equals(item.getTeacherUserId()))
                .filter(item -> !StringUtils.hasText(sExpId) || sExpId.equals(item.getStudentExpId()))
                .collect(Collectors.toList());

        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, filtered.size());
        int toIndex = Math.min(fromIndex + safePageSize, filtered.size());
        List<ExpHomeworkStudent> records = new ArrayList<ExpHomeworkStudent>(filtered.subList(fromIndex, toIndex));
        PageResult<ExpHomeworkStudent> page = new PageResult<ExpHomeworkStudent>(filtered.size(), records);
        List<ExpHomeworkStudent> recordsnew = page.getRecords();
        recordsnew.forEach(record -> {
            String tempteacherExpId = asString(record.getTeacherExpId());
            ExpMsg TeacherExpMsg = StringUtils.hasText(tempteacherExpId) ? expMsgRepository.findById(tempteacherExpId).orElse(null) : null;
            if (TeacherExpMsg != null) {
                record.setTeacherExpName(TeacherExpMsg.getExpName());
            }
            String tempteacherUserId = asString(record.getTeacherUserId());
            SysUser teacherUser = StringUtils.hasText(tempteacherUserId) ? sysUserRepository.findById(tempteacherUserId).orElse(null) : null;
            if (teacherUser != null) {
                record.setTeacherUserName(teacherUser.getUserName());
            }
            String tempstudentExpId = asString(record.getStudentExpId());
            ExpMsg studentExpMsg = StringUtils.hasText(tempstudentExpId) ? expMsgRepository.findById(tempstudentExpId).orElse(null) : null;
            if (studentExpMsg != null) {
                record.setStudentExpName(studentExpMsg.getExpName());
            }
            String tempstudentUserId = asString(record.getStudentUserId());
            SysUser studentUser = StringUtils.hasText(tempstudentUserId) ? sysUserRepository.findById(tempstudentUserId).orElse(null) : null;
            if (studentUser != null) {
                record.setStudentUserName(studentUser.getUserName());
            }
        });
        return page;
    }

    @Override
    public ExpHomeworkStudent get(String seqId) {
        return repository.findById(seqId).orElseThrow(() -> new RuntimeException("记录不存在"));
    }

    @Override
    public void create(Map<String, Object> payload) {
        ExpHomeworkStudent item = new ExpHomeworkStudent();
        item.setSeqId(UUID.randomUUID().toString().replace("-", ""));
        applyPayload(item, payload, true);
        repository.save(item);
    }

    @Override
    @Transactional
    public void update(String seqId, Map<String, Object> payload) {
        ExpHomeworkStudent item = repository.findById(seqId).orElseThrow(() -> new RuntimeException("记录不存在"));
        applyPayload(item, payload, false);
        item.setMarkTime(new Date());
        repository.save(item);

        //log
        String currentUserId = getCurrentUserId();
        ExpLog expLog = new ExpLog();
        expLog.setLogId(UUID.randomUUID().toString().replace("-", ""));
        expLog.setExpId(item.getStudentExpId());
        expLog.setLogType("mark");
        expLog.setLogTypeName("批改");
        expLog.setLogTime(new Date());
        expLog.setLogUserId(currentUserId);
        expLog.setLogUserName(resolveUserName(currentUserId));
        logRepository.save(expLog);
    }

    @Override
    public void delete(String seqId) {
        repository.deleteById(seqId);
    }

    private void applyPayload(ExpHomeworkStudent item, Map<String, Object> payload, boolean isCreate) {
        item.setHomeworkId(asString(payload.get("homeworkId"), payload.get("homework_id")));
        item.setTeacherExpId(asString(payload.get("teacherExpId"), payload.get("teacher_exp_id")));
        item.setTeacherUserId(asString(payload.get("teacherUserId"), payload.get("teacher_user_id")));
        item.setRequireDate(asString(payload.get("requireDate"), payload.get("require_date")));
        item.setStudentExpId(asString(payload.get("studentExpId"), payload.get("student_exp_id")));
        item.setStudentSubmitDate(asString(payload.get("studentSubmitDate"), payload.get("student_submit_date")));
        item.setMarkUserId(asString(payload.get("markUserId"), payload.get("mark_user_id")));
        item.setMarkComments(asString(payload.get("markComments"), payload.get("mark_comments")));
        item.setMarkResult(asString(payload.get("markResult"), payload.get("mark_result")));
        Object markTime = payload.get("markTime");
        if (markTime instanceof Date) {
            item.setMarkTime((Date) markTime);
        } else if (StringUtils.hasText(asString(markTime))) {
            item.setMarkTime(new Date());
        } else if (isCreate) {
            item.setMarkTime(null);
        }
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

    private String getCurrentUserId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            throw new IllegalStateException("无法获取当前登录用户");
        }
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        String userId = request.getHeader("X-User-Id");
        if (!StringUtils.hasText(userId)) {
            throw new IllegalStateException("未获取到登录用户信息");
        }
        return userId.trim();
    }

    private String resolveUserName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "";
        }
        return sysUserRepository.findById(userId)
                .map(user -> StringUtils.hasText(user.getUserName()) ? user.getUserName() : user.getLoginName())
                .orElse(userId);
    }
}
