package com.xuanyue.exp.edu.repository;

import com.xuanyue.exp.edu.entity.DataCoursebookContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DataCoursebookContentRepository extends JpaRepository<DataCoursebookContent, String>, JpaSpecificationExecutor<DataCoursebookContent> {
    List<DataCoursebookContent> findByCoursebookIdOrderBySortOrderAscContentNameAsc(String coursebookId);
}
