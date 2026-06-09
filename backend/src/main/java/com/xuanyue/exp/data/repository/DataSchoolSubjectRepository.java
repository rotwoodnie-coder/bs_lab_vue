package com.xuanyue.exp.data.repository;

import com.xuanyue.exp.data.entity.DataSchoolSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DataSchoolSubjectRepository extends JpaRepository<DataSchoolSubject, String> {

    List<DataSchoolSubject> findBySubjectIdIn(Collection<String> subjectIds);

    List<DataSchoolSubject> findByStatusOrderBySortOrderAsc(String status);
}
