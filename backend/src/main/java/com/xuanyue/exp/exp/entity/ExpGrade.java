package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "exp_grade")
public class ExpGrade {

    @Id
    @Column(name = "seq_id")
    private String seqId;

    @Column(name = "exp_id")
    private String expId;

    @Column(name = "grade_id")
    private String gradeId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public String getSeqId() { return seqId; }
    public void setSeqId(String seqId) { this.seqId = seqId; }
    public String getExpId() { return expId; }
    public void setExpId(String expId) { this.expId = expId; }
    public String getGradeId() { return gradeId; }
    public void setGradeId(String gradeId) { this.gradeId = gradeId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
