package com.xuanyue.exp.mobile.dto;

public class LoginNameAvailabilityDto {

    private boolean available;
    private String message;

    public LoginNameAvailabilityDto() {
    }

    public LoginNameAvailabilityDto(boolean available, String message) {
        this.available = available;
        this.message = message;
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
