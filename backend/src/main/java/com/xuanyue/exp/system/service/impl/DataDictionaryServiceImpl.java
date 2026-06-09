package com.xuanyue.exp.system.service.impl;

import com.xuanyue.exp.system.entity.DataMsgType;
import com.xuanyue.exp.system.entity.DataOrgType;
import com.xuanyue.exp.system.entity.DataRole;
import com.xuanyue.exp.system.repository.DataMsgTypeRepository;
import com.xuanyue.exp.system.repository.DataOrgTypeRepository;
import com.xuanyue.exp.system.repository.DataRoleRepository;
import com.xuanyue.exp.system.service.DataDictionaryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DataDictionaryServiceImpl implements DataDictionaryService {

    private final DataMsgTypeRepository dataMsgTypeRepository;
    private final DataOrgTypeRepository dataOrgTypeRepository;
    private final DataRoleRepository dataRoleRepository;

    public DataDictionaryServiceImpl(DataMsgTypeRepository dataMsgTypeRepository,
                                     DataOrgTypeRepository dataOrgTypeRepository,
                                     DataRoleRepository dataRoleRepository) {
        this.dataMsgTypeRepository = dataMsgTypeRepository;
        this.dataOrgTypeRepository = dataOrgTypeRepository;
        this.dataRoleRepository = dataRoleRepository;
    }

    @Override
    public List<?> list(String type) {
        if ("data_msg_type".equals(type)) {
            return dataMsgTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));
        }
        if ("data_org_type".equals(type)) {
            return dataOrgTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));
        }
        if ("data_role".equals(type)) {
            return dataRoleRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));
        }
        throw new RuntimeException("未知字典类型");
    }

    @Override
    public Object get(String type, String id) {
        if ("data_msg_type".equals(type)) {
            return dataMsgTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        }
        if ("data_org_type".equals(type)) {
            return dataOrgTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        }
        if ("data_role".equals(type)) {
            return dataRoleRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        }
        throw new RuntimeException("未知字典类型");
    }

    @Override
    public void create(String type, Map<String, Object> payload) {
        if ("data_msg_type".equals(type)) {
            DataMsgType item = new DataMsgType();
            item.setMsgTypeId(readId(payload.get("id")));
            item.setMsgTypeName(asString(payload.get("msgTypeName")));
            item.setComments(asString(payload.get("comments")));
            item.setStatus(defaultStatus(payload.get("status")));
            item.setSortOrder(asInteger(payload.get("sortOrder")));
            dataMsgTypeRepository.save(item);
            return;
        }
        if ("data_org_type".equals(type)) {
            DataOrgType item = new DataOrgType();
            item.setOrgTypeId(readId(payload.get("id")));
            item.setOrgTypeName(asString(payload.get("orgTypeName")));
            item.setComments(asString(payload.get("comments")));
            item.setStatus(defaultStatus(payload.get("status")));
            item.setSortOrder(asInteger(payload.get("sortOrder")));
            dataOrgTypeRepository.save(item);
            return;
        }
        if ("data_role".equals(type)) {
            DataRole item = new DataRole();
            item.setRoleId(readId(payload.get("id")));
            item.setRoleName(asString(payload.get("roleName")));
            item.setComments(asString(payload.get("comments")));
            item.setStatus(defaultStatus(payload.get("status")));
            item.setSortOrder(asInteger(payload.get("sortOrder")));
            dataRoleRepository.save(item);
            return;
        }
        throw new RuntimeException("未知字典类型");
    }

    @Override
    public void update(String type, String id, Map<String, Object> payload) {
        if ("data_msg_type".equals(type)) {
            DataMsgType item = dataMsgTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
            item.setMsgTypeName(asString(payload.get("msgTypeName")));
            item.setComments(asString(payload.get("comments")));
            item.setStatus(defaultStatus(payload.get("status")));
            item.setSortOrder(asInteger(payload.get("sortOrder")));
            dataMsgTypeRepository.save(item);
            return;
        }
        if ("data_org_type".equals(type)) {
            DataOrgType item = dataOrgTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
            item.setOrgTypeName(asString(payload.get("orgTypeName")));
            item.setComments(asString(payload.get("comments")));
            item.setStatus(defaultStatus(payload.get("status")));
            item.setSortOrder(asInteger(payload.get("sortOrder")));
            dataOrgTypeRepository.save(item);
            return;
        }
        if ("data_role".equals(type)) {
            DataRole item = dataRoleRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
            item.setRoleName(asString(payload.get("roleName")));
            item.setComments(asString(payload.get("comments")));
            item.setStatus(defaultStatus(payload.get("status")));
            item.setSortOrder(asInteger(payload.get("sortOrder")));
            dataRoleRepository.save(item);
            return;
        }
        throw new RuntimeException("未知字典类型");
    }

    @Override
    public void delete(String type, String id) {
        if ("data_msg_type".equals(type)) {
            dataMsgTypeRepository.deleteById(id);
            return;
        }
        if ("data_org_type".equals(type)) {
            dataOrgTypeRepository.deleteById(id);
            return;
        }
        if ("data_role".equals(type)) {
            dataRoleRepository.deleteById(id);
            return;
        }
        throw new RuntimeException("未知字典类型");
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
