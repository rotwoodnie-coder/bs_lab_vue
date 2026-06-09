package com.xuanyue.exp.mobile.dto;

public class TeacherRemindResultDto {

    private int notifiedCount;
    private String message;

    public TeacherRemindResultDto() {
    }

    public TeacherRemindResultDto(int notifiedCount, String message) {
        this.notifiedCount = notifiedCount;
        this.message = message;
    }

    public int getNotifiedCount() { return notifiedCount; }
    public void setNotifiedCount(int notifiedCount) { this.notifiedCount = notifiedCount; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
