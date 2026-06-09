package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "exp_log")
public class ExpLog {

    @Id
    @Column(name = "log_id")
    private String logId;

    @Column(name = "exp_id")
    private String expId;

    @Column(name = "log_type")
    private String logType;

    @Column(name = "log_type_name")
    private String logTypeName;

    @Column(name = "log_comments")
    private String logComments;

    @Column(name = "log_user_id")
    private String logUserId;

    @Column(name = "log_user_name")
    private String logUserName;

    @Column(name = "log_time")
    private Date logTime;

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getExpId() { return expId; }
    public void setExpId(String expId) { this.expId = expId; }
    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }
    public String getLogTypeName() { return logTypeName; }
    public void setLogTypeName(String logTypeName) { this.logTypeName = logTypeName; }
    public String getLogComments() { return logComments; }
    public void setLogComments(String logComments) { this.logComments = logComments; }
    public String getLogUserId() { return logUserId; }
    public void setLogUserId(String logUserId) { this.logUserId = logUserId; }
    public String getLogUserName() { return logUserName; }
    public void setLogUserName(String logUserName) { this.logUserName = logUserName; }
    public Date getLogTime() { return logTime; }
    public void setLogTime(Date logTime) { this.logTime = logTime; }
}
