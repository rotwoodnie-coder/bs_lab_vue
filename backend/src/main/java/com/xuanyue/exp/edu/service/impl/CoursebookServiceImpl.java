package com.xuanyue.exp.edu.service.impl;

import com.xuanyue.exp.common.storage.FileStorageService;
import com.xuanyue.exp.edu.entity.DataCoursebook;
import com.xuanyue.exp.edu.repository.DataCoursebookRepository;
import com.xuanyue.exp.edu.service.CoursebookService;
import org.springframework.data.domain.PageRequest;
import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CoursebookServiceImpl implements CoursebookService {

    private final DataCoursebookRepository repository;
    private final FileStorageService fileStorageService;
    private final MinioStorageService minioStorageService;

    public CoursebookServiceImpl(DataCoursebookRepository repository, FileStorageService fileStorageService,MinioStorageService minioStorageService) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
        this.minioStorageService = minioStorageService;
    }

    @Override
    public List<Map<String, Object>> list(String keyword, String editionId, String subjectId, String gradeId) {
        List<Map<String, Object>> records =  repository.findAll(spec(keyword, editionId, subjectId, gradeId)).stream().map(this::toMap).collect(Collectors.toList());
        buildPreviewUrlMap(records);
        return records;
    }

    @Override
    public Object get(String id) {
        DataCoursebook entity = repository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        if(entity.getFileUrl() != null){
            entity.setPreviewUrl(minioStorageService.buildPreviewUrl(entity.getFileUrl()));
        }
        return toMap(entity);
    }

    @Override
    public DataCoursebook findById(String id) {
        DataCoursebook entity = repository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        if(entity.getFileUrl() != null){
            entity.setPreviewUrl(minioStorageService.buildPreviewUrl(entity.getFileUrl()));
        }
        return entity;
    }

    @Override
    public void create(Map<String, Object> payload) {
        DataCoursebook entity = new DataCoursebook();
        entity.setCoursebookId(readId(payload.get("id")));
        applyPayload(entity, payload);
        repository.save(entity);
    }

    @Override
    public void update(String id, Map<String, Object> payload) {
        DataCoursebook entity = repository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        applyPayload(entity, payload);
        entity.setCoursebookId(id);
        repository.save(entity);
    }

    @Override
    public void delete(String id) {
        DataCoursebook entity = repository.findById(id).orElse(null);
        if (entity != null) {
            fileStorageService.deleteByUrl(entity.getFileUrl());
            repository.deleteById(id);
        }
    }

    @Override
    public String getFileUrl(String id) {
        DataCoursebook entity = repository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        return entity.getFileUrl();
    }

    @Override
    public List<Map<String, Object>> page(int pageNum, int pageSize, String keyword, String editionId, String subjectId, String gradeId) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize, Sort.by(Sort.Direction.ASC, "coursebookName"));
        List<Map<String, Object>> records = repository.findAll(spec(keyword, editionId, subjectId, gradeId), pageable).getContent().stream().map(this::toMap).collect(Collectors.toList());
        buildPreviewUrlMap(records);
        return records;
    }

    private void buildPreviewUrlMap(List<Map<String, Object>> records) {
        for(Map<String, Object> record : records) {
            if(record.containsKey("fileUrl")) {
                System.out.println("fileUrl: " + record.get("fileUrl"));
                record.put("previewUrl", minioStorageService.buildPreviewUrl(record.get("fileUrl").toString()));
            }
        }
    }

    @Override
    public long count(String keyword, String editionId, String subjectId, String gradeId) {
        return repository.count(spec(keyword, editionId, subjectId, gradeId));
    }

    private Specification<DataCoursebook> spec(String keyword, String editionId, String subjectId, String gradeId) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicate = cb.and(predicate, cb.like(root.get("coursebookName"), like));
            }
            if (StringUtils.hasText(editionId)) {
                predicate = cb.and(predicate, cb.equal(root.get("editionId"), editionId.trim()));
            }
            if (StringUtils.hasText(subjectId)) {
                predicate = cb.and(predicate, cb.equal(root.get("subjectId"), subjectId.trim()));
            }
            if (StringUtils.hasText(gradeId)) {
                predicate = cb.and(predicate, cb.equal(root.get("gradeId"), gradeId.trim()));
            }
            query.orderBy(cb.asc(root.get("coursebookName")));
            return predicate;
        };
    }

    private void applyPayload(DataCoursebook entity, Map<String, Object> payload) {
        entity.setCoursebookName(asString(payload.get("coursebookName")));
        entity.setEditionId(readRequired(payload.get("editionId"), "教材版本不能为空"));
        entity.setSubjectId(readRequired(payload.get("subjectId"), "学科不能为空"));
        entity.setLevelId(readRequired(payload.get("levelId"), "学段不能为空"));
        entity.setGradeId(readRequired(payload.get("gradeId"), "年级不能为空"));
        entity.setSemesterId(readRequired(payload.get("semesterId"), "学期不能为空"));
        entity.setFileUrl(asString(payload.get("fileUrl")));
        entity.setComments(asString(payload.get("comments")));
        entity.setStatus(defaultStatus(payload.get("status")));
    }

    private Map<String, Object> toMap(DataCoursebook entity) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("coursebookId", entity.getCoursebookId());
        map.put("coursebookName", entity.getCoursebookName());
        map.put("editionId", entity.getEditionId());
        map.put("subjectId", entity.getSubjectId());
        map.put("levelId", entity.getLevelId());
        map.put("gradeId", entity.getGradeId());
        map.put("semesterId", entity.getSemesterId());
        map.put("fileUrl", entity.getFileUrl());
        map.put("comments", entity.getComments());
        map.put("status", entity.getStatus());
        return map;
    }

    private String readRequired(Object value, String message) {
        String result = asString(value);
        if (!StringUtils.hasText(result)) {
            throw new RuntimeException(message);
        }
        return result;
    }

    private String readId(Object value) {
        String id = asString(value);
        if (!StringUtils.hasText(id)) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return id;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String defaultStatus(Object value) {
        String status = asString(value);
        return StringUtils.hasText(status) ? status : "y";
    }
}
