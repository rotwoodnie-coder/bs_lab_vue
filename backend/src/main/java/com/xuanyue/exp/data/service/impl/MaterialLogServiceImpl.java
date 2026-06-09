package com.xuanyue.exp.data.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.data.entity.MaterialLog;
import com.xuanyue.exp.data.repository.MaterialLogRepository;
import com.xuanyue.exp.data.service.MaterialLogService;
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
public class MaterialLogServiceImpl implements MaterialLogService {

    private final MaterialLogRepository materialLogRepository;

    public MaterialLogServiceImpl(MaterialLogRepository materialLogRepository) {
        this.materialLogRepository = materialLogRepository;
    }

    @Override
    public PageResult<?> list(String keyword, String materialId, String logType, String logUserId, int pageNum, int pageSize) {
        String kw = asString(keyword);
        String mid = asString(materialId);
        String type = asString(logType);
        String userId = asString(logUserId);
        List<Map<String, Object>> records = materialLogRepository.findAll(Sort.by(Sort.Direction.DESC, "logTime")).stream()
                .filter(item -> !StringUtils.hasText(kw)
                        || containsIgnoreCase(item.getLogType(), kw)
                        || containsIgnoreCase(item.getLogTypeName(), kw)
                        || containsIgnoreCase(item.getLogUserName(), kw)
                        || containsIgnoreCase(item.getMaterialId(), kw))
                .filter(item -> !StringUtils.hasText(mid) || mid.equals(item.getMaterialId()))
                .filter(item -> !StringUtils.hasText(type) || type.equals(item.getLogType()))
                .filter(item -> !StringUtils.hasText(userId) || userId.equals(item.getLogUserId()))
                .map(this::toView)
                .collect(Collectors.toList());
        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, records.size());
        int toIndex = Math.min(fromIndex + safePageSize, records.size());
        return new PageResult<>(records.size(), records.subList(fromIndex, toIndex));
    }

    @Override
    public Object get(String id) {
        return materialLogRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
    }

    @Override
    public void create(Map<String, Object> payload, String currentUserId) {
        MaterialLog item = new MaterialLog();
        item.setLogId(UUID.randomUUID().toString().replace("-", ""));
        applyPayload(item, payload, currentUserId);
        if (item.getLogTime() == null) {
            item.setLogTime(new Date());
        }
        materialLogRepository.save(item);
    }

    @Override
    public void update(String id, Map<String, Object> payload, String currentUserId) {
        MaterialLog item = materialLogRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        applyPayload(item, payload, currentUserId);
        materialLogRepository.save(item);
    }

    @Override
    public void delete(String id) {
        materialLogRepository.deleteById(id);
    }

    private void applyPayload(MaterialLog item, Map<String, Object> payload, String currentUserId) {
        item.setMaterialId(asString(payload.get("materialId")));
        item.setLogType(asString(payload.get("logType")));
        item.setLogTypeName(asString(payload.get("logTypeName")));
        item.setLogUserId(StringUtils.hasText(asString(payload.get("logUserId"))) ? asString(payload.get("logUserId")) : currentUserId);
        item.setLogUserName(asString(payload.get("logUserName")));
        Object logTime = payload.get("logTime");
        if (logTime instanceof Date) {
            item.setLogTime((Date) logTime);
        } else if (StringUtils.hasText(asString(logTime))) {
            item.setLogTime(new Date(Long.parseLong(asString(logTime))));
        }
    }

    private Map<String, Object> toView(MaterialLog item) {
        Map<String, Object> view = new HashMap<String, Object>();
        view.put("logId", item.getLogId());
        view.put("materialId", item.getMaterialId());
        view.put("logType", item.getLogType());
        view.put("logTypeName", item.getLogTypeName());
        view.put("logUserId", item.getLogUserId());
        view.put("logUserName", item.getLogUserName());
        view.put("logTime", item.getLogTime());
        return view;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}
