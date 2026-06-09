package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MbTaskRepository extends JpaRepository<MbTask, String> {
    List<MbTask> findByStatusOrderByCreateTimeDesc(String status);
    List<MbTask> findByTeacherUserIdAndStatusOrderByCreateTimeDesc(String teacherUserId, String status);
    long countByStatus(String status);
    long countByTeacherUserIdAndStatus(String teacherUserId, String status);
    Optional<MbTask> findFirstByTitleAndStatus(String title, String status);
}
