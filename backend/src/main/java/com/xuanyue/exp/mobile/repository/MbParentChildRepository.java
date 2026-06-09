package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbParentChild;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MbParentChildRepository extends JpaRepository<MbParentChild, String> {
    List<MbParentChild> findByParentUserIdAndBindStatusOrderByIsDefaultDesc(String parentUserId, String bindStatus);
    List<MbParentChild> findByParentUserIdOrderByIsDefaultDesc(String parentUserId);
    Optional<MbParentChild> findByParentUserIdAndChildUserId(String parentUserId, String childUserId);
    long countByParentUserId(String parentUserId);
    List<MbParentChild> findByChildUserIdOrderByIsDefaultDesc(String childUserId);
    long countByChildUserIdAndBindStatus(String childUserId, String bindStatus);
    List<MbParentChild> findByChildUserIdIn(List<String> childUserIds);
    List<MbParentChild> findByBindStatusIgnoreCaseOrderByCreateTimeDesc(String bindStatus);
    List<MbParentChild> findByClassOrgIdInAndBindStatusIgnoreCaseOrderByCreateTimeDesc(
            List<String> classOrgIds, String bindStatus);
}
