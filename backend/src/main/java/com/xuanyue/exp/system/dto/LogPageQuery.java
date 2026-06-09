package com.xuanyue.exp.system.dto;

public class LogPageQuery {
    private int pageNum = 1;
    private int pageSize = 10;
    private String startTime;
    private String endTime;
    private String logType;

    public int getPageNum() { return pageNum; }
    public void setPageNum(int pageNum) { this.pageNum = pageNum; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }
}
