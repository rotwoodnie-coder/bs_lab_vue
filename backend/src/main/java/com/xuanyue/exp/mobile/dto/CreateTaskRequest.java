package com.xuanyue.exp.mobile.dto;

public class CreateTaskRequest {

    private String className;
    /** 班级组织 id（sys_org） */
    private String classOrgId;
    private String experimentTitle;
    private String deadline;
    private String requirements;
    private String taskType;

    private String experimentId;
    private String simulatorId;

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getClassOrgId() { return classOrgId; }
    public void setClassOrgId(String classOrgId) { this.classOrgId = classOrgId; }
    public String getExperimentTitle() { return experimentTitle; }
    public void setExperimentTitle(String experimentTitle) { this.experimentTitle = experimentTitle; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getExperimentId() { return experimentId; }
    public void setExperimentId(String experimentId) { this.experimentId = experimentId; }
    public String getSimulatorId() { return simulatorId; }
    public void setSimulatorId(String simulatorId) { this.simulatorId = simulatorId; }
}
