package com.xuanyue.exp.data.repository;

import com.xuanyue.exp.data.entity.MaterialPic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialPicRepository extends JpaRepository<MaterialPic, String> {
    List<MaterialPic> findByMaterialIdOrderBySortOrderAscCreateTimeAsc(String materialId);
    void deleteByMaterialId(String materialId);
}
