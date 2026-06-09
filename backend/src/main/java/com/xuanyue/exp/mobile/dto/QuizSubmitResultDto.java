package com.xuanyue.exp.mobile.dto;

public class QuizSubmitResultDto {

    private int score;
    private int total;
    private int points;
    private boolean perfect;
    private String resultType;
    private int streakDays;

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public boolean isPerfect() { return perfect; }
    public void setPerfect(boolean perfect) { this.perfect = perfect; }
    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }
    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }
}
