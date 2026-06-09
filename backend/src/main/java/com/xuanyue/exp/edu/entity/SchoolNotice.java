package com.xuanyue.exp.edu.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "school_notice")
public class SchoolNotice {

    @Id
    @Column(name = "notice_id")
    private String noticeId;

    @Column(name = "notice_name")
    private String noticeName;

    @Column(name = "notice_content")
    private String noticeContent;

    @Column(name = "notice_org_id")
    private String noticeOrgId;

    @Column(name = "status")
    private String status;

    @Column(name = "create_user_id")
    private String createUserId;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_user_id")
    private String updateUserId;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "release_user_id")
    private String releaseUserId;

    @Column(name = "release_time")
    private Date releaseTime;

    public String getNoticeId() { return noticeId; }
    public void setNoticeId(String noticeId) { this.noticeId = noticeId; }
    public String getNoticeName() { return noticeName; }
    public void setNoticeName(String noticeName) { this.noticeName = noticeName; }
    public String getNoticeContent() { return noticeContent; }
    public void setNoticeContent(String noticeContent) { this.noticeContent = noticeContent; }
    public String getNoticeOrgId() { return noticeOrgId; }
    public void setNoticeOrgId(String noticeOrgId) { this.noticeOrgId = noticeOrgId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateUserId() { return createUserId; }
    public void setCreateUserId(String createUserId) { this.createUserId = createUserId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getUpdateUserId() { return updateUserId; }
    public void setUpdateUserId(String updateUserId) { this.updateUserId = updateUserId; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public String getReleaseUserId() { return releaseUserId; }
    public void setReleaseUserId(String releaseUserId) { this.releaseUserId = releaseUserId; }
    public Date getReleaseTime() { return releaseTime; }
    public void setReleaseTime(Date releaseTime) { this.releaseTime = releaseTime; }
}
