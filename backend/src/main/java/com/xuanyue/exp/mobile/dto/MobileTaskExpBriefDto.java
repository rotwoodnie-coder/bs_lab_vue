package com.xuanyue.exp.mobile.dto;

public class MobileTaskExpBriefDto {

    /** 对照教材，单行摘要 */
    private String curriculumLine;
    /** 实验材料名称，顿号分隔 */
    private String materialsLine;
    /** 实验步骤标题，箭头或顿号连接的一行 */
    private String stepsLine;
    /** 安全提示，单行纯文本 */
    private String safetyLine;

    public String getCurriculumLine() { return curriculumLine; }
    public void setCurriculumLine(String curriculumLine) { this.curriculumLine = curriculumLine; }
    public String getMaterialsLine() { return materialsLine; }
    public void setMaterialsLine(String materialsLine) { this.materialsLine = materialsLine; }
    public String getStepsLine() { return stepsLine; }
    public void setStepsLine(String stepsLine) { this.stepsLine = stepsLine; }
    public String getSafetyLine() { return safetyLine; }
    public void setSafetyLine(String safetyLine) { this.safetyLine = safetyLine; }

    public boolean hasAnyContent() {
        return hasText(curriculumLine) || hasText(materialsLine) || hasText(stepsLine) || hasText(safetyLine);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
