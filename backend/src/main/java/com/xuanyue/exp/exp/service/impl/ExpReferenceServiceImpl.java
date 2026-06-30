package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.exp.entity.ExpReference;
import com.xuanyue.exp.exp.repository.ExpReferenceRepository;
import com.xuanyue.exp.exp.service.ExpReferenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExpReferenceServiceImpl implements ExpReferenceService {

    private final ExpReferenceRepository repository;

    public ExpReferenceServiceImpl(ExpReferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<?> listByExpId(String expId) {
        return repository.findByExpIdOrderBySortOrderAsc(expId);
    }

    @Override
    @Transactional
    public List<?> saveBatch(String expId, List<Map<String, Object>> references) {
        repository.deleteByExpId(expId);
        List<ExpReference> saved = new ArrayList<>();
        for (int i = 0; i < references.size(); i++) {
            saved.add(buildAndSave(expId, references.get(i), i + 1));
        }
        return saved;
    }

    @Override
    @Transactional
    public Object saveOne(String expId, Map<String, Object> reference) {
        ExpReference entity = buildAndSave(expId, reference, parseOrder(reference.get("sortOrder"), 1));
        return entity;
    }

    @Override
    @Transactional
    public void delete(String referenceId) {
        repository.deleteById(referenceId);
    }

    private ExpReference buildAndSave(String expId, Map<String, Object> reference, Integer sortOrder) {
        String referenceId = hasText(reference.get("referenceId")) ? asString(reference.get("referenceId")) : null;
        ExpReference entity = (referenceId != null)
                ? repository.findById(referenceId).orElseGet(ExpReference::new)
                : new ExpReference();
        if (!StringUtils.hasText(entity.getReferenceId())) {
            entity.setReferenceId(randomId());
        }
        entity.setExpId(expId);
        entity.setReferenceName(asString(reference.get("referenceName")));
        entity.setReferenceSource(asString(reference.get("referenceSource")));
        entity.setReferenceComments(asString(reference.get("referenceComments")));
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
