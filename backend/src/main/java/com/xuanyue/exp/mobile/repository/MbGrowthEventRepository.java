package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbGrowthEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MbGrowthEventRepository extends JpaRepository<MbGrowthEvent, String> {
    List<MbGrowthEvent> findByUserIdOrderBySortTimeDesc(String userId);
    Optional<MbGrowthEvent> findByUserIdAndSourceTypeAndSourceId(String userId, String sourceType, String sourceId);
}
