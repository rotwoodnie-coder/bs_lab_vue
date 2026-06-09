package com.xuanyue.exp.system.repository;

import com.xuanyue.exp.system.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysUserRoleRepository extends JpaRepository<SysUserRole, SysUserRole.SysUserRoleId> {

    List<SysUserRole> findByUserId(String userId);

    void deleteByUserId(String userId);
}
