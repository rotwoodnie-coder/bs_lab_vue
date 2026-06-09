package com.xuanyue.exp.data.service.impl;

import com.xuanyue.exp.data.service.DataDictService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataDictServiceImpl implements DataDictService {

    private static final Map<String, DictMeta> META = new HashMap<String, DictMeta>();

    static {
        META.put("data_school_subject", new DictMeta("data_school_subject", "subject_id", "subject_name", "subject_name", "sort_order"));
        META.put("data_school_semester", new DictMeta("data_school_semester", "semester_id", "semester_name", "semester_name", "sort_order"));
        META.put("data_school_level", new DictMeta("data_school_level", "level_id", "level_name", "level_name", "sort_order"));
        META.put("data_school_grade", new DictMeta("data_school_grade", "grade_id", "grade_name", "grade_name", "sort_order"));
        META.put("data_material_prop", new DictMeta("data_material_prop", "prop_id", "prop_name", "prop_name", "sort_order"));
        META.put("data_material_type", new DictMeta("data_material_type", "type_id", "type_name", "type_name", "sort_order"));
        META.put("data_material_unit", new DictMeta("data_material_unit", "unit_id", "unit_name", "unit_name", "sort_order"));
        META.put("data_material_security", new DictMeta("data_material_security", "security_id", "security_name", "security_name", "sort_order"));
        META.put("data_pref_title", new DictMeta("data_pref_title", "title_id", "title_name", "title_name", "sort_order"));
        META.put("data_rating_scale", new DictMeta("data_rating_scale", "scale_id", "scale_name", "scale_name", "sort_order"));
        META.put("data_textbook_edition", new DictMeta("data_textbook_edition", "edition_id", "edition_name", "edition_name", "sort_order"));
        META.put("data_msg_type", new DictMeta("data_msg_type", "msg_type_id", "msg_type_name", "msg_type_name", "sort_order"));
        META.put("data_question_type", new DictMeta("data_question_type", "type_id", "type_name", "type_name", "sort_order"));
        META.put("data_question_capacity", new DictMeta("data_question_capacity", "capacity_id", "capacity_name", "capacity_name", "sort_order"));
        META.put("data_difficulty_type", new DictMeta("data_difficulty_type", "difficulty_id", "difficulty_name", "difficulty_name", "sort_order"));
    }

    private final JdbcTemplate jdbcTemplate;

    public DataDictServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> list(String type) {
        DictMeta meta = meta(type);
        String sql = "select * from " + meta.tableName + " order by " + meta.sortCol + " asc, " + meta.nameCol + " asc";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Object get(String type, String id) {
        DictMeta meta = meta(type);
        String sql = "select * from " + meta.tableName + " where " + meta.idCol + " = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);
        if (rows.isEmpty()) {
            throw new RuntimeException("记录不存在");
        }
        return rows.get(0);
    }

    @Override
    public void create(String type, Map<String, Object> payload) {
        DictMeta meta = meta(type);
        String id = readId(payload.get("id"));
        String name = asString(payload.get(meta.fieldKey));
        String comments = asString(payload.get("comments"));
        Integer sortOrder = asInteger(payload.get("sortOrder"));
        String status = defaultStatus(payload.get("status"));
        String sql = "insert into " + meta.tableName + " (" + meta.idCol + ", " + meta.nameCol + ", comments, status, sort_order) values (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, name, comments, status, sortOrder);
    }

    @Override
    public void update(String type, String id, Map<String, Object> payload) {
        DictMeta meta = meta(type);
        String name = asString(payload.get(meta.fieldKey));
        String comments = asString(payload.get("comments"));
        Integer sortOrder = asInteger(payload.get("sortOrder"));
        String status = defaultStatus(payload.get("status"));
        String sql = "update " + meta.tableName + " set " + meta.nameCol + " = ?, comments = ?, status = ?, sort_order = ? where " + meta.idCol + " = ?";
        jdbcTemplate.update(sql, name, comments, status, sortOrder, id);
    }

    @Override
    public void delete(String type, String id) {
        DictMeta meta = meta(type);
        String sql = "delete from " + meta.tableName + " where " + meta.idCol + " = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Map<String, Object>> page(String type, int pageNum, int pageSize, String keyword) {
        DictMeta meta = meta(type);
        int offset = Math.max(pageNum - 1, 0) * pageSize;
        String where = buildKeywordWhere(meta, keyword);
        String sql = "select * from " + meta.tableName + where + " order by " + meta.sortCol + " asc, " + meta.nameCol + " asc limit ? offset ?";
        return jdbcTemplate.queryForList(sql, keywordParams(meta, keyword, pageSize, offset));
    }

    @Override
    public long count(String type, String keyword) {
        DictMeta meta = meta(type);
        String where = buildKeywordWhere(meta, keyword);
        String sql = "select count(1) from " + meta.tableName + where;
        return jdbcTemplate.queryForObject(sql, Long.class, keywordParams(meta, keyword));
    }

    private Object[] keywordParams(DictMeta meta, String keyword, Object... extra) {
        if (StringUtils.hasText(keyword)) {
            Object[] params = new Object[1 + extra.length];
            params[0] = "%" + keyword.trim() + "%";
            System.arraycopy(extra, 0, params, 1, extra.length);
            return params;
        }
        return extra;
    }

    private String buildKeywordWhere(DictMeta meta, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }
        return " where " + meta.nameCol + " like ?";
    }

    private DictMeta meta(String type) {
        DictMeta meta = META.get(type);
        if (meta == null) {
            throw new RuntimeException("未知字典类型");
        }
        return meta;
    }

    private String readId(Object value) {
        String id = asString(value);
        if (!StringUtils.hasText(id)) {
            throw new RuntimeException("编号不能为空");
        }
        return id;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Integer asInteger(Object value) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return 0;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private String defaultStatus(Object value) {
        String status = asString(value);
        return StringUtils.hasText(status) ? status : "y";
    }

    private static class DictMeta {
        final String tableName;
        final String idCol;
        final String nameCol;
        final String fieldKey;
        final String sortCol;

        DictMeta(String tableName, String idCol, String nameCol, String fieldKey, String sortCol) {
            this.tableName = tableName;
            this.idCol = idCol;
            this.nameCol = nameCol;
            this.fieldKey = fieldKey;
            this.sortCol = sortCol;
        }
    }
}
