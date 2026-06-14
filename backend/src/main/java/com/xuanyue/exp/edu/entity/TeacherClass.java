package com.xuanyue.exp.edu.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "teacher_class")
public class TeacherClass {

    @Id
    @Column(name = "seq_id")
    private String seqId;

    @Column(name = "teacher_id")
    private String teacherId;

    @Column(name = "class_id")
    private String classId;

    public String getSeqId() { return seqId; }
    public void setSeqId(String seqId) { this.seqId = seqId; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
}
