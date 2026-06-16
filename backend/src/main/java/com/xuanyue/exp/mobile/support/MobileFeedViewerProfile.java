package com.xuanyue.exp.mobile.support;

import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Set;

/** 首页 Feed 个性化排序：当前登录用户（或家长所选孩子）的视角。 */
public final class MobileFeedViewerProfile {

    private final String role;
    private final String schoolGradeId;
    private final String classOrgId;
    private final String rootOrgId;
    private final Set<String> teacherClassOrgIds;

    public MobileFeedViewerProfile(String role,
                                   String schoolGradeId,
                                   String classOrgId,
                                   String rootOrgId,
                                   Set<String> teacherClassOrgIds) {
        this.role = role != null ? role.trim().toLowerCase() : "";
        this.schoolGradeId = schoolGradeId;
        this.classOrgId = classOrgId;
        this.rootOrgId = rootOrgId;
        this.teacherClassOrgIds = teacherClassOrgIds != null ? teacherClassOrgIds : Collections.emptySet();
    }

    public static MobileFeedViewerProfile guest() {
        return new MobileFeedViewerProfile("", null, null, null, Collections.emptySet());
    }

    public String getRole() { return role; }
    public String getSchoolGradeId() { return schoolGradeId; }
    public String getClassOrgId() { return classOrgId; }
    public String getRootOrgId() { return rootOrgId; }
    public Set<String> getTeacherClassOrgIds() { return teacherClassOrgIds; }

    public boolean isTeacher() { return "teacher".equals(role); }
    public boolean isParent() { return "parent".equals(role); }
    public boolean isStudent() { return "student".equals(role); }
    public boolean isLoggedIn() { return StringUtils.hasText(role); }
}
