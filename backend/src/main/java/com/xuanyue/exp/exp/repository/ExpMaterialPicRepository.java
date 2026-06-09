package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpMaterialPic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpMaterialPicRepository extends JpaRepository<ExpMaterialPic, String> {
    List<ExpMaterialPic> findByExpMaterialIdOrderBySortOrderAsc(String expMaterialId);
    void deleteByExpMaterialId(String expMaterialId);
}
