package com.xuanyue.exp.mobile.dto;

public class MobileRefreshTokenRequest {

    private String refreshToken;
    private String deviceId;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}
