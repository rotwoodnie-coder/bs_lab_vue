package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.exp.entity.ExpScientist;
import com.xuanyue.exp.exp.repository.ExpScientistRepository;
import com.xuanyue.exp.exp.service.ExpScientistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExpScientistServiceImpl implements ExpScientistService {

    private final ExpScientistRepository repository;

    public ExpScientistServiceImpl(ExpScientistRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<?> listByExpId(String expId) {
        return repository.findByExpIdOrderBySortOrderAsc(expId);
    }

    @Override
    @Transactional
    public List<?> saveBatch(String expId, List<Map<String, Object>> scientists) {
        repository.deleteByExpId(expId);
        List<ExpScientist> saved = new ArrayList<>();
        for (int i = 0; i < scientists.size(); i++) {
            saved.add(buildAndSave(expId, scientists.get(i), i + 1));
        }
        return saved;
    }

    @Override
    @Transactional
    public Object saveOne(String expId, Map<String, Object> scientist) {
        ExpScientist entity = buildAndSave(expId, scientist, parseOrder(scientist.get("sortOrder"), 1));
        return entity;
    }

    @Override
    @Transactional
    public void delete(String scientistId) {
        repository.deleteById(scientistId);
    }

    private ExpScientist buildAndSave(String expId, Map<String, Object> scientist, Integer sortOrder) {
        String scientistId = hasText(scientist.get("scientistId")) ? asString(scientist.get("scientistId")) : null;
        ExpScientist entity = (scientistId != null)
                ? repository.findById(scientistId).orElseGet(ExpScientist::new)
                : new ExpScientist();
        if (!StringUtils.hasText(entity.getScientistId())) {
            entity.setScientistId(randomId());
        }
        entity.setExpId(expId);
        entity.setScientistName(asString(scientist.get("scientistName")));
        entity.setStoryName(asString(scientist.get("storyName")));
        entity.setStoryComments(asString(scientist.get("storyComments")));
        entity.setSortOrder(sortOrder);
        return repository.save(entity);
    }

    private String randomId() { return UUID.randomUUID().toString().replace("-", ""); }
    private boolean hasText(Object value) { return StringUtils.hasText(asString(value)); }
    private String asString(Object value) { return value == null ? null : String.valueOf(value).trim(); }
    private Integer parseOrder(Object value, Integer defaultValue) {
        String text = asString(value);
        return StringUtils.hasText(text) ? Integer.valueOf(text) : defaultValue;
    }
}
