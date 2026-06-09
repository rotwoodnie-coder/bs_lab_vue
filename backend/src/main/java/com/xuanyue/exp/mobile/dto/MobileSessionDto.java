package com.xuanyue.exp.mobile.dto;

public class MobileSessionDto {

    private String userId;
    private String username;
    private String loginName;
    private String userRoleId;
    private String status;
    private String rootOrgId;
    private String rootOrgName;
    private Boolean parentRestricted;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getUserRoleId() { return userRoleId; }
    public void setUserRoleId(String userRoleId) { this.userRoleId = userRoleId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRootOrgId() { return rootOrgId; }
    public void setRootOrgId(String rootOrgId) { this.rootOrgId = rootOrgId; }
    public String getRootOrgName() { return rootOrgName; }
    public void setRootOrgName(String rootOrgName) { this.rootOrgName = rootOrgName; }
    public Boolean getParentRestricted() { return parentRestricted; }
    public void setParentRestricted(Boolean parentRestricted) { this.parentRestricted = parentRestricted; }
}
