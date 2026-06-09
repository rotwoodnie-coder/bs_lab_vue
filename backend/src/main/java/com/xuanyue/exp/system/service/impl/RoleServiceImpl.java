package com.xuanyue.exp.system.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.system.dto.RoleListItem;
import com.xuanyue.exp.system.dto.RoleMenuAuthResponse;
import com.xuanyue.exp.system.dto.RolePageQuery;
import com.xuanyue.exp.system.dto.RoleSaveRequest;
import com.xuanyue.exp.system.dto.RoleUpdateRequest;
import com.xuanyue.exp.system.entity.SysMenu;
import com.xuanyue.exp.system.entity.SysRole;
import com.xuanyue.exp.system.entity.SysRoleMenu;
import com.xuanyue.exp.system.repository.SysMenuRepository;
import com.xuanyue.exp.system.repository.SysRoleMenuRepository;
import com.xuanyue.exp.system.repository.SysRoleRepository;
import com.xuanyue.exp.system.service.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final SysRoleRepository sysRoleRepository;
    private final SysMenuRepository sysMenuRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;

    public RoleServiceImpl(SysRoleRepository sysRoleRepository, SysMenuRepository sysMenuRepository, SysRoleMenuRepository sysRoleMenuRepository) {
        this.sysRoleRepository = sysRoleRepository;
        this.sysMenuRepository = sysMenuRepository;
        this.sysRoleMenuRepository = sysRoleMenuRepository;
    }

    @Override
    public PageResult<RoleListItem> page(RolePageQuery query) {
        Pageable pageable = PageRequest.of(Math.max(query.getPageNum() - 1, 0), query.getPageSize());
        String keyword = StringUtils.hasText(query.getKeyword()) ? query.getKeyword().trim() : null;
        String status = StringUtils.hasText(query.getStatus()) ? query.getStatus().trim() : null;

        List<SysRole> allRoles;
        if (StringUtils.hasText(keyword) && StringUtils.hasText(status)) {
            allRoles = sysRoleRepository.findByRoleNameContainingOrRoleCodeContainingAndStatus(keyword, keyword, status);
        } else if (StringUtils.hasText(keyword)) {
            allRoles = sysRoleRepository.findByRoleNameContainingOrRoleCodeContaining(keyword, keyword);
        } else if (StringUtils.hasText(status)) {
            allRoles = sysRoleRepository.findByStatus(status);
        } else {
            allRoles = sysRoleRepository.findAll();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allRoles.size());
        List<SysRole> pageContent = start > end ? new ArrayList<SysRole>() : allRoles.subList(start, end);

        List<RoleListItem> list = new ArrayList<RoleListItem>();
        for (SysRole role : pageContent) {
            list.add(new RoleListItem(role.getRoleId(), role.getRoleCode(), role.getRoleName(), role.getStatus()));
        }

        Page<SysRole> page = new PageImpl<SysRole>(pageContent, pageable, allRoles.size());
        return new PageResult<RoleListItem>(page.getTotalElements(), list);
    }

    @Override
    public void create(RoleSaveRequest request) {
        sysRoleRepository.findByRoleCode(request.getRoleCode()).ifPresent(role -> {
            throw new RuntimeException("角色编码已存在");
        });
        SysRole role = new SysRole();
        role.setRoleId(request.getRoleCode());
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "y");
        sysRoleRepository.save(role);
    }

    @Override
    public void update(String roleId, RoleUpdateRequest request) {
        SysRole role = sysRoleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("角色不存在"));
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "y");
        sysRoleRepository.save(role);
    }

    @Override
    public void delete(String roleId) {
        SysRole role = sysRoleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("角色不存在"));
        sysRoleRepository.delete(role);
    }

    @Override
    public void updateStatus(String roleId, String status) {
        SysRole role = sysRoleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("角色不存在"));
        role.setStatus(status);
        sysRoleRepository.save(role);
    }

    @Override
    public RoleMenuAuthResponse getRoleMenus(String roleId) {
        List<SysRoleMenu> roleMenus = sysRoleMenuRepository.findByRoleId(roleId);
        List<String> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return new RoleMenuAuthResponse(roleId, menuIds);
    }

    @Override
    @Transactional
    public void saveRoleMenus(String roleId, List<String> menuIds) {
        sysRoleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("角色不存在"));
        sysRoleMenuRepository.deleteByRoleId(roleId);
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        List<SysRoleMenu> relations = new ArrayList<SysRoleMenu>();
        for (String menuId : menuIds) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            relations.add(relation);
        }
        sysRoleMenuRepository.saveAll(relations);
    }
}
