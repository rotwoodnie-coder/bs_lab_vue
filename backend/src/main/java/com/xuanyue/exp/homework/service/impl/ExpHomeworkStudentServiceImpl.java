package com.xuanyue.exp.homework.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.homework.entity.ExpHomeworkStudent;
import com.xuanyue.exp.homework.repository.ExpHomeworkStudentRepository;
import com.xuanyue.exp.homework.service.ExpHomeworkStudentService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpHomeworkStudentServiceImpl implements ExpHomeworkStudentService {

    private final ExpHomeworkStudentRepository repository;

    public ExpHomeworkStudentServiceImpl(ExpHomeworkStudentRepository repository) {
        this.repository = repository;
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
        return new PageResult<ExpHomeworkStudent>(filtered.size(), records);
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
    public void update(String seqId, Map<String, Object> payload) {
        ExpHomeworkStudent item = repository.findById(seqId).orElseThrow(() -> new RuntimeException("记录不存在"));
        applyPayload(item, payload, false);
        repository.save(item);
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
}
