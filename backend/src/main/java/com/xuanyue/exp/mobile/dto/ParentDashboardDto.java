package com.xuanyue.exp.mobile.dto;

import java.util.List;
import java.util.Map;

/**
 * 家长首页仪表盘 DTO
 */
public class ParentDashboardDto {

    private List<ChildInfo> children;
    private TodayProgress todayProgress;
    private List<ActivityItem> recentActivities;
    /** 按孩子 id 的最近动态 */
    private Map<String, List<ActivityItem>> activitiesByChild;
    /** 按孩子 id 的今日答题状态（独立展示，不混入最近动态） */
    private Map<String, QuizTodayStatus> quizTodayByChild;

    public List<ChildInfo> getChildren() { return children; }
    public void setChildren(List<ChildInfo> children) { this.children = children; }
    public TodayProgress getTodayProgress() { return todayProgress; }
    public void setTodayProgress(TodayProgress todayProgress) { this.todayProgress = todayProgress; }
    public List<ActivityItem> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<ActivityItem> recentActivities) { this.recentActivities = recentActivities; }
    public Map<String, List<ActivityItem>> getActivitiesByChild() { return activitiesByChild; }
    public void setActivitiesByChild(Map<String, List<ActivityItem>> activitiesByChild) { this.activitiesByChild = activitiesByChild; }
    public Map<String, QuizTodayStatus> getQuizTodayByChild() { return quizTodayByChild; }
    public void setQuizTodayByChild(Map<String, QuizTodayStatus> quizTodayByChild) { this.quizTodayByChild = quizTodayByChild; }

    public static class ChildInfo {
        private String id;
        private String name;
        private String avatar;
        private boolean current;
        private int pending;
        private int completed;
        /** pending / approved / rejected */
        private String bindStatus;
        private String classLabel;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public boolean isCurrent() { return current; }
        public void setCurrent(boolean current) { this.current = current; }
        public int getPending() { return pending; }
        public void setPending(int pending) { this.pending = pending; }
        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }
        public String getBindStatus() { return bindStatus; }
        public void setBindStatus(String bindStatus) { this.bindStatus = bindStatus; }
        public String getClassLabel() { return classLabel; }
        public void setClassLabel(String classLabel) { this.classLabel = classLabel; }
    }

    public static class TodayProgress {
        private int pending;
        private int completed;
        private int completionRate;

        public int getPending() { return pending; }
        public void setPending(int pending) { this.pending = pending; }
        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }
        public int getCompletionRate() { return completionRate; }
        public void setCompletionRate(int completionRate) { this.completionRate = completionRate; }
    }

    public static class ActivityItem {
        private String type;
        private String content;
        private String time;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
    }

    /** pending | done | disabled */
    public static class QuizTodayStatus {
        private String state;
        private String label;

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
}
