package com.xuanyue.exp.mobile.admin.dto;

public class ParentRegistrationAuditRequest {

    /** approve -> status=y, reject -> status=n */
    private String action;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
