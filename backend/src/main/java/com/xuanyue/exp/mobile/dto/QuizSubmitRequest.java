package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class QuizSubmitRequest {

    private List<Integer> answers;
    private boolean practice;

    public List<Integer> getAnswers() { return answers; }
    public void setAnswers(List<Integer> answers) { this.answers = answers; }
    public boolean isPractice() { return practice; }
    public void setPractice(boolean practice) { this.practice = practice; }
}
