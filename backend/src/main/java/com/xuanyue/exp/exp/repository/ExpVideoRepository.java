package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpVideoRepository extends JpaRepository<ExpVideo, String> {

    List<ExpVideo> findByExpIdOrderBySortOrderAsc(String expId);

    void deleteByExpId(String expId);

    List<ExpVideo> findByExpIdIn(List<String> expIds);
}
