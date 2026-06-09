package com.xuanyue.exp.system.dto;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class UserRoleAuthRequest {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    private List<String> roleIds;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }
}
