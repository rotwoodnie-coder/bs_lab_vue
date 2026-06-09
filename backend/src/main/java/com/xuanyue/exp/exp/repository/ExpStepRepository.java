package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpStepRepository extends JpaRepository<ExpStep, String> {
    List<ExpStep> findByExpIdOrderBySortOrderAsc(String expId);
    void deleteByExpId(String expId);
}
