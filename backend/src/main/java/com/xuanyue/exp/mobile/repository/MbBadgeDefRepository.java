package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbBadgeDef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MbBadgeDefRepository extends JpaRepository<MbBadgeDef, String> {
    List<MbBadgeDef> findByStatusOrderBySortOrderAsc(String status);
}
