package com.xuanyue.exp.system.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.system.dto.MenuListItem;
import com.xuanyue.exp.system.dto.MenuPageQuery;
import com.xuanyue.exp.system.dto.MenuTreeItem;
import com.xuanyue.exp.system.dto.MenuSaveRequest;
import com.xuanyue.exp.system.dto.MenuUpdateRequest;

import java.util.List;

public interface MenuService {
    PageResult<MenuListItem> page(MenuPageQuery query);
    List<MenuTreeItem> visibleMenus(String userId);
    void create(MenuSaveRequest request);
    void update(String menuId, MenuUpdateRequest request);
    void delete(String menuId);
    void updateStatus(String menuId, String status);
}
