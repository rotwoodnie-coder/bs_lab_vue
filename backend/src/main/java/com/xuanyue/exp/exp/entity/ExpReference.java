package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "exp_reference")
public class ExpReference {

    @Id
    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "exp_id")
    private String expId;

    @Column(name = "reference_name")
    private String referenceName;

    @Column(name = "reference_source")
    private String referenceSource;

    @Column(name = "reference_comments")
    private String referenceComments;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getExpId() { return expId; }
    public void setExpId(String expId) { this.expId = expId; }
    public String getReferenceName() { return referenceName; }
    public void setReferenceName(String referenceName) { this.referenceName = referenceName; }
    public String getReferenceSource() { return referenceSource; }
    public void setReferenceSource(String referenceSource) { this.referenceSource = referenceSource; }
    public String getReferenceComments() { return referenceComments; }
    public void setReferenceComments(String referenceComments) { this.referenceComments = referenceComments; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
