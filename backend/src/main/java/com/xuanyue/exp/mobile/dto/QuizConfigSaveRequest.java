package com.xuanyue.exp.mobile.dto;

public class QuizConfigSaveRequest {

    private Integer questionsPerDay;
    private Integer basePoints;
    private Integer streakBonus;
    private Boolean enabled;

    public Integer getQuestionsPerDay() { return questionsPerDay; }
    public void setQuestionsPerDay(Integer questionsPerDay) { this.questionsPerDay = questionsPerDay; }
    public Integer getBasePoints() { return basePoints; }
    public void setBasePoints(Integer basePoints) { this.basePoints = basePoints; }
    public Integer getStreakBonus() { return streakBonus; }
    public void setStreakBonus(Integer streakBonus) { this.streakBonus = streakBonus; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
