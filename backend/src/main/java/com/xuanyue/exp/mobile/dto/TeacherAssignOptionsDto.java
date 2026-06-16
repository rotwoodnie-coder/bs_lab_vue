package com.xuanyue.exp.mobile.dto;

import java.util.List;

public class TeacherAssignOptionsDto {

    private List<OptionItem> classes;
    private List<OptionItem> experiments;

    public List<OptionItem> getClasses() { return classes; }
    public void setClasses(List<OptionItem> classes) { this.classes = classes; }
    public List<OptionItem> getExperiments() { return experiments; }
    public void setExperiments(List<OptionItem> experiments) { this.experiments = experiments; }

    public static class OptionItem {
        private String id;
        private String label;
        private int studentCount;
        /** experiment | simulator */
        private String sourceType;
        /** 模拟实验 id（sourceType=simulator 时用于布置任务） */
        private String simulatorId;
        private String subtitle;

        public OptionItem() {
        }

        public OptionItem(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public OptionItem(String id, String label, int studentCount) {
            this.id = id;
            this.label = label;
            this.studentCount = studentCount;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public int getStudentCount() { return studentCount; }
        public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        public String getSimulatorId() { return simulatorId; }
        public void setSimulatorId(String simulatorId) { this.simulatorId = simulatorId; }
        public String getSubtitle() { return subtitle; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    }
}
