package com.xuanyue.exp.system.repository;

import com.xuanyue.exp.system.entity.SysLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysLogRepository extends JpaRepository<SysLog, String>, JpaSpecificationExecutor<SysLog> {
}
