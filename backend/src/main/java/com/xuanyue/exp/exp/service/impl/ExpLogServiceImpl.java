package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.exp.entity.ExpLog;
import com.xuanyue.exp.exp.repository.ExpLogRepository;
import com.xuanyue.exp.exp.service.ExpLogService;
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
public class ExpLogServiceImpl implements ExpLogService {

    private final ExpLogRepository expLogRepository;

    public ExpLogServiceImpl(ExpLogRepository expLogRepository) {
        this.expLogRepository = expLogRepository;
    }

    @Override
    public PageResult<?> list(String keyword, String expId, String logType, String logUserId, int pageNum, int pageSize) {
        String kw = asString(keyword);
        String eId = asString(expId);
        String type = asString(logType);
        String userId = asString(logUserId);
        List<Map<String, Object>> records = expLogRepository.findAll(Sort.by(Sort.Direction.DESC, "logTime")).stream()
                .filter(item -> !StringUtils.hasText(kw)
                        || containsIgnoreCase(item.getLogTypeName(), kw)
                        || containsIgnoreCase(item.getLogComments(), kw)
                        || containsIgnoreCase(item.getLogUserName(), kw))
                .filter(item -> !StringUtils.hasText(eId) || eId.equals(item.getExpId()))
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
        return expLogRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
    }

    @Override
    public void create(Map<String, Object> payload, String currentUserId) {
        ExpLog item = new ExpLog();
        item.setLogId(UUID.randomUUID().toString().replace("-", ""));
        applyPayload(item, payload, currentUserId);
        expLogRepository.save(item);
    }

    @Override
    public void update(String id, Map<String, Object> payload, String currentUserId) {
        ExpLog item = expLogRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        applyPayload(item, payload, currentUserId);
        expLogRepository.save(item);
    }

    @Override
    public void delete(String id) {
        expLogRepository.deleteById(id);
    }

    private void applyPayload(ExpLog item, Map<String, Object> payload, String currentUserId) {
        item.setExpId(asString(payload.get("expId")));
        item.setLogType(asString(payload.get("logType")));
        item.setLogTypeName(asString(payload.get("logTypeName")));
        item.setLogComments(asString(payload.get("logComments")));
        item.setLogUserId(asString(payload.get("logUserId")));
        item.setLogUserName(asString(payload.get("logUserName")));
        if (!StringUtils.hasText(item.getLogUserId()) && StringUtils.hasText(currentUserId)) {
            item.setLogUserId(currentUserId);
        }
        item.setLogTime(new Date());
    }

    private Map<String, Object> toView(ExpLog item) {
        Map<String, Object> view = new HashMap<String, Object>();
        view.put("logId", item.getLogId());
        view.put("expId", item.getExpId());
        view.put("logType", item.getLogType());
        view.put("logTypeName", item.getLogTypeName());
        view.put("logComments", item.getLogComments());
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
