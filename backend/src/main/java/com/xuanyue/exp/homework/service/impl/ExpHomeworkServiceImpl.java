package com.xuanyue.exp.homework.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.homework.entity.ExpHomework;
import com.xuanyue.exp.homework.repository.ExpHomeworkRepository;
import com.xuanyue.exp.homework.service.ExpHomeworkService;
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
public class ExpHomeworkServiceImpl implements ExpHomeworkService {

    private final ExpHomeworkRepository repository;

    public ExpHomeworkServiceImpl(ExpHomeworkRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<ExpHomework> page(String keyword, String teacherExpId, String tearcherUserId, String classId, String requireDate, int pageNum, int pageSize) {
        String kw = asString(keyword);
        String expId = asString(teacherExpId);
        String userId = asString(tearcherUserId);
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
        List<ExpHomework> records = new ArrayList<ExpHomework>(filtered.subList(fromIndex, toIndex));
        return new PageResult<ExpHomework>(filtered.size(), records);
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
        repository.save(item);
    }

    @Override
    public void update(String homeworkId, Map<String, Object> payload) {
        ExpHomework item = repository.findById(homeworkId).orElseThrow(() -> new RuntimeException("记录不存在"));
        applyPayload(item, payload, false);
        repository.save(item);
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
        if (isCreate) {
            item.setCreateTime(new Date());
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
