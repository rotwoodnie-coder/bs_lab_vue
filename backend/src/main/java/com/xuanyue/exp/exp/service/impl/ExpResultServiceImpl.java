package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.exp.entity.ExpResult;
import com.xuanyue.exp.exp.repository.ExpResultRepository;
import com.xuanyue.exp.exp.service.ExpResultService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExpResultServiceImpl implements ExpResultService {

    private final ExpResultRepository repository;

    public ExpResultServiceImpl(ExpResultRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<?> listByExpId(String expId) {
        return repository.findByExpIdOrderBySortOrderAsc(expId);
    }

    @Override
    @Transactional
    public List<?> saveBatch(String expId, List<Map<String, Object>> results) {
        repository.deleteByExpId(expId);
        List<ExpResult> saved = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            saved.add(buildAndSave(expId, results.get(i), i + 1));
        }
        return saved;
    }

    @Override
    @Transactional
    public String saveOne(String expId, Map<String, Object> result) {
        ExpResult entity = buildAndSave(expId, result, parseOrder(result.get("sortOrder"), 1));
        return entity.getResultId();
    }

    @Override
    @Transactional
    public void delete(String resultId) {
        repository.deleteById(resultId);
    }

    private ExpResult buildAndSave(String expId, Map<String, Object> result, Integer sortOrder) {
        String resultId = null;
        if (result.containsKey("resultId") && result.get("resultId") != null
                && !result.get("resultId").toString().isEmpty()) {
            resultId = asString(result.get("resultId"));
        }
        ExpResult entity = (resultId != null)
                ? repository.findById(resultId).orElseGet(ExpResult::new)
                : new ExpResult();
        if (!StringUtils.hasText(entity.getResultId())) {
            entity.setResultId(UUID.randomUUID().toString().replace("-", ""));
        }
        entity.setExpId(expId);
        entity.setResultName(asString(result.get("resultName")));
        entity.setResultComments(asString(result.get("resultComments")));
        entity.setSortOrder(sortOrder);
        return repository.save(entity);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Integer parseOrder(Object value, Integer defaultValue) {
        String text = asString(value);
        if (!StringUtils.hasText(text)) return defaultValue;
        return Integer.valueOf(text);
    }
}
