package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MobileExpHomework;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MobileExpHomeworkRepository extends JpaRepository<MobileExpHomework, String> {

    List<MobileExpHomework> findByTearcherUserIdAndCreateTimeIsNotNullOrderByCreateTimeDesc(String tearcherUserId);

    List<MobileExpHomework> findByCreateTimeIsNotNullOrderByCreateTimeDesc();
}
