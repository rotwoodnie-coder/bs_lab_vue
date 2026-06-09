package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpQuestionSelect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpQuestionSelectRepository extends JpaRepository<ExpQuestionSelect, String> {
    List<ExpQuestionSelect> findByQuestionIdOrderBySortOrderAscSelectIdAsc(String questionId);
    void deleteByQuestionId(String questionId);
}
