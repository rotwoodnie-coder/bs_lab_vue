package com.xuanyue.exp.mobile.dto;

import java.util.List;

/**
 * 移动端 v2 教师布置作业请求
 */
public class AssignHomeworkRequest {

    private String experimentId;
    private String classId;
    private String requireDate;
    private String requirements;

    public String getExperimentId() { return experimentId; }
    public void setExperimentId(String experimentId) { this.experimentId = experimentId; }
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public String getRequireDate() { return requireDate; }
    public void setRequireDate(String requireDate) { this.requireDate = requireDate; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
}
