package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class TeacherDashboardDto {

    private int pendingReview;
    private int assigned;
    private int submitted;
    private int students;
    private int submitRate;
    private int unsubmitted;
    private String latestTaskId;
    private String latestTaskTitle;
    private String teacherName;
    private String classLabel;

    private List<Integer> weeklyTrend;
    private int trendTotal;
    private int trendDeltaPercent;
    private int pendingParentBinds;

    public int getPendingReview() { return pendingReview; }
    public void setPendingReview(int pendingReview) { this.pendingReview = pendingReview; }
    public int getAssigned() { return assigned; }
    public void setAssigned(int assigned) { this.assigned = assigned; }
    public int getSubmitted() { return submitted; }
    public void setSubmitted(int submitted) { this.submitted = submitted; }
    public int getStudents() { return students; }
    public void setStudents(int students) { this.students = students; }
    public int getSubmitRate() { return submitRate; }
    public void setSubmitRate(int submitRate) { this.submitRate = submitRate; }
    public int getUnsubmitted() { return unsubmitted; }
    public void setUnsubmitted(int unsubmitted) { this.unsubmitted = unsubmitted; }
    public String getLatestTaskId() { return latestTaskId; }
    public void setLatestTaskId(String latestTaskId) { this.latestTaskId = latestTaskId; }
    public String getLatestTaskTitle() { return latestTaskTitle; }
    public void setLatestTaskTitle(String latestTaskTitle) { this.latestTaskTitle = latestTaskTitle; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getClassLabel() { return classLabel; }
    public void setClassLabel(String classLabel) { this.classLabel = classLabel; }
    public List<Integer> getWeeklyTrend() { return weeklyTrend; }
    public void setWeeklyTrend(List<Integer> weeklyTrend) { this.weeklyTrend = weeklyTrend; }
    public int getTrendTotal() { return trendTotal; }
    public void setTrendTotal(int trendTotal) { this.trendTotal = trendTotal; }
    public int getTrendDeltaPercent() { return trendDeltaPercent; }
    public void setTrendDeltaPercent(int trendDeltaPercent) { this.trendDeltaPercent = trendDeltaPercent; }
    public int getPendingParentBinds() { return pendingParentBinds; }
    public void setPendingParentBinds(int pendingParentBinds) { this.pendingParentBinds = pendingParentBinds; }
}
