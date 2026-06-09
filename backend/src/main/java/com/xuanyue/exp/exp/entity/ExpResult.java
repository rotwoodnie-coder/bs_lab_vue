package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "exp_result")
public class ExpResult {

    @Id
    @Column(name = "result_id")
    private String resultId;

    @Column(name = "exp_id")
    private String expId;

    @Column(name = "result_name")
    private String resultName;

    @Column(name = "result_comments")
    private String resultComments;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getExpId() { return expId; }
    public void setExpId(String expId) { this.expId = expId; }
    public String getResultName() { return resultName; }
    public void setResultName(String resultName) { this.resultName = resultName; }
    public String getResultComments() { return resultComments; }
    public void setResultComments(String resultComments) { this.resultComments = resultComments; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
