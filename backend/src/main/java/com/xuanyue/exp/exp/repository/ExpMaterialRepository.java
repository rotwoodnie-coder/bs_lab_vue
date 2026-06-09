package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpMaterialRepository extends JpaRepository<ExpMaterial, String> {
    List<ExpMaterial> findByExpIdOrderBySortOrderAsc(String expId);
    void deleteByExpId(String expId);
    List<ExpMaterial> findByExpIdIn(List<String> expIds);
}
