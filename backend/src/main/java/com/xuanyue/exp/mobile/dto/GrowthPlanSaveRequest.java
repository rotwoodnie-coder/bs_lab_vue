package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class GrowthPlanSaveRequest {

    private List<String> contentKeys;
    private String visibility;
    private String range;

    public List<String> getContentKeys() { return contentKeys; }
    public void setContentKeys(List<String> contentKeys) { this.contentKeys = contentKeys; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
}
