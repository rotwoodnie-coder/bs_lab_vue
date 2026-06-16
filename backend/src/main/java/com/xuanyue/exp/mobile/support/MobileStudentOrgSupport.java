package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.data.entity.DataSchoolGrade;
import com.xuanyue.exp.data.repository.DataSchoolGradeRepository;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 从学生 user_id 解析班级、年级（g1~g6）、学校等组织信息。
 */
@Component
public class MobileStudentOrgSupport {

    static final String ORG_TYPE_GRADE = "Org_School_Grade";
    static final String ORG_TYPE_CLASS = "Org_School_Class";
    static final String ORG_TYPE_SCHOOL = "Org_School";

    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final DataSchoolGradeRepository gradeRepository;

    private volatile List<DataSchoolGrade> activeGrades = Collections.emptyList();

    public MobileStudentOrgSupport(SysUserRepository sysUserRepository,
                                   SysOrgRepository sysOrgRepository,
                                   DataSchoolGradeRepository gradeRepository) {
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.gradeRepository = gradeRepository;
    }

    public StudentOrgContext resolve(String studentUserId) {
        StudentOrgContext ctx = new StudentOrgContext();
        if (!StringUtils.hasText(studentUserId)) {
            return ctx;
        }
        Optional<SysUser> userOpt = sysUserRepository.findById(studentUserId.trim());
        if (!userOpt.isPresent()) {
            return ctx;
        }
        SysUser user = userOpt.get();
        ctx.setStudentUserId(user.getUserId());
        ctx.setRootOrgId(trim(user.getRootOrgId()));
        if (StringUtils.hasText(user.getRootOrgId())) {
            sysOrgRepository.findById(user.getRootOrgId())
                    .map(SysOrg::getOrgName)
                    .filter(StringUtils::hasText)
                    .ifPresent(ctx::setSchoolName);
        }
        if (!StringUtils.hasText(user.getUserOrgId())) {
            return ctx;
        }
        ctx.setClassOrgId(trim(user.getUserOrgId()));
        sysOrgRepository.findById(user.getUserOrgId()).ifPresent(classOrg -> {
            ctx.setClassName(trim(classOrg.getOrgName()));
            SysOrg gradeOrg = parentOrg(classOrg);
            if (gradeOrg != null) {
                ctx.setGradeOrgId(trim(gradeOrg.getOrgId()));
                ctx.setGradeName(trim(gradeOrg.getOrgName()));
                ctx.setSchoolGradeId(mapOrgNameToGradeId(gradeOrg.getOrgName()));
            }
        });
        return ctx;
    }

    private SysOrg parentOrg(SysOrg org) {
        if (org == null || !StringUtils.hasText(org.getParentOrgId())) {
            return null;
        }
        return sysOrgRepository.findById(org.getParentOrgId().trim()).orElse(null);
    }

    private String mapOrgNameToGradeId(String orgName) {
        if (!StringUtils.hasText(orgName)) {
            return null;
        }
        String trimmed = orgName.trim();
        for (DataSchoolGrade grade : loadActiveGrades()) {
            if (!StringUtils.hasText(grade.getGradeId()) || !StringUtils.hasText(grade.getGradeName())) {
                continue;
            }
            String gradeName = grade.getGradeName().trim();
            if (trimmed.equals(gradeName) || trimmed.contains(gradeName) || gradeName.contains(trimmed)) {
                return grade.getGradeId().trim();
            }
        }
        Integer level = parseChineseGradeLevel(trimmed);
        if (level != null && level >= 1 && level <= 6) {
            return "g" + level;
        }
        return null;
    }

    private List<DataSchoolGrade> loadActiveGrades() {
        List<DataSchoolGrade> cached = activeGrades;
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        synchronized (this) {
            if (activeGrades != null && !activeGrades.isEmpty()) {
                return activeGrades;
            }
            List<DataSchoolGrade> loaded = gradeRepository.findByStatusOrderBySortOrderAsc("y");
            activeGrades = loaded != null ? loaded : Collections.emptyList();
            return activeGrades;
        }
    }

    private Integer parseChineseGradeLevel(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        if (text.contains("一")) return 1;
        if (text.contains("二")) return 2;
        if (text.contains("三")) return 3;
        if (text.contains("四")) return 4;
        if (text.contains("五")) return 5;
        if (text.contains("六")) return 6;
        return null;
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }

    public static final class StudentOrgContext {
        private String studentUserId;
        private String classOrgId;
        private String gradeOrgId;
        private String schoolGradeId;
        private String rootOrgId;
        private String className;
        private String gradeName;
        private String schoolName;

        public String getStudentUserId() { return studentUserId; }
        public void setStudentUserId(String studentUserId) { this.studentUserId = studentUserId; }
        public String getClassOrgId() { return classOrgId; }
        public void setClassOrgId(String classOrgId) { this.classOrgId = classOrgId; }
        public String getGradeOrgId() { return gradeOrgId; }
        public void setGradeOrgId(String gradeOrgId) { this.gradeOrgId = gradeOrgId; }
        public String getSchoolGradeId() { return schoolGradeId; }
        public void setSchoolGradeId(String schoolGradeId) { this.schoolGradeId = schoolGradeId; }
        public String getRootOrgId() { return rootOrgId; }
        public void setRootOrgId(String rootOrgId) { this.rootOrgId = rootOrgId; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getGradeName() { return gradeName; }
        public void setGradeName(String gradeName) { this.gradeName = gradeName; }
        public String getSchoolName() { return schoolName; }
        public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    }
}
