package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbDingtalkBind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MbDingtalkBindRepository extends JpaRepository<MbDingtalkBind, String> {

    Optional<MbDingtalkBind> findByDingUnionId(String dingUnionId);
}
