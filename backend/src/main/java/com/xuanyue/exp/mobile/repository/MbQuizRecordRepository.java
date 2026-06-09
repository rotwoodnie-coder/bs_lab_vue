package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbQuizRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface MbQuizRecordRepository extends JpaRepository<MbQuizRecord, String> {
    List<MbQuizRecord> findByUserIdOrderByQuizDateDesc(String userId);
    Optional<MbQuizRecord> findByUserIdAndQuizDate(String userId, Date quizDate);
    List<MbQuizRecord> findByUserIdAndQuizDateGreaterThanEqualOrderByQuizDateDesc(String userId, Date fromDate);
    long countByUserId(String userId);

    @Query("SELECT COALESCE(SUM(r.score), 0) FROM MbQuizRecord r WHERE r.userId = ?1")
    long sumScoreByUserId(String userId);
}
