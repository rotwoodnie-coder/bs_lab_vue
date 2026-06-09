package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbBadgeProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MbBadgeProgressRepository extends JpaRepository<MbBadgeProgress, Long> {
    List<MbBadgeProgress> findByUserId(String userId);
    long countByUserIdAndEarned(String userId, String earned);
    Optional<MbBadgeProgress> findByUserIdAndBadgeId(String userId, String badgeId);
}
