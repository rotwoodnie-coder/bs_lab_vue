package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpReferenceRepository extends JpaRepository<ExpReference, String> {
    List<ExpReference> findByExpIdOrderBySortOrderAsc(String expId);
    void deleteByExpId(String expId);
}
