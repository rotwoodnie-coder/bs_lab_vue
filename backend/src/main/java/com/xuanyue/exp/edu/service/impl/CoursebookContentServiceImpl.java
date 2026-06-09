package com.xuanyue.exp.edu.service.impl;

import com.xuanyue.exp.edu.entity.DataCoursebookContent;
import com.xuanyue.exp.edu.repository.DataCoursebookContentRepository;
import com.xuanyue.exp.edu.service.CoursebookContentService;
import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CoursebookContentServiceImpl implements CoursebookContentService {

    private final DataCoursebookContentRepository repository;
    private final ExpMsgRepository expMsgRepository;

    public CoursebookContentServiceImpl(DataCoursebookContentRepository repository, ExpMsgRepository expMsgRepository) {
        this.repository = repository;
        this.expMsgRepository = expMsgRepository;
    }

    @Override
    public List<Map<String, Object>> listByCoursebook(String coursebookId) {
        return listByCoursebookWithTeachCount(coursebookId, null);
    }

    @Override
    public List<Map<String, Object>> listByCoursebookWithTeachCount(String coursebookId, String currentUserId) {
        List<DataCoursebookContent> contents = repository.findByCoursebookIdOrderBySortOrderAscContentNameAsc(coursebookId);
        Map<String, Long> teachCountMap = expMsgRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("expType"), "teach"),
                StringUtils.hasText(currentUserId) ? cb.equal(root.get("createUserId"), currentUserId) : cb.conjunction(),
                cb.isNotNull(root.get("sectionId"))
        )).stream().collect(Collectors.groupingBy(ExpMsg::getSectionId, Collectors.counting()));
        return contents.stream().map(entity -> {
            Map<String, Object> map = toMap(entity);
            map.put("teach_count", teachCountMap.getOrDefault(entity.getContentId(), 0L));
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public void create(Map<String, Object> payload) {
        DataCoursebookContent entity = new DataCoursebookContent();
        entity.setContentId(readId(payload.get("content_id")));
        apply(entity, payload);
        validateHierarchy(entity.getContentType(), entity.getParentId());
        repository.save(entity);
    }

    @Override
    public void update(String id, Map<String, Object> payload) {
        DataCoursebookContent entity = repository.findById(id).orElseThrow(() -> new RuntimeException("目录不存在"));
        apply(entity, payload);
        validateHierarchy(entity.getContentType(), entity.getParentId());
        entity.setContentId(id);
        repository.save(entity);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public DataCoursebookContent findById(String id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
    }

    @Override
    public void reorder(String coursebookId, List<Map<String, Object>> tree) {
        if (tree == null) return;
        List<ReorderItem> items = new ArrayList<>();
        flatten(tree, "0", 0, items);
        for (ReorderItem item : items) {
            DataCoursebookContent entity = repository.findById(item.id).orElse(null);
            if (entity != null && coursebookId.equals(entity.getCoursebookId())) {
                entity.setParentId(item.parentId);
                entity.setSortOrder(item.sortOrder);
                repository.save(entity);
            }
        }
    }

    private void flatten(List<Map<String, Object>> nodes, String parentId, int level, List<ReorderItem> items) {
        if (nodes == null) return;
        int sort = 0;
        for (Map<String, Object> node : nodes) {
            String id = asString(node.get("content_id"));
            if (!StringUtils.hasText(id)) continue;
            items.add(new ReorderItem(id, parentId, sort++));
            Object children = node.get("children");
            if (children instanceof List) {
                //noinspection unchecked
                flatten((List<Map<String, Object>>) children, id, level + 1, items);
            }
        }
    }

    private void apply(DataCoursebookContent entity, Map<String, Object> payload) {
        entity.setContentName(readRequired(payload.get("content_name"), "目录名称不能为空"));
        entity.setContentType(readRequired(payload.get("content_type"), "目录类型不能为空"));
        entity.setParentId(defaultParent(payload.get("parent_id")));
        entity.setCoursebookId(readRequired(payload.get("coursebook_id"), "教材ID不能为空"));
        entity.setComments(asString(payload.get("comments")));
        entity.setStatus(defaultStatus(payload.get("status")));
        entity.setSortOrder(readInt(payload.get("sort_order")));
    }

    private void validateHierarchy(String type, String parentId) {
        if (!StringUtils.hasText(type)) throw new RuntimeException("目录类型不能为空");
        if ("unit".equals(type)) {
            if (!"0".equals(parentId)) throw new RuntimeException("单元的上级必须为根节点");
            return;
        }
        DataCoursebookContent parent = repository.findById(parentId).orElseThrow(() -> new RuntimeException("上级目录不存在"));
        if ("chapter".equals(type) && !"unit".equals(parent.getContentType())) throw new RuntimeException("章的上级必须是单元");
        if ("section".equals(type) && !"chapter".equals(parent.getContentType())) throw new RuntimeException("节的上级必须是章");
    }

    private Map<String, Object> toMap(DataCoursebookContent entity) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("content_id", entity.getContentId());
        map.put("content_name", entity.getContentName());
        map.put("content_type", entity.getContentType());
        map.put("parent_id", entity.getParentId());
        map.put("coursebook_id", entity.getCoursebookId());
        map.put("comments", entity.getComments());
        map.put("status", entity.getStatus());
        map.put("sort_order", entity.getSortOrder());
        return map;
    }

    private String readRequired(Object value, String message) {
        String result = asString(value);
        if (!StringUtils.hasText(result)) throw new RuntimeException(message);
        return result;
    }

    private String defaultParent(Object value) {
        String parentId = asString(value);
        return StringUtils.hasText(parentId) ? parentId : "0";
    }

    private String defaultStatus(Object value) {
        String status = asString(value);
        return StringUtils.hasText(status) ? status : "y";
    }

    private Integer readInt(Object value) {
        String s = asString(value);
        if (!StringUtils.hasText(s)) return 0;
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private String readId(Object value) {
        String id = asString(value);
        return StringUtils.hasText(id) ? id : UUID.randomUUID().toString().replace("-", "");
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private static class ReorderItem {
        private final String id;
        private final String parentId;
        private final int sortOrder;

        private ReorderItem(String id, String parentId, int sortOrder) {
            this.id = id;
            this.parentId = parentId;
            this.sortOrder = sortOrder;
        }
    }
}
