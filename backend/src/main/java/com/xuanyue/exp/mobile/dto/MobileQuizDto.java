package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class MobileQuizDto {

    private int streakDays;
    private int bonusPoints;
    private int basePoints;
    private int questionsPerDay;
    private boolean enabled;
    private boolean ready;
    private String message;
    private boolean submittedToday;
    private TodayResult todayResult;
    private List<Question> questions;
    private List<HistoryItem> history;

    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isSubmittedToday() { return submittedToday; }
    public void setSubmittedToday(boolean submittedToday) { this.submittedToday = submittedToday; }
    public TodayResult getTodayResult() { return todayResult; }
    public void setTodayResult(TodayResult todayResult) { this.todayResult = todayResult; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }
    public int getBonusPoints() { return bonusPoints; }
    public void setBonusPoints(int bonusPoints) { this.bonusPoints = bonusPoints; }
    public int getBasePoints() { return basePoints; }
    public void setBasePoints(int basePoints) { this.basePoints = basePoints; }
    public int getQuestionsPerDay() { return questionsPerDay; }
    public void setQuestionsPerDay(int questionsPerDay) { this.questionsPerDay = questionsPerDay; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public List<HistoryItem> getHistory() { return history; }
    public void setHistory(List<HistoryItem> history) { this.history = history; }

    public static class Question {
        private int id;
        private String questionId;
        private String title;
        private String meta;
        private List<String> options;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getQuestionId() { return questionId; }
        public void setQuestionId(String questionId) { this.questionId = questionId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMeta() { return meta; }
        public void setMeta(String meta) { this.meta = meta; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
    }

    public static class HistoryItem {
        private String date;
        private String score;
        private String points;
        private boolean perfect;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getScore() { return score; }
        public void setScore(String score) { this.score = score; }
        public String getPoints() { return points; }
        public void setPoints(String points) { this.points = points; }
        public boolean isPerfect() { return perfect; }
        public void setPerfect(boolean perfect) { this.perfect = perfect; }
    }

    public static class TodayResult {
        private int score;
        private int total;
        private int points;
        private boolean perfect;
        private String resultType;
        private int streakDays;
        private String submitTime;
        private int wrongCount;

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
        public String getSubmitTime() { return submitTime; }
        public void setSubmitTime(String submitTime) { this.submitTime = submitTime; }
        public int getWrongCount() { return wrongCount; }
        public void setWrongCount(int wrongCount) { this.wrongCount = wrongCount; }
    }
}
