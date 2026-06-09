package com.xuanyue.exp.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "material_log")
public class MaterialLog {

    @Id
    @Column(name = "log_id")
    private String logId;

    @Column(name = "material_id")
    private String materialId;

    @Column(name = "log_type")
    private String logType;

    @Column(name = "log_type_name")
    private String logTypeName;

    @Column(name = "log_user_id")
    private String logUserId;

    @Column(name = "log_user_name")
    private String logUserName;

    @Column(name = "log_time")
    private Date logTime;

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }
    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }
    public String getLogTypeName() { return logTypeName; }
    public void setLogTypeName(String logTypeName) { this.logTypeName = logTypeName; }
    public String getLogUserId() { return logUserId; }
    public void setLogUserId(String logUserId) { this.logUserId = logUserId; }
    public String getLogUserName() { return logUserName; }
    public void setLogUserName(String logUserName) { this.logUserName = logUserName; }
    public Date getLogTime() { return logTime; }
    public void setLogTime(Date logTime) { this.logTime = logTime; }
}
