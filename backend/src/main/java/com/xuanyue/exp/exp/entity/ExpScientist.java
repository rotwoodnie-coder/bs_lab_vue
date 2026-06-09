package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "exp_scientist")
public class ExpScientist {

    @Id
    @Column(name = "scientist_id")
    private String scientistId;

    @Column(name = "exp_id")
    private String expId;

    @Column(name = "scientist_name")
    private String scientistName;

    @Column(name = "story_name")
    private String storyName;

    @Column(name = "story_comments")
    private String storyComments;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public String getScientistId() { return scientistId; }
    public void setScientistId(String scientistId) { this.scientistId = scientistId; }
    public String getExpId() { return expId; }
    public void setExpId(String expId) { this.expId = expId; }
    public String getScientistName() { return scientistName; }
    public void setScientistName(String scientistName) { this.scientistName = scientistName; }
    public String getStoryName() { return storyName; }
    public void setStoryName(String storyName) { this.storyName = storyName; }
    public String getStoryComments() { return storyComments; }
    public void setStoryComments(String storyComments) { this.storyComments = storyComments; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
