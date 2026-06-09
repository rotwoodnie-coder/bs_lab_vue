package com.xuanyue.exp.mobile.dto;

public class ParentChildListItemDto {

    private String id;
    private String name;
    private String avatar;
    private boolean current;
    private int pending;
    private int completed;
    private int works;
    private String classLabel;
    private String bindStatus;

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
    public int getWorks() { return works; }
    public void setWorks(int works) { this.works = works; }
    public String getClassLabel() { return classLabel; }
    public void setClassLabel(String classLabel) { this.classLabel = classLabel; }
    public String getBindStatus() { return bindStatus; }
    public void setBindStatus(String bindStatus) { this.bindStatus = bindStatus; }
}
