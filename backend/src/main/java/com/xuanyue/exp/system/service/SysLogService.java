package com.xuanyue.exp.system.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.system.entity.SysLog;

public interface SysLogService {
    PageResult<?> page(int pageNum, int pageSize, String startTime, String endTime, String logType);
    Object get(String logId);
    void delete(String logId);
    SysLog save(SysLog log);
}
