package com.xuanyue.exp.edu.repository;

import com.xuanyue.exp.edu.entity.TeacherSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, String> {
    List<TeacherSubject> findByTeacherId(String teacherId);
    List<TeacherSubject> findBySubjectId(String subjectId);
    void deleteByTeacherId(String teacherId);
}
