package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpResultRepository extends JpaRepository<ExpResult, String> {
    List<ExpResult> findByExpIdOrderBySortOrderAsc(String expId);
    void deleteByExpId(String expId);
}
