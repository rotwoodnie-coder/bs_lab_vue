package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class UpdateWorkRequest {

    private String title;
    private String description;
    private List<CreateWorkFileItem> files;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<CreateWorkFileItem> getFiles() { return files; }
    public void setFiles(List<CreateWorkFileItem> files) { this.files = files; }
}
