package com.xuanyue.exp.mobile.dto;

public class ParentRegisterRequest {

    private String loginName;
    private String password;
    private String nickname;
    private String userPhone;
    private String classOrgId;
    private String childName;
    private String childUserId;
    private String studentNo;
    private String relation;

    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public String getClassOrgId() { return classOrgId; }
    public void setClassOrgId(String classOrgId) { this.classOrgId = classOrgId; }
    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }
    public String getChildUserId() { return childUserId; }
    public void setChildUserId(String childUserId) { this.childUserId = childUserId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
}
