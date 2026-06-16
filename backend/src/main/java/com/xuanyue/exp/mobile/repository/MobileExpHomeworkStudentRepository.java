package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MobileExpHomeworkStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MobileExpHomeworkStudentRepository extends JpaRepository<MobileExpHomeworkStudent, String> {

    List<MobileExpHomeworkStudent> findByHomeworkId(String homeworkId);

    List<MobileExpHomeworkStudent> findByStudentExpId(String studentExpId);

    Optional<MobileExpHomeworkStudent> findByHomeworkIdAndStudentExpId(String homeworkId, String studentExpId);

    List<MobileExpHomeworkStudent> findByStudentExpIdIn(List<String> studentExpIds);
}
