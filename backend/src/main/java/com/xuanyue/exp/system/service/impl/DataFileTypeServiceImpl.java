package com.xuanyue.exp.system.service.impl;

import com.xuanyue.exp.system.entity.DataFileType;
import com.xuanyue.exp.system.repository.DataFileTypeRepository;
import com.xuanyue.exp.system.service.DataFileTypeService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DataFileTypeServiceImpl implements DataFileTypeService {

    private final DataFileTypeRepository dataFileTypeRepository;

    public DataFileTypeServiceImpl(DataFileTypeRepository dataFileTypeRepository) {
        this.dataFileTypeRepository = dataFileTypeRepository;
    }

    @Override
    public List<?> list() {
        return dataFileTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));
    }

    @Override
    public Object get(String id) {
        return dataFileTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
    }

    @Override
    public void create(Map<String, Object> payload) {
        DataFileType item = new DataFileType();
        item.setTypeId(readId(payload.get("typeId")));
        item.setTypeName(asString(payload.get("typeName")));
        item.setComments(asString(payload.get("comments")));
        item.setStatus(defaultStatus(payload.get("status")));
        item.setSortOrder(asInteger(payload.get("sortOrder")));
        item.setLogoClass(asString(payload.get("logoClass")));
        dataFileTypeRepository.save(item);
    }

    @Override
    public void update(String id, Map<String, Object> payload) {
        DataFileType item = dataFileTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        item.setTypeName(asString(payload.get("typeName")));
        item.setComments(asString(payload.get("comments")));
        item.setStatus(defaultStatus(payload.get("status")));
        item.setSortOrder(asInteger(payload.get("sortOrder")));
        item.setLogoClass(asString(payload.get("logoClass")));
        dataFileTypeRepository.save(item);
    }

    @Override
    public void delete(String id) {
        dataFileTypeRepository.deleteById(id);
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
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private String defaultStatus(Object value) {
        String status = asString(value);
        return StringUtils.hasText(status) ? status : "y";
    }
}
