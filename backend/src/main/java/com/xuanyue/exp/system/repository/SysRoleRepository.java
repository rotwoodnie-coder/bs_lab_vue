package com.xuanyue.exp.system.repository;

import com.xuanyue.exp.system.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysRoleRepository extends JpaRepository<SysRole, String> {

    Optional<SysRole> findByRoleCode(String roleCode);

    List<SysRole> findByRoleNameContainingOrRoleCodeContaining(String roleName, String roleCode);

    List<SysRole> findByStatus(String status);

    List<SysRole> findByRoleNameContainingOrRoleCodeContainingAndStatus(String roleName, String roleCode, String status);
}
