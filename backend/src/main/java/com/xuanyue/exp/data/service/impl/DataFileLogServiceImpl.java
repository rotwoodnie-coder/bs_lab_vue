package com.xuanyue.exp.data.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.data.entity.DataFileLog;
import com.xuanyue.exp.data.repository.DataFileLogRepository;
import com.xuanyue.exp.data.service.DataFileLogService;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DataFileLogServiceImpl implements DataFileLogService {

    private final DataFileLogRepository repository;
    private final SysUserRepository sysUserRepository;

    public DataFileLogServiceImpl(DataFileLogRepository repository, SysUserRepository sysUserRepository) {
        this.repository = repository;
        this.sysUserRepository = sysUserRepository;
    }

    @Override
    public PageResult<?> list(String keyword, String fileId, String logType, String logUserId, int pageNum, int pageSize) {
        String kw = asString(keyword);
        String fid = asString(fileId);
        String type = asString(logType);
        String userId = asString(logUserId);
        Map<String, String> userNameMap = loadUserNameMap();
        List<Map<String, Object>> records = repository.findAll(Sort.by(Sort.Direction.DESC, "logTime")).stream()
                .filter(item -> !StringUtils.hasText(kw)
                        || containsIgnoreCase(item.getFileId(), kw)
                        || containsIgnoreCase(item.getLogType(), kw)
                        || containsIgnoreCase(item.getLogTypeName(), kw)
                        || containsIgnoreCase(item.getLogUserName(), kw))
                .filter(item -> !StringUtils.hasText(fid) || fid.equals(item.getFileId()))
                .filter(item -> !StringUtils.hasText(type) || type.equals(item.getLogType()))
                .filter(item -> !StringUtils.hasText(userId) || userId.equals(item.getLogUserId()))
                .map(item -> toView(item, userNameMap))
                .collect(Collectors.toList());
        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, records.size());
        int toIndex = Math.min(fromIndex + safePageSize, records.size());
        return new PageResult<>(records.size(), records.subList(fromIndex, toIndex));
    }

    @Override
    public Object get(String id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
    }

    @Override
    public void create(Map<String, Object> payload, String currentUserId) {
        DataFileLog item = new DataFileLog();
        item.setLogId(UUID.randomUUID().toString().replace("-", ""));
        applyPayload(item, payload, currentUserId);
        repository.save(item);
    }

    @Override
    public void update(String id, Map<String, Object> payload, String currentUserId) {
        DataFileLog item = repository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        applyPayload(item, payload, currentUserId);
        repository.save(item);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    private void applyPayload(DataFileLog item, Map<String, Object> payload, String currentUserId) {
        item.setFileId(asString(payload.get("fileId")));
        item.setLogType(asString(payload.get("logType")));
        item.setLogTypeName(asString(payload.get("logTypeName")));
        item.setLogUserId(defaultText(payload.get("logUserId"), currentUserId));
        item.setLogUserName(asString(payload.get("logUserName")));
        item.setLogTime(asDate(payload.get("logTime")));
        if (item.getLogTime() == null) {
            item.setLogTime(new Date());
        }
    }

    private Map<String, String> loadUserNameMap() {
        return sysUserRepository.findAll().stream().collect(Collectors.toMap(SysUser::getUserId, user -> {
            String userName = user.getUserName();
            if (!StringUtils.hasText(userName)) {
                userName = user.getLoginName();
            }
            return StringUtils.hasText(userName) ? userName : user.getUserId();
        }, (a, b) -> a, HashMap::new));
    }

    private Map<String, Object> toView(DataFileLog item, Map<String, String> userNameMap) {
        Map<String, Object> view = new HashMap<String, Object>();
        view.put("logId", item.getLogId());
        view.put("fileId", item.getFileId());
        view.put("logType", item.getLogType());
        view.put("logTypeName", item.getLogTypeName());
        view.put("logUserId", item.getLogUserId());
        view.put("logUserName", userNameMap.getOrDefault(item.getLogUserId(), item.getLogUserName()));
        view.put("logTime", item.getLogTime());
        return view;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String defaultText(Object explicit, String fallback) {
        String value = asString(explicit);
        return StringUtils.hasText(value) ? value : fallback;
    }

    private Date asDate(Object value) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        try {
            return javax.xml.bind.DatatypeConverter.parseDateTime(String.valueOf(value)).getTime();
        } catch (Exception ex) {
            return new Date();
        }
    }
}
