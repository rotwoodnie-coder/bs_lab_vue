package com.xuanyue.exp.data.repository;

import com.xuanyue.exp.data.entity.DataSchoolGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DataSchoolGradeRepository extends JpaRepository<DataSchoolGrade, String> {

    List<DataSchoolGrade> findByGradeIdIn(Collection<String> gradeIds);

    List<DataSchoolGrade> findByStatusOrderBySortOrderAsc(String status);
}
