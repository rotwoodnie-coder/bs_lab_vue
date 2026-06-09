package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "exp_question_select")
public class ExpQuestionSelect {

    @Id
    @Column(name = "select_id")
    private String selectId;

    @Column(name = "question_id")
    private String questionId;

    @Column(name = "select_content")
    private String selectContent;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_right")
    private String isRight;

    public String getSelectId() { return selectId; }
    public void setSelectId(String selectId) { this.selectId = selectId; }
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getSelectContent() { return selectContent; }
    public void setSelectContent(String selectContent) { this.selectContent = selectContent; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getIsRight() { return isRight; }
    public void setIsRight(String isRight) { this.isRight = isRight; }
}
