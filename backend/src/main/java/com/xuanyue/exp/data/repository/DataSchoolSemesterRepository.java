package com.xuanyue.exp.data.repository;

import com.xuanyue.exp.data.entity.DataSchoolSemester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DataSchoolSemesterRepository extends JpaRepository<DataSchoolSemester, String> {

    List<DataSchoolSemester> findBySemesterIdIn(Collection<String> semesterIds);

    List<DataSchoolSemester> findByStatusOrderBySortOrderAsc(String status);
}
