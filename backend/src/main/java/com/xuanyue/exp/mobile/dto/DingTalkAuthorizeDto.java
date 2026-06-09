package com.xuanyue.exp.mobile.dto;

public class DingTalkAuthorizeDto {

    private boolean configured;
    private String authorizeUrl;
    private String state;
    private String redirectUri;
    private String message;

    public boolean isConfigured() { return configured; }
    public void setConfigured(boolean configured) { this.configured = configured; }
    public String getAuthorizeUrl() { return authorizeUrl; }
    public void setAuthorizeUrl(String authorizeUrl) { this.authorizeUrl = authorizeUrl; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
