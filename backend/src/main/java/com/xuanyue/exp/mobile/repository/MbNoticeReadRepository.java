package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbNoticeRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MbNoticeReadRepository extends JpaRepository<MbNoticeRead, MbNoticeRead.NoticeReadId> {

    boolean existsByUserIdAndNoticeId(String userId, String noticeId);

    List<MbNoticeRead> findByUserIdOrderByReadTimeDesc(String userId);
}
