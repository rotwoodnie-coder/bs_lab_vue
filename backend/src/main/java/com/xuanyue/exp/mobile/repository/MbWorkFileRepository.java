package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbWorkFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MbWorkFileRepository extends JpaRepository<MbWorkFile, String> {
    List<MbWorkFile> findByWorkIdOrderBySortOrderAsc(String workId);
}
