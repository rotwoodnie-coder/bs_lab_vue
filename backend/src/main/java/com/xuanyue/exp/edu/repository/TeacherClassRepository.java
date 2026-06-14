package com.xuanyue.exp.edu.repository;

import com.xuanyue.exp.edu.entity.TeacherClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherClassRepository extends JpaRepository<TeacherClass, String> {
    List<TeacherClass> findByTeacherId(String teacherId);
    void deleteByTeacherId(String teacherId);
}
