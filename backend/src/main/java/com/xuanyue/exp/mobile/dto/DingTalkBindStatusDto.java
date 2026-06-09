package com.xuanyue.exp.mobile.dto;

import java.util.Date;

public class DingTalkBindStatusDto {

    private boolean configured;
    private boolean bound;
    private String dingNick;
    private Date bindTime;
    private String label;

    public boolean isConfigured() { return configured; }
    public void setConfigured(boolean configured) { this.configured = configured; }
    public boolean isBound() { return bound; }
    public void setBound(boolean bound) { this.bound = bound; }
    public String getDingNick() { return dingNick; }
    public void setDingNick(String dingNick) { this.dingNick = dingNick; }
    public Date getBindTime() { return bindTime; }
    public void setBindTime(Date bindTime) { this.bindTime = bindTime; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
