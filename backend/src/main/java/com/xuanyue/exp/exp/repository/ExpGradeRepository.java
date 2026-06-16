package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ExpGradeRepository extends JpaRepository<ExpGrade, String> {

    List<ExpGrade> findByExpIdOrderBySortOrderAsc(String expId);

    List<ExpGrade> findByExpIdIn(Collection<String> expIds);

    void deleteByExpId(String expId);
}
