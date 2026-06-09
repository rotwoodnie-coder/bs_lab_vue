package com.xuanyue.exp.system.dto;

import java.util.List;

public class UserRoleAuthResponse {

    private String userId;
    private List<String> roleIds;

    public UserRoleAuthResponse() {
    }

    public UserRoleAuthResponse(String userId, List<String> roleIds) {
        this.userId = userId;
        this.roleIds = roleIds;
    }

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
