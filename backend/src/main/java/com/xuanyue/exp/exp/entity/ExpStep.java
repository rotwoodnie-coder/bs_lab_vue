package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "exp_step")
public class ExpStep {

    @Id
    @Column(name = "step_id")
    private String stepId;

    @Column(name = "exp_id")
    private String expId;

    @Column(name = "step_name")
    private String stepName;

    @Column(name = "step_comments")
    private String stepComments;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }
    public String getExpId() { return expId; }
    public void setExpId(String expId) { this.expId = expId; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getStepComments() { return stepComments; }
    public void setStepComments(String stepComments) { this.stepComments = stepComments; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
