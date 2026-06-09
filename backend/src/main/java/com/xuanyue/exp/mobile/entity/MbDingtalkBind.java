package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "mb_dingtalk_bind")
public class MbDingtalkBind {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "ding_union_id")
    private String dingUnionId;

    @Column(name = "ding_open_id")
    private String dingOpenId;

    @Column(name = "ding_nick")
    private String dingNick;

    @Column(name = "bind_time")
    private Date bindTime;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDingUnionId() { return dingUnionId; }
    public void setDingUnionId(String dingUnionId) { this.dingUnionId = dingUnionId; }
    public String getDingOpenId() { return dingOpenId; }
    public void setDingOpenId(String dingOpenId) { this.dingOpenId = dingOpenId; }
    public String getDingNick() { return dingNick; }
    public void setDingNick(String dingNick) { this.dingNick = dingNick; }
    public Date getBindTime() { return bindTime; }
    public void setBindTime(Date bindTime) { this.bindTime = bindTime; }
}
