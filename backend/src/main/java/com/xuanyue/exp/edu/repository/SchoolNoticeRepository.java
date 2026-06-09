package com.xuanyue.exp.edu.repository;

import java.util.List;
import com.xuanyue.exp.edu.entity.SchoolNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface SchoolNoticeRepository extends JpaRepository<SchoolNotice, String>, JpaSpecificationExecutor<SchoolNotice> {
    List<SchoolNotice> findTopByStatusOrderByReleaseTimeDesc(String status);
}
