package com.xuanyue.exp.system.dto;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class RoleMenuAuthRequest {

    @NotBlank(message = "角色ID不能为空")
    private String roleId;

    private List<String> menuIds;

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
