package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbUserReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MbUserReactionRepository extends JpaRepository<MbUserReaction, Long> {

    Optional<MbUserReaction> findByUserIdAndTargetIdAndTargetTypeAndReactionType(
            String userId, String targetId, String targetType, String reactionType);

    long deleteByUserIdAndTargetIdAndTargetTypeAndReactionType(
            String userId, String targetId, String targetType, String reactionType);

    long countByTargetIdAndTargetTypeAndReactionType(String targetId, String targetType, String reactionType);

    boolean existsByUserIdAndTargetIdAndTargetTypeAndReactionType(
            String userId, String targetId, String targetType, String reactionType);
}
