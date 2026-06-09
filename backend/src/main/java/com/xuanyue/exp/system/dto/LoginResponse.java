package com.xuanyue.exp.system.dto;

public class LoginResponse {

    private String token;
    private String userId;
    private String username;
    private String loginName;
    private String rootOrgId;
    private String rootOrgName;
    private String userRoleId;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;
    private String status;
    private boolean parentRestricted;

    public LoginResponse() {
    }

    public LoginResponse(String token, String userId, String username, String loginName) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.loginName = loginName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRootOrgId() {
        return rootOrgId;
    }

    public void setRootOrgId(String rootOrgId) {
        this.rootOrgId = rootOrgId;
    }

    public String getRootOrgName() {
        return rootOrgName;
    }

    public void setRootOrgName(String rootOrgName) {
        this.rootOrgName = rootOrgName;
    }

    public String getUserRoleId() {
        return userRoleId;
    }

    public void setUserRoleId(String userRoleId) {
        this.userRoleId = userRoleId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isParentRestricted() {
        return parentRestricted;
    }

    public void setParentRestricted(boolean parentRestricted) {
        this.parentRestricted = parentRestricted;
    }
}
