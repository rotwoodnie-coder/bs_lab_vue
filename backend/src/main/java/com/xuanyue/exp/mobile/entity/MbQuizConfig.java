package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "mb_quiz_config")
public class MbQuizConfig {

    public static final String DEFAULT_ID = "default";

    @Id
    @Column(name = "config_id")
    private String configId;

    @Column(name = "questions_per_day")
    private Integer questionsPerDay;

    @Column(name = "base_points")
    private Integer basePoints;

    @Column(name = "streak_bonus")
    private Integer streakBonus;

    @Column(name = "enabled")
    private String enabled;

    @Column(name = "update_by")
    private String updateBy;

    @Column(name = "update_time")
    private Date updateTime;

    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }
    public Integer getQuestionsPerDay() { return questionsPerDay; }
    public void setQuestionsPerDay(Integer questionsPerDay) { this.questionsPerDay = questionsPerDay; }
    public Integer getBasePoints() { return basePoints; }
    public void setBasePoints(Integer basePoints) { this.basePoints = basePoints; }
    public Integer getStreakBonus() { return streakBonus; }
    public void setStreakBonus(Integer streakBonus) { this.streakBonus = streakBonus; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
