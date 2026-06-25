package com.xuanyue.exp.mobile.dto;

/**
 * 积分明细（流水）列表项
 */
public class MobilePointsLedgerItemDto {

    private String id;
    private int delta;
    private Integer balanceAfter;
    private String sourceType;
    private String sourceTypeLabel;
    private String remark;
    private String time;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getDelta() { return delta; }
    public void setDelta(int delta) { this.delta = delta; }

    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourceTypeLabel() { return sourceTypeLabel; }
    public void setSourceTypeLabel(String sourceTypeLabel) { this.sourceTypeLabel = sourceTypeLabel; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
