package com.xuanyue.exp.mobile.dto;

import java.util.Date;

public class MobileAccountSecurityDto {

    private boolean phoneBound;
    private String maskedPhone;
    private boolean dingTalkConfigured;
    private boolean dingTalkBound;
    private String dingTalkLabel;
    private boolean wechatBound;
    private boolean wechatSupported;
    private boolean parentBound;
    private int parentBindCount;
    private int parentPendingCount;
    private String parentBindLabel;
    private Date lastLoginTime;
    private String lastLoginLabel;
    private String currentDeviceLabel;

    public boolean isPhoneBound() { return phoneBound; }
    public void setPhoneBound(boolean phoneBound) { this.phoneBound = phoneBound; }
    public String getMaskedPhone() { return maskedPhone; }
    public void setMaskedPhone(String maskedPhone) { this.maskedPhone = maskedPhone; }
    public boolean isDingTalkConfigured() { return dingTalkConfigured; }
    public void setDingTalkConfigured(boolean dingTalkConfigured) { this.dingTalkConfigured = dingTalkConfigured; }
    public boolean isDingTalkBound() { return dingTalkBound; }
    public void setDingTalkBound(boolean dingTalkBound) { this.dingTalkBound = dingTalkBound; }
    public String getDingTalkLabel() { return dingTalkLabel; }
    public void setDingTalkLabel(String dingTalkLabel) { this.dingTalkLabel = dingTalkLabel; }
    public boolean isWechatBound() { return wechatBound; }
    public void setWechatBound(boolean wechatBound) { this.wechatBound = wechatBound; }
    public boolean isWechatSupported() { return wechatSupported; }
    public void setWechatSupported(boolean wechatSupported) { this.wechatSupported = wechatSupported; }
    public boolean isParentBound() { return parentBound; }
    public void setParentBound(boolean parentBound) { this.parentBound = parentBound; }
    public int getParentBindCount() { return parentBindCount; }
    public void setParentBindCount(int parentBindCount) { this.parentBindCount = parentBindCount; }
    public int getParentPendingCount() { return parentPendingCount; }
    public void setParentPendingCount(int parentPendingCount) { this.parentPendingCount = parentPendingCount; }
    public String getParentBindLabel() { return parentBindLabel; }
    public void setParentBindLabel(String parentBindLabel) { this.parentBindLabel = parentBindLabel; }
    public Date getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(Date lastLoginTime) { this.lastLoginTime = lastLoginTime; }
    public String getLastLoginLabel() { return lastLoginLabel; }
    public void setLastLoginLabel(String lastLoginLabel) { this.lastLoginLabel = lastLoginLabel; }
    public String getCurrentDeviceLabel() { return currentDeviceLabel; }
    public void setCurrentDeviceLabel(String currentDeviceLabel) { this.currentDeviceLabel = currentDeviceLabel; }
}
