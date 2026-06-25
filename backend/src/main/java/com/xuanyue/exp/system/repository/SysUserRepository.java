package com.xuanyue.exp.system.repository;

import com.xuanyue.exp.system.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, String> {

    Optional<SysUser> findByLoginName(String loginName);

    Optional<SysUser> findByLoginNameAndStatus(String loginName, String status);

    List<SysUser> findByUserNameContainingOrLoginNameContaining(String userName, String loginName);

    List<SysUser> findByUserOrgIdAndStatus(String userOrgId, String status);

    List<SysUser> findByUserOrgIdAndStatusAndUserRoleId(String userOrgId, String status, String userRoleId);

    List<SysUser> findByStatus(String status);

    List<SysUser> findByRootOrgId(String rootOrgId);

    List<SysUser> findByUserNameContainingOrLoginNameContainingAndStatus(String userName, String loginName, String status);

    Optional<SysUser> findByUserPhone(String userPhone);
}
