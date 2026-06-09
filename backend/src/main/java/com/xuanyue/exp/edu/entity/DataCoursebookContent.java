package com.xuanyue.exp.edu.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "data_coursebook_content")
public class DataCoursebookContent {

    @Id
    @Column(name = "content_id")
    private String contentId;

    @Column(name = "content_name")
    private String contentName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "coursebook_id")
    private String coursebookId;

    @Column(name = "comments")
    private String comments;

    @Column(name = "status")
    private String status;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public String getContentName() { return contentName; }
    public void setContentName(String contentName) { this.contentName = contentName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getCoursebookId() { return coursebookId; }
    public void setCoursebookId(String coursebookId) { this.coursebookId = coursebookId; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
