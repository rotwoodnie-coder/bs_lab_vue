package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbPointsLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface MbPointsLedgerRepository extends JpaRepository<MbPointsLedger, String> {
    boolean existsByUserIdAndSourceTypeAndSourceId(String userId, String sourceType, String sourceId);

    @Query("SELECT COALESCE(SUM(l.delta), 0) FROM MbPointsLedger l WHERE l.userId = ?1")
    long sumDeltaByUserId(String userId);

    @Query("SELECT COALESCE(SUM(l.delta), 0) FROM MbPointsLedger l WHERE l.userId = ?1 AND l.createTime >= ?2")
    long sumDeltaByUserIdSince(String userId, Date since);

    Page<MbPointsLedger> findByUserIdOrderByCreateTimeDesc(String userId, Pageable pageable);
}
