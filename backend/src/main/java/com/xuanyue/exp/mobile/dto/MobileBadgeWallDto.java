package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class MobileBadgeWallDto {

    private int earned;
    private int total;
    private String progressHint;
    private double progressPercent;
    private List<BadgeItem> items;

    public int getEarned() { return earned; }
    public void setEarned(int earned) { this.earned = earned; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public String getProgressHint() { return progressHint; }
    public void setProgressHint(String progressHint) { this.progressHint = progressHint; }
    public double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(double progressPercent) { this.progressPercent = progressPercent; }
    public List<BadgeItem> getItems() { return items; }
    public void setItems(List<BadgeItem> items) { this.items = items; }

    public static class BadgeItem {
        private String id;
        private String icon;
        private String title;
        private String desc;
        private boolean earned;
        private String progress;
        private String action;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public boolean isEarned() { return earned; }
        public void setEarned(boolean earned) { this.earned = earned; }
        public String getProgress() { return progress; }
        public void setProgress(String progress) { this.progress = progress; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }
}
