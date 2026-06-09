package com.xuanyue.exp.system.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.system.dto.RoleListItem;
import com.xuanyue.exp.system.dto.RoleMenuAuthResponse;
import com.xuanyue.exp.system.dto.RolePageQuery;
import com.xuanyue.exp.system.dto.RoleSaveRequest;
import com.xuanyue.exp.system.dto.RoleUpdateRequest;

public interface RoleService {

    PageResult<RoleListItem> page(RolePageQuery query);

    void create(RoleSaveRequest request);

    void update(String roleId, RoleUpdateRequest request);

    void delete(String roleId);

    void updateStatus(String roleId, String status);

    RoleMenuAuthResponse getRoleMenus(String roleId);

    void saveRoleMenus(String roleId, java.util.List<String> menuIds);
}
