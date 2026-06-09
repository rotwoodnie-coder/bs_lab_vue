package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.exp.entity.ExpQuestion;
import com.xuanyue.exp.exp.repository.ExpQuestionRepository;
import com.xuanyue.exp.exp.service.ExpQuestionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpQuestionServiceImpl implements ExpQuestionService {

    private final ExpQuestionRepository repository;

    public ExpQuestionServiceImpl(ExpQuestionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>> list(String keyword, String status, String questionTypeId, String gradeId, String subjectId) {
        return repository.findAll(spec(keyword, status, questionTypeId, gradeId, subjectId)).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> page(int pageNum, int pageSize, String keyword, String status, String questionTypeId, String gradeId, String subjectId) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        return repository.findAll(spec(keyword, status, questionTypeId, gradeId, subjectId), pageable).getContent().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, String status, String questionTypeId, String gradeId, String subjectId) {
        return repository.count(spec(keyword, status, questionTypeId, gradeId, subjectId));
    }

    @Override
    public Map<String, Object> get(String id) {
        Optional<ExpQuestion> optional = repository.findById(id);
        return optional.map(this::toMap).orElse(null);
    }

    @Override
    @Transactional
    public String create(Map<String, Object> payload) {
        ExpQuestion entity = new ExpQuestion();
        entity.setQuestionId(UUID.randomUUID().toString().replace("-", ""));
        copy(entity, payload);
        if (entity.getStatus() == null || entity.getStatus().trim().isEmpty()) {
            entity.setStatus("t");
        }
        String currentUserId = getCurrentUserId();
        entity.setCreateUserId(currentUserId);
        entity.setUpdateUserId(currentUserId);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        repository.save(entity);
        return entity.getQuestionId();
    }

    @Override
    @Transactional
    public void update(String id, Map<String, Object> payload) {
        ExpQuestion entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("题目不存在"));
        copy(entity, payload);
        entity.setUpdateUserId(getCurrentUserId());
        entity.setUpdateTime(new Date());
        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(String id) {
        repository.deleteById(id);
    }

    private void copy(ExpQuestion entity, Map<String, Object> payload) {
        entity.setQuestionContent(asString(payload.get("questionContent"), payload.get("question_content"), entity.getQuestionContent()));
        entity.setQuestionTypeId(asString(payload.get("questionTypeId"), payload.get("question_type_id"), entity.getQuestionTypeId()));
        entity.setDifficultyTypeId(asString(payload.get("difficultyTypeId"), payload.get("difficulty_type_id"), entity.getDifficultyTypeId()));
        entity.setQuestionCapacityId(asString(payload.get("questionCapacityId"), payload.get("question_capacity_id"), entity.getQuestionCapacityId()));
        entity.setGradeId(asString(payload.get("gradeId"), payload.get("grade_id"), entity.getGradeId()));
        entity.setSubjectId(asString(payload.get("subjectId"), payload.get("subject_id"), entity.getSubjectId()));
        entity.setCoursebookId(asString(payload.get("coursebookId"), payload.get("coursebook_id"), entity.getCoursebookId()));
        entity.setContentUnitId(asString(payload.get("contentUnitId"), payload.get("content_unit_id"), entity.getContentUnitId()));
        entity.setContentChapterId(asString(payload.get("contentChapterId"), payload.get("content_chapter_id"), entity.getContentChapterId()));
        entity.setContentSectionId(asString(payload.get("contentSectionId"), payload.get("content_section_id"), entity.getContentSectionId()));
        entity.setKnowledgeContent(asString(payload.get("knowledgeContent"), payload.get("knowledge_content"), entity.getKnowledgeContent()));
        entity.setStatus(asString(payload.get("status"), entity.getStatus()));
        entity.setCreateUserId(asString(payload.get("createUserId"), payload.get("create_user_id"), entity.getCreateUserId()));
        entity.setUpdateUserId(asString(payload.get("updateUserId"), payload.get("update_user_id"), entity.getUpdateUserId()));
    }

    private Specification<ExpQuestion> spec(String keyword, String status, String questionTypeId, String gradeId, String subjectId) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("questionId"), like),
                        cb.like(root.get("questionContent"), like),
                        cb.like(root.get("knowledgeContent"), like)
                ));
            }
            if (StringUtils.hasText(status)) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status.trim()));
            }
            if (StringUtils.hasText(questionTypeId)) {
                predicate = cb.and(predicate, cb.equal(root.get("questionTypeId"), questionTypeId.trim()));
            }
            if (StringUtils.hasText(gradeId)) {
                predicate = cb.and(predicate, cb.equal(root.get("gradeId"), gradeId.trim()));
            }
            if (StringUtils.hasText(subjectId)) {
                predicate = cb.and(predicate, cb.equal(root.get("subjectId"), subjectId.trim()));
            }
            query.orderBy(cb.desc(root.get("createTime")));
            return predicate;
        };
    }

    private Map<String, Object> toMap(ExpQuestion item) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("questionId", item.getQuestionId());
        result.put("questionContent", item.getQuestionContent());
        result.put("questionTypeId", item.getQuestionTypeId());
        result.put("difficultyTypeId", item.getDifficultyTypeId());
        result.put("questionCapacityId", item.getQuestionCapacityId());
        result.put("gradeId", item.getGradeId());
        result.put("subjectId", item.getSubjectId());
        result.put("coursebookId", item.getCoursebookId());
        result.put("contentUnitId", item.getContentUnitId());
        result.put("contentChapterId", item.getContentChapterId());
        result.put("contentSectionId", item.getContentSectionId());
        result.put("knowledgeContent", item.getKnowledgeContent());
        result.put("status", item.getStatus());
        result.put("createUserId", item.getCreateUserId());
        result.put("createTime", item.getCreateTime());
        result.put("updateUserId", item.getUpdateUserId());
        result.put("updateTime", item.getUpdateTime());
        return result;
    }

    private boolean match(String keyword, String... values) {
        if (empty(keyword)) return true;
        String lower = keyword.toLowerCase();
        for (String value : values) {
            if (value != null && value.toLowerCase().contains(lower)) return true;
        }
        return false;
    }

    private boolean empty(String value) {
        return value == null || value.trim().isEmpty();
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

    private String asString(Object... values) {
        for (Object value : values) {
            if (value != null) {
                String s = String.valueOf(value).trim();
                if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) return s;
            }
        }
        return null;
    }
}
