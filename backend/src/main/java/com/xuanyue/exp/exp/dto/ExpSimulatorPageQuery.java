package com.xuanyue.exp.exp.dto;

public class ExpSimulatorPageQuery {

    private String keyword;
    private String status;
    private String subjectId;
    private String gradeKey;
    private Integer pageNum;
    private Integer pageSize;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getGradeKey() { return gradeKey; }
    public void setGradeKey(String gradeKey) { this.gradeKey = gradeKey; }
}
