package com.xuanyue.exp.system.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.system.entity.SysLog;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysLogRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import com.xuanyue.exp.system.service.SysLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SysLogServiceImpl implements SysLogService {

    private final SysLogRepository repository;
    private final SysUserRepository sysUserRepository;
    private final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SysLogServiceImpl(SysLogRepository repository, SysUserRepository sysUserRepository) {
        this.repository = repository;
        this.sysUserRepository = sysUserRepository;
    }

    @Override
    public PageResult<?> page(int pageNum, int pageSize, String startTime, String endTime, String logType) {
        Specification<SysLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(logType)) {
                predicates.add(cb.like(root.get("logType"), "%" + logType.trim() + "%"));
            }
            try {
                if (StringUtils.hasText(startTime)) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("logTime"), parser.parse(startTime)));
                }
                if (StringUtils.hasText(endTime)) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("logTime"), parser.parse(endTime)));
                }
            } catch (ParseException e) {
                throw new RuntimeException("时间格式不正确");
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<SysLog> page = repository.findAll(
                spec,
                PageRequest.of(Math.max(pageNum - 1, 0), Math.max(pageSize, 1), Sort.by(Sort.Direction.DESC, "logTime"))
        );
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SysLog log : page.getContent()) {
            rows.add(toView(log));
        }
        return new PageResult<>(page.getTotalElements(), rows);
    }

    @Override
    public Object get(String logId) {
        SysLog log = repository.findById(logId).orElseThrow(() -> new RuntimeException("日志不存在"));
        return toView(log);
    }

    @Override
    @Transactional
    public void delete(String logId) {
        if (!repository.existsById(logId)) {
            throw new RuntimeException("日志不存在");
        }
        repository.deleteById(logId);
    }

    @Override
    @Transactional
    public SysLog save(SysLog log) {
        if (!StringUtils.hasText(log.getLogId())) {
            log.setLogId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (log.getLogTime() == null) {
            log.setLogTime(new Date());
        }
        return repository.save(log);
    }

    private Map<String, Object> toView(SysLog log) {
        Map<String, Object> view = new HashMap<>();
        view.put("logId", log.getLogId());
        view.put("userId", log.getUserId());
        view.put("userName", resolveUserName(log.getUserId()));
        view.put("logType", log.getLogType());
        view.put("logTime", log.getLogTime());
        view.put("logDataType", log.getLogDataType());
        view.put("logDataId", log.getLogDataId());
        view.put("logDataContent", log.getLogDataContent());
        return view;
    }

    private String resolveUserName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "-";
        }
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return userId;
        }
        String userName = StringUtils.hasText(user.getUserName()) ? user.getUserName() : userId;
        String loginName = StringUtils.hasText(user.getLoginName()) ? user.getLoginName() : userId;
        return userName + "-" + loginName + "";
    }
}
