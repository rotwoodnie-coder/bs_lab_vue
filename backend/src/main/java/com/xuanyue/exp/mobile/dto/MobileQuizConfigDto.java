package com.xuanyue.exp.mobile.dto;

public class MobileQuizConfigDto {

    private int questionsPerDay;
    private int basePoints;
    private int streakBonus;
    private boolean enabled;
    private int eligibleQuestionCount;
    private String poolWarning;

    public int getQuestionsPerDay() { return questionsPerDay; }
    public void setQuestionsPerDay(int questionsPerDay) { this.questionsPerDay = questionsPerDay; }
    public int getBasePoints() { return basePoints; }
    public void setBasePoints(int basePoints) { this.basePoints = basePoints; }
    public int getStreakBonus() { return streakBonus; }
    public void setStreakBonus(int streakBonus) { this.streakBonus = streakBonus; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getEligibleQuestionCount() { return eligibleQuestionCount; }
    public void setEligibleQuestionCount(int eligibleQuestionCount) { this.eligibleQuestionCount = eligibleQuestionCount; }
    public String getPoolWarning() { return poolWarning; }
    public void setPoolWarning(String poolWarning) { this.poolWarning = poolWarning; }
}
