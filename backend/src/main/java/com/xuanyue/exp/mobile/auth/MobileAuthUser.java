package com.xuanyue.exp.mobile.auth;

public class MobileAuthUser {

    private final String userId;
    private final String userRoleId;
    private final String status;
    private final String rootOrgId;
    private final String loginName;
    private final String userName;

    public MobileAuthUser(String userId, String userRoleId, String status, String rootOrgId,
                          String loginName, String userName) {
        this.userId = userId;
        this.userRoleId = userRoleId;
        this.status = status;
        this.rootOrgId = rootOrgId;
        this.loginName = loginName;
        this.userName = userName;
    }

    public String getUserId() { return userId; }
    public String getUserRoleId() { return userRoleId; }
    public String getStatus() { return status; }
    public String getRootOrgId() { return rootOrgId; }
    public String getLoginName() { return loginName; }
    public String getUserName() { return userName; }
}
