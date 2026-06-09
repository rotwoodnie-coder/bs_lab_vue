package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpScientist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpScientistRepository extends JpaRepository<ExpScientist, String> {
    List<ExpScientist> findByExpIdOrderBySortOrderAsc(String expId);
    void deleteByExpId(String expId);
}
