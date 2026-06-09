package com.xuanyue.exp.system.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.system.dto.MenuListItem;
import com.xuanyue.exp.system.dto.MenuPageQuery;
import com.xuanyue.exp.system.dto.MenuTreeItem;
import com.xuanyue.exp.system.dto.MenuSaveRequest;
import com.xuanyue.exp.system.dto.MenuUpdateRequest;
import com.xuanyue.exp.system.entity.SysMenu;
import com.xuanyue.exp.system.entity.SysRoleMenu;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.entity.SysUserRole;
import com.xuanyue.exp.system.repository.SysMenuRepository;
import com.xuanyue.exp.system.repository.SysRoleMenuRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import com.xuanyue.exp.system.repository.SysUserRoleRepository;
import com.xuanyue.exp.system.service.MenuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MenuServiceImpl implements MenuService {

    private final SysMenuRepository sysMenuRepository;
    private final SysUserRepository sysUserRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;

    public MenuServiceImpl(SysMenuRepository sysMenuRepository,
                           SysUserRepository sysUserRepository,
                           SysUserRoleRepository sysUserRoleRepository,
                           SysRoleMenuRepository sysRoleMenuRepository) {
        this.sysMenuRepository = sysMenuRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.sysRoleMenuRepository = sysRoleMenuRepository;
    }

    @Override
    public PageResult<MenuListItem> page(MenuPageQuery query) {
        Pageable pageable = PageRequest.of(Math.max(query.getPageNum() - 1, 0), query.getPageSize());
        String keyword = StringUtils.hasText(query.getKeyword()) ? query.getKeyword().trim() : null;
        String status = StringUtils.hasText(query.getStatus()) ? query.getStatus().trim() : null;

        List<SysMenu> allMenus;
        if (StringUtils.hasText(keyword) && StringUtils.hasText(status)) {
            allMenus = sysMenuRepository.findByMenuNameContainingOrMenuCodeContainingAndStatus(keyword, keyword, status);
        } else if (StringUtils.hasText(keyword)) {
            allMenus = sysMenuRepository.findByMenuNameContainingOrMenuCodeContaining(keyword, keyword);
        } else if (StringUtils.hasText(status)) {
            allMenus = sysMenuRepository.findByStatus(status);
        } else {
            allMenus = sysMenuRepository.findAll();
        }

        allMenus.sort((a, b) -> {
            String parentA = a.getParentId() == null ? "" : a.getParentId();
            String parentB = b.getParentId() == null ? "" : b.getParentId();
            int parentCompare = parentA.compareTo(parentB);
            if (parentCompare != 0) {
                return parentCompare;
            }
            Integer sortA = a.getSortOrder() == null ? 0 : a.getSortOrder();
            Integer sortB = b.getSortOrder() == null ? 0 : b.getSortOrder();
            return sortA.compareTo(sortB);
        });

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allMenus.size());
        List<SysMenu> pageContent = start > end ? new ArrayList<SysMenu>() : allMenus.subList(start, end);

        List<MenuListItem> list = new ArrayList<MenuListItem>();
        for (SysMenu menu : pageContent) {
            list.add(new MenuListItem(menu.getMenuId(), menu.getParentId(), menu.getMenuName(), menu.getMenuCode(), menu.getMenuType(), menu.getPath(), menu.getComponent(), menu.getSortOrder(), menu.getStatus(), menu.getComments()));
        }

        Page<SysMenu> page = new PageImpl<SysMenu>(pageContent, pageable, allMenus.size());
        return new PageResult<MenuListItem>(page.getTotalElements(), list);
    }

    @Override
    public List<MenuTreeItem> visibleMenus(String userId) {
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ArrayList<MenuTreeItem>();
        }
        Set<String> roleIds = new LinkedHashSet<String>();
        List<SysUserRole> userRoles = sysUserRoleRepository.findByUserId(userId);
        for (SysUserRole userRole : userRoles) {
            if (userRole != null && StringUtils.hasText(userRole.getRoleId())) {
                roleIds.add(userRole.getRoleId());
            }
        }
        Set<String> menuIds = new LinkedHashSet<String>();
        for (String roleId : roleIds) {
            List<SysRoleMenu> roleMenus = sysRoleMenuRepository.findByRoleId(roleId);
            for (SysRoleMenu roleMenu : roleMenus) {
                if (roleMenu != null && StringUtils.hasText(roleMenu.getMenuId())) {
                    menuIds.add(roleMenu.getMenuId());
                }
            }
        }
        List<SysMenu> menus = sysMenuRepository.findAllById(menuIds);
        Map<String, SysMenu> menuMap = new LinkedHashMap<String, SysMenu>();
        for (SysMenu menu : menus) {
            menuMap.put(menu.getMenuId(), menu);
        }
        List<SysMenu> ordered = new ArrayList<SysMenu>(menuMap.values());
        ordered.sort(new Comparator<SysMenu>() {
            @Override
            public int compare(SysMenu a, SysMenu b) {
                String parentA = a.getParentId() == null ? "" : a.getParentId();
                String parentB = b.getParentId() == null ? "" : b.getParentId();
                int parentCompare = parentA.compareTo(parentB);
                if (parentCompare != 0) {
                    return parentCompare;
                }
                Integer sortA = a.getSortOrder() == null ? 0 : a.getSortOrder();
                Integer sortB = b.getSortOrder() == null ? 0 : b.getSortOrder();
                return sortA.compareTo(sortB);
            }
        });

        Map<String, MenuTreeItem> nodeMap = new LinkedHashMap<String, MenuTreeItem>();
        List<MenuTreeItem> roots = new ArrayList<MenuTreeItem>();
        for (SysMenu menu : ordered) {
            MenuTreeItem node = new MenuTreeItem();
            node.setKey(menu.getMenuId());
            node.setPath(menu.getPath());
            node.setLabel(menu.getMenuName());
            node.setIcon(menu.getComponent());
            nodeMap.put(menu.getMenuId(), node);
        }
        for (SysMenu menu : ordered) {
            MenuTreeItem node = nodeMap.get(menu.getMenuId());
            if (!StringUtils.hasText(menu.getParentId()) || !nodeMap.containsKey(menu.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(menu.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    public void create(MenuSaveRequest request) {
        sysMenuRepository.findByMenuCode(request.getMenuCode()).ifPresent(menu -> {
            throw new RuntimeException("菜单编码已存在");
        });
        SysMenu menu = new SysMenu();
        menu.setMenuId(UUID.randomUUID().toString().replace("-", ""));
        menu.setParentId(request.getParentId());
        menu.setMenuName(request.getMenuName());
        menu.setMenuCode(request.getMenuCode());
        menu.setMenuType(request.getMenuType());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        menu.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "y");
        menu.setComments(request.getComments());
        sysMenuRepository.save(menu);
    }

    @Override
    public void update(String menuId, MenuUpdateRequest request) {
        SysMenu menu = sysMenuRepository.findById(menuId).orElseThrow(() -> new RuntimeException("菜单不存在"));
        menu.setParentId(request.getParentId());
        menu.setMenuName(request.getMenuName());
        menu.setMenuCode(request.getMenuCode());
        menu.setMenuType(request.getMenuType());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        menu.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "y");
        menu.setComments(request.getComments());
        sysMenuRepository.save(menu);
    }

    @Override
    public void delete(String menuId) {
        SysMenu menu = sysMenuRepository.findById(menuId).orElseThrow(() -> new RuntimeException("菜单不存在"));
        sysMenuRepository.delete(menu);
    }

    @Override
    public void updateStatus(String menuId, String status) {
        SysMenu menu = sysMenuRepository.findById(menuId).orElseThrow(() -> new RuntimeException("菜单不存在"));
        menu.setStatus(status);
        sysMenuRepository.save(menu);
    }

    private String resolveIcon(SysMenu menu) {
        String code = menu.getMenuCode();
        if (!StringUtils.hasText(code)) {
            return "HomeFilled";
        }
        if (code.contains("exp")) return "Aim";
        if (code.contains("course") || code.contains("teacher") || code.contains("subject")) return "Reading";
        if (code.contains("file") || code.contains("material") || code.contains("data")) return "FolderOpened";
        if (code.contains("user")) return "User";
        if (code.contains("role")) return "Avatar";
        if (code.contains("menu")) return "Menu";
        if (code.contains("org")) return "OfficeBuilding";
        if (code.contains("dict")) return "DataAnalysis";
        if (code.contains("log")) return "Document";
        if (code.contains("msg")) return "ChatDotRound";
        return "HomeFilled";
    }
}
