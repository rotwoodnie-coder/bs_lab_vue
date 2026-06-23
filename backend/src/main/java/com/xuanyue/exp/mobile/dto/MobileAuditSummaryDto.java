package com.xuanyue.exp.mobile.dto;

public class MobileAuditSummaryDto {

    private int pendingWorkReviews;
    private int pendingParentBinds;
    private int pendingExpAudits;
    /** 家长注册待审核（管理员） */
    private int pendingParentRegistrations;

    public int getPendingWorkReviews() { return pendingWorkReviews; }
    public void setPendingWorkReviews(int pendingWorkReviews) { this.pendingWorkReviews = pendingWorkReviews; }
    public int getPendingParentBinds() { return pendingParentBinds; }
    public void setPendingParentBinds(int pendingParentBinds) { this.pendingParentBinds = pendingParentBinds; }
    public int getPendingExpAudits() { return pendingExpAudits; }
    public void setPendingExpAudits(int pendingExpAudits) { this.pendingExpAudits = pendingExpAudits; }
    public int getPendingParentRegistrations() { return pendingParentRegistrations; }
    public void setPendingParentRegistrations(int pendingParentRegistrations) {
        this.pendingParentRegistrations = pendingParentRegistrations;
    }

    public int getTotal() {
        return pendingWorkReviews + pendingParentBinds + pendingExpAudits + pendingParentRegistrations;
    }
}
