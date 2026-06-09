package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class MobileQuizReviewDto {

    private String date;
    private String title;
    private String meta;
    private List<String> options;
    private int userAnswerIndex;
    private String userAnswerText;
    private int correctAnswerIndex;
    private String correctAnswerText;
    private int questionNo;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMeta() { return meta; }
    public void setMeta(String meta) { this.meta = meta; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public int getUserAnswerIndex() { return userAnswerIndex; }
    public void setUserAnswerIndex(int userAnswerIndex) { this.userAnswerIndex = userAnswerIndex; }
    public String getUserAnswerText() { return userAnswerText; }
    public void setUserAnswerText(String userAnswerText) { this.userAnswerText = userAnswerText; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) { this.correctAnswerIndex = correctAnswerIndex; }
    public String getCorrectAnswerText() { return correctAnswerText; }
    public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText; }
    public int getQuestionNo() { return questionNo; }
    public void setQuestionNo(int questionNo) { this.questionNo = questionNo; }
}
