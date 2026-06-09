package com.xuanyue.exp.edu.repository;

import com.xuanyue.exp.edu.entity.DataCoursebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DataCoursebookRepository extends JpaRepository<DataCoursebook, String>, JpaSpecificationExecutor<DataCoursebook> {
}
