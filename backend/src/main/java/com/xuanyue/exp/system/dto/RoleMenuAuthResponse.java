package com.xuanyue.exp.system.dto;

import java.util.List;

public class RoleMenuAuthResponse {

    private String roleId;
    private List<String> menuIds;

    public RoleMenuAuthResponse() {
    }

    public RoleMenuAuthResponse(String roleId, List<String> menuIds) {
        this.roleId = roleId;
        this.menuIds = menuIds;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public List<String> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<String> menuIds) {
        this.menuIds = menuIds;
    }
}
