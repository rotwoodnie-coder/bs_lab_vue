package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.exp.entity.ExpSimulatorLog;
import com.xuanyue.exp.exp.repository.ExpSimulatorLogRepository;
import com.xuanyue.exp.exp.service.ExpSimulatorLogService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class ExpSimulatorLogServiceImpl implements ExpSimulatorLogService {

    private final ExpSimulatorLogRepository repository;

    public ExpSimulatorLogServiceImpl(ExpSimulatorLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void record(String simulatorId, String userId) {
        ExpSimulatorLog log = new ExpSimulatorLog();
        log.setLogId(UUID.randomUUID().toString().replace("-", ""));
        log.setSimulatorId(simulatorId);
        log.setUserId(userId);
        log.setLogTime(new Date());
        repository.save(log);
    }
}
