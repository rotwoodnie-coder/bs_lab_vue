package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MbCommentRepository extends JpaRepository<MbComment, String> {

    List<MbComment> findByTargetTypeAndTargetIdAndStatusOrderByCreateTimeDesc(
            String targetType, String targetId, String status);

    long countByTargetTypeAndTargetIdAndStatus(String targetType, String targetId, String status);
}
