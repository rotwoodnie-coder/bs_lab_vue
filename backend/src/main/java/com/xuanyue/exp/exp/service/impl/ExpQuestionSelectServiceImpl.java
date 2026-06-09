package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.exp.entity.ExpQuestionSelect;
import com.xuanyue.exp.exp.repository.ExpQuestionSelectRepository;
import com.xuanyue.exp.exp.service.ExpQuestionSelectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpQuestionSelectServiceImpl implements ExpQuestionSelectService {

    private final ExpQuestionSelectRepository repository;

    public ExpQuestionSelectServiceImpl(ExpQuestionSelectRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>> list(String questionId) {
        return repository.findByQuestionIdOrderBySortOrderAscSelectIdAsc(questionId).stream().map(this::toMap).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveBatch(String questionId, List<Map<String, Object>> selects) {
        repository.deleteByQuestionId(questionId);
        if (selects == null) return;
        int sort = 1;
        for (Map<String, Object> item : selects) {
            String content = asString(item.get("selectContent"), item.get("select_content"));
            if (!StringUtils.hasText(content)) continue;
            ExpQuestionSelect entity = new ExpQuestionSelect();
            entity.setSelectId(readId(item.get("selectId"), item.get("select_id")));
            entity.setQuestionId(questionId);
            entity.setSelectContent(content);
            entity.setSortOrder(readInt(item.get("sortOrder"), item.get("sort_order"), sort));
            entity.setIsRight(defaultRight(item.get("isRight"), item.get("is_right")));
            repository.save(entity);
            sort++;
        }
    }

    @Override
    public void delete(String selectId) {
        repository.deleteById(selectId);
    }

    private Map<String, Object> toMap(ExpQuestionSelect entity) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("selectId", entity.getSelectId());
        map.put("questionId", entity.getQuestionId());
        map.put("selectContent", entity.getSelectContent());
        map.put("sortOrder", entity.getSortOrder());
        map.put("isRight", entity.getIsRight());
        return map;
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

    private String readId(Object... values) {
        String id = asString(values);
        return StringUtils.hasText(id) ? id : UUID.randomUUID().toString().replace("-", "");
    }

    private Integer readInt(Object v1, Object v2, int defaultValue) {
        String s = asString(v1, v2);
        if (!StringUtils.hasText(s)) return defaultValue;
        try { return Integer.parseInt(s); } catch (Exception e) { return defaultValue; }
    }

    private String defaultRight(Object... values) {
        String s = asString(values);
        return StringUtils.hasText(s) ? s : "n";
    }
}
