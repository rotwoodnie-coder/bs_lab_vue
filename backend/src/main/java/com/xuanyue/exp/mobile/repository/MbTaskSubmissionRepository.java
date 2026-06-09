package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MbTaskSubmissionRepository extends JpaRepository<MbTaskSubmission, String> {
    Optional<MbTaskSubmission> findByTaskIdAndStudentUserId(String taskId, String studentUserId);
    List<MbTaskSubmission> findByTaskId(String taskId);
    List<MbTaskSubmission> findByStudentUserIdOrderByUpdateTimeDesc(String studentUserId);
    long countByStudentUserIdAndStateNot(String studentUserId, String state);
    long countByStudentUserIdAndState(String studentUserId, String state);
    long countByStudentUserIdAndStateIn(String studentUserId, List<String> states);
}
