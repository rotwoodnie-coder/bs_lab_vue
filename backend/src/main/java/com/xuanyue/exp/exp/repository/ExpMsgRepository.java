package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpMsg;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExpMsgRepository extends JpaRepository<ExpMsg, String>, JpaSpecificationExecutor<ExpMsg> {

    Optional<ExpMsg> findTopByCreateUserIdAndStatusAndExpTypeOrderByCreateTimeDesc(String createUserId, String status, String expType);

    Optional<ExpMsg> findFirstByExpNameAndStatus(String expName, String status);

    @Query("SELECT e FROM ExpMsg e WHERE e.status = ?1 AND e.expName LIKE %?2%")
    List<ExpMsg> findTitleMatches(String status, String title, PageRequest pageRequest);
}
