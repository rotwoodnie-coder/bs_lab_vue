package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class MobileGrowthDto {

    private Stats stats;
    private Plan plan;
    private List<TimelineItem> timeline;
    private Nudge nudge;
    private Access access;

    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public List<TimelineItem> getTimeline() { return timeline; }
    public void setTimeline(List<TimelineItem> timeline) { this.timeline = timeline; }
    public Nudge getNudge() { return nudge; }
    public void setNudge(Nudge nudge) { this.nudge = nudge; }
    public Access getAccess() { return access; }
    public void setAccess(Access access) { this.access = access; }

    public static class Access {
        private boolean canEditPlan;
        private boolean canViewDetail = true;
        private String restrictedReason;

        public boolean isCanEditPlan() { return canEditPlan; }
        public void setCanEditPlan(boolean canEditPlan) { this.canEditPlan = canEditPlan; }
        public boolean isCanViewDetail() { return canViewDetail; }
        public void setCanViewDetail(boolean canViewDetail) { this.canViewDetail = canViewDetail; }
        public String getRestrictedReason() { return restrictedReason; }
        public void setRestrictedReason(String restrictedReason) { this.restrictedReason = restrictedReason; }
    }

    public static class Nudge {
        private boolean quizSubmittedToday;
        private int questionsPerDay;
        private int streakDays;
        private String text;

        public boolean isQuizSubmittedToday() { return quizSubmittedToday; }
        public void setQuizSubmittedToday(boolean quizSubmittedToday) { this.quizSubmittedToday = quizSubmittedToday; }
        public int getQuestionsPerDay() { return questionsPerDay; }
        public void setQuestionsPerDay(int questionsPerDay) { this.questionsPerDay = questionsPerDay; }
        public int getStreakDays() { return streakDays; }
        public void setStreakDays(int streakDays) { this.streakDays = streakDays; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public static class Stats {
        private int experiments;
        private int works;
        private int quizDays;
        private int points;
        private int totalPoints;
        private int periodPoints;
        private int badges;

        public int getExperiments() { return experiments; }
        public void setExperiments(int experiments) { this.experiments = experiments; }
        public int getWorks() { return works; }
        public void setWorks(int works) { this.works = works; }
        public int getQuizDays() { return quizDays; }
        public void setQuizDays(int quizDays) { this.quizDays = quizDays; }
        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }
        public int getTotalPoints() { return totalPoints; }
        public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
        public int getPeriodPoints() { return periodPoints; }
        public void setPeriodPoints(int periodPoints) { this.periodPoints = periodPoints; }
        public int getBadges() { return badges; }
        public void setBadges(int badges) { this.badges = badges; }
    }

    public static class Plan {
        private String content;
        private String visibility;
        private String range;
        private List<String> contentKeys;
        private String visibilityKey;
        private String rangeKey;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getVisibility() { return visibility; }
        public void setVisibility(String visibility) { this.visibility = visibility; }
        public String getRange() { return range; }
        public void setRange(String range) { this.range = range; }
        public List<String> getContentKeys() { return contentKeys; }
        public void setContentKeys(List<String> contentKeys) { this.contentKeys = contentKeys; }
        public String getVisibilityKey() { return visibilityKey; }
        public void setVisibilityKey(String visibilityKey) { this.visibilityKey = visibilityKey; }
        public String getRangeKey() { return rangeKey; }
        public void setRangeKey(String rangeKey) { this.rangeKey = rangeKey; }
    }

    public static class TimelineItem {
        private String time;
        private String groupLabel;
        private String emoji;
        private String title;
        private String hint;
        private List<String> badges;
        private String badge;
        private String dot;
        private String badgeClass;
        private String linkType;
        private String linkId;

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getGroupLabel() { return groupLabel; }
        public void setGroupLabel(String groupLabel) { this.groupLabel = groupLabel; }
        public String getEmoji() { return emoji; }
        public void setEmoji(String emoji) { this.emoji = emoji; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getHint() { return hint; }
        public void setHint(String hint) { this.hint = hint; }
        public List<String> getBadges() { return badges; }
        public void setBadges(List<String> badges) { this.badges = badges; }
        public String getBadge() { return badge; }
        public void setBadge(String badge) { this.badge = badge; }
        public String getDot() { return dot; }
        public void setDot(String dot) { this.dot = dot; }
        public String getBadgeClass() { return badgeClass; }
        public void setBadgeClass(String badgeClass) { this.badgeClass = badgeClass; }
        public String getLinkType() { return linkType; }
        public void setLinkType(String linkType) { this.linkType = linkType; }
        public String getLinkId() { return linkId; }
        public void setLinkId(String linkId) { this.linkId = linkId; }
    }
}
