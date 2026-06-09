package com.xuanyue.exp.edu.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "subject_group_researcher")
public class SubjectGroupResearcher {

    @Id
    @Column(name = "seq_id")
    private String seqId;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "researcher_user_id")
    private String researcherUserId;

    @Column(name = "create_user_id")
    private String createUserId;

    @Column(name = "create_time")
    private Date createTime;

    public String getSeqId() { return seqId; }
    public void setSeqId(String seqId) { this.seqId = seqId; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getResearcherUserId() { return researcherUserId; }
    public void setResearcherUserId(String researcherUserId) { this.researcherUserId = researcherUserId; }
    public String getCreateUserId() { return createUserId; }
    public void setCreateUserId(String createUserId) { this.createUserId = createUserId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
