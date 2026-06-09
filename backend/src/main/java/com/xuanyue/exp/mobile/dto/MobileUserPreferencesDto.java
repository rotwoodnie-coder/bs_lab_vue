package com.xuanyue.exp.mobile.dto;

public class MobileUserPreferencesDto {

    private NotifyPrefs notify = new NotifyPrefs();
    private PrivacyPrefs privacy = new PrivacyPrefs();
    private ParentalPrefs parental = new ParentalPrefs();
    private TeachingPrefs teaching = new TeachingPrefs();

    public NotifyPrefs getNotify() { return notify; }
    public void setNotify(NotifyPrefs notify) { this.notify = notify; }
    public PrivacyPrefs getPrivacy() { return privacy; }
    public void setPrivacy(PrivacyPrefs privacy) { this.privacy = privacy; }
    public ParentalPrefs getParental() { return parental; }
    public void setParental(ParentalPrefs parental) { this.parental = parental; }
    public TeachingPrefs getTeaching() { return teaching; }
    public void setTeaching(TeachingPrefs teaching) { this.teaching = teaching; }

    public static class NotifyPrefs {
        private boolean task = true;
        private boolean exp = true;
        private boolean badge = true;
        private boolean sys = true;
        private boolean push = false;
        private boolean parentAssist = true;
        private boolean childProgress = true;
        private boolean classSubmit = true;

        public boolean isTask() { return task; }
        public void setTask(boolean task) { this.task = task; }
        public boolean isExp() { return exp; }
        public void setExp(boolean exp) { this.exp = exp; }
        public boolean isBadge() { return badge; }
        public void setBadge(boolean badge) { this.badge = badge; }
        public boolean isSys() { return sys; }
        public void setSys(boolean sys) { this.sys = sys; }
        public boolean isPush() { return push; }
        public void setPush(boolean push) { this.push = push; }
        public boolean isParentAssist() { return parentAssist; }
        public void setParentAssist(boolean parentAssist) { this.parentAssist = parentAssist; }
        public boolean isChildProgress() { return childProgress; }
        public void setChildProgress(boolean childProgress) { this.childProgress = childProgress; }
        public boolean isClassSubmit() { return classSubmit; }
        public void setClassSubmit(boolean classSubmit) { this.classSubmit = classSubmit; }
    }

    public static class PrivacyPrefs {
        private boolean works = true;
        private boolean childWorks = true;
        private boolean mention = true;
        private boolean ai = false;

        public boolean isWorks() { return works; }
        public void setWorks(boolean works) { this.works = works; }
        public boolean isChildWorks() { return childWorks; }
        public void setChildWorks(boolean childWorks) { this.childWorks = childWorks; }
        public boolean isMention() { return mention; }
        public void setMention(boolean mention) { this.mention = mention; }
        public boolean isAi() { return ai; }
        public void setAi(boolean ai) { this.ai = ai; }
    }

    public static class ParentalPrefs {
        private boolean upload = true;
        private boolean msg = true;

        public boolean isUpload() { return upload; }
        public void setUpload(boolean upload) { this.upload = upload; }
        public boolean isMsg() { return msg; }
        public void setMsg(boolean msg) { this.msg = msg; }
    }

    public static class TeachingPrefs {
        private boolean alert = true;
        private boolean ai = true;

        public boolean isAlert() { return alert; }
        public void setAlert(boolean alert) { this.alert = alert; }
        public boolean isAi() { return ai; }
        public void setAi(boolean ai) { this.ai = ai; }
    }
}
