package com.xuanyue.exp.edu.repository;

import com.xuanyue.exp.edu.entity.SubjectGroupTeacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectGroupTeacherRepository extends JpaRepository<SubjectGroupTeacher, String> {
    List<SubjectGroupTeacher> findByGroupId(String groupId);
    void deleteByGroupId(String groupId);
}
