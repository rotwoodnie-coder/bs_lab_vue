package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbQuizDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MbQuizDailyRepository extends JpaRepository<MbQuizDaily, String> {

    Optional<MbQuizDaily> findByQuizDateAndStatus(java.sql.Date quizDate, String status);
}
