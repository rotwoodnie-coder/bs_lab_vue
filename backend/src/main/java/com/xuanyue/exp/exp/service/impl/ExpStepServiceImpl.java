package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.exp.entity.ExpStep;
import com.xuanyue.exp.exp.repository.ExpStepRepository;
import com.xuanyue.exp.exp.service.ExpStepService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExpStepServiceImpl implements ExpStepService {

    private final ExpStepRepository repository;

    public ExpStepServiceImpl(ExpStepRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<?> listByExpId(String expId) {
        return repository.findByExpIdOrderBySortOrderAsc(expId);
    }

    @Override
    @Transactional
    public List<?> saveBatch(String expId, List<Map<String, Object>> steps) {
        repository.deleteByExpId(expId);
        List<ExpStep> saved = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            saved.add(buildAndSave(expId, steps.get(i), i + 1));
        }
        return saved;
    }

    @Override
    @Transactional
    public String saveOne(String expId, Map<String, Object> step) {
        ExpStep entity = buildAndSave(expId, step, parseOrder(step.get("sortOrder"), 1));
        return entity.getStepId();
    }

    @Override
    @Transactional
    public void delete(String stepId) {
        repository.deleteById(stepId);
    }

    private ExpStep buildAndSave(String expId, Map<String, Object> step, Integer sortOrder) {
        ExpStep entity = new ExpStep();
        if (step.containsKey("stepId") && step.get("stepId") != null 
           && !step.get("stepId").toString().isEmpty()) {
            entity.setStepId(asString(step.get("stepId")));
        } else {
            entity.setStepId(UUID.randomUUID().toString().replace("-", ""));
        }
        entity.setExpId(expId);
        entity.setStepName(asString(step.get("stepName")));
        entity.setStepComments(asString(step.get("stepComments")));
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
