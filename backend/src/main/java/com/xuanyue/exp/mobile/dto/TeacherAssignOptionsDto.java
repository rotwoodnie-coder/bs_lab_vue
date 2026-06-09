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
    }
}
