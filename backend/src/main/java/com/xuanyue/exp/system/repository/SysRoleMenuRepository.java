package com.xuanyue.exp.system.repository;

import com.xuanyue.exp.system.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, String> {

    List<SysRoleMenu> findByRoleId(String roleId);

    void deleteByRoleId(String roleId);
}
