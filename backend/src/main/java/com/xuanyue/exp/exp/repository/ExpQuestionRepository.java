package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ExpQuestionRepository extends JpaRepository<ExpQuestion, String>, JpaSpecificationExecutor<ExpQuestion> {

    List<ExpQuestion> findByStatusOrderByQuestionIdAsc(String status);
}
