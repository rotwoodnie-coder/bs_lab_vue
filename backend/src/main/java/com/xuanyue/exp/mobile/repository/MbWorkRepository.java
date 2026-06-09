package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MbWorkRepository extends JpaRepository<MbWork, String> {
    List<MbWork> findByStatusAndWorkTypeOrderByCreateTimeDesc(String status, String workType);
    List<MbWork> findByStatusOrderByCreateTimeDesc(String status);
    List<MbWork> findByStudentUserIdOrderByCreateTimeDesc(String studentUserId);
    List<MbWork> findByStudentUserIdAndWorkTypeOrderByCreateTimeDesc(String studentUserId, String workType);
    long countByStatus(String status);
    long countByStudentUserIdAndStatus(String studentUserId, String status);
    long countByStudentUserIdAndStatusAndIsFeatured(String studentUserId, String status, String isFeatured);
    List<MbWork> findByTaskId(String taskId);
}
