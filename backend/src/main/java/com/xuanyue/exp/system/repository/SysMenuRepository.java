package com.xuanyue.exp.system.repository;

import com.xuanyue.exp.system.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysMenuRepository extends JpaRepository<SysMenu, String> {

    Optional<SysMenu> findByMenuCode(String menuCode);

    List<SysMenu> findByMenuNameContainingOrMenuCodeContaining(String menuName, String menuCode);

    List<SysMenu> findByStatus(String status);

    List<SysMenu> findByMenuNameContainingOrMenuCodeContainingAndStatus(String menuName, String menuCode, String status);
}
