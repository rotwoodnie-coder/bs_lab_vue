package com.xuanyue.exp.mobile.dto;

/**
 * 学科筛选条目 DTO
 */
public class SubjectItem {

    private String key;
    private String label;

    public SubjectItem() {}

    public SubjectItem(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
