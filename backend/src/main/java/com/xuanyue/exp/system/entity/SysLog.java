package com.xuanyue.exp.system.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "sys_log")
public class SysLog {

    @Id
    @Column(name = "log_id")
    private String logId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "log_type")
    private String logType;

    @Column(name = "log_time")
    private Date logTime;

    @Column(name = "log_data_type")
    private String logDataType;

    @Column(name = "log_data_id")
    private String logDataId;

    @Column(name = "log_data_content")
    private String logDataContent;

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }
    public Date getLogTime() { return logTime; }
    public void setLogTime(Date logTime) { this.logTime = logTime; }
    public String getLogDataType() { return logDataType; }
    public void setLogDataType(String logDataType) { this.logDataType = logDataType; }
    public String getLogDataId() { return logDataId; }
    public void setLogDataId(String logDataId) { this.logDataId = logDataId; }
    public String getLogDataContent() { return logDataContent; }
    public void setLogDataContent(String logDataContent) { this.logDataContent = logDataContent; }
}
