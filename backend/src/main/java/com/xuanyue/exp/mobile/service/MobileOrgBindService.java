package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.dto.OrgOptionDto;
import com.xuanyue.exp.mobile.dto.StudentSearchItemDto;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MobileOrgBindService {

    static final String ORG_TYPE_SCHOOL = "Org_School";
    static final String ORG_TYPE_GRADE = "Org_School_Grade";
    static final String ORG_TYPE_CLASS = "Org_School_Class";
    static final String STUDENT_ROLE = "Student";

    private final SysOrgRepository sysOrgRepository;
    private final SysUserRepository sysUserRepository;

    public MobileOrgBindService(SysOrgRepository sysOrgRepository, SysUserRepository sysUserRepository) {
        this.sysOrgRepository = sysOrgRepository;
        this.sysUserRepository = sysUserRepository;
    }

    public List<OrgOptionDto> listSchools() {
        List<SysOrg> all = activeOrgs();
        Map<String, SysOrg> byId = indexById(all);
        Map<String, List<SysOrg>> byParent = groupByParent(all);
        List<OrgOptionDto> result = new ArrayList<OrgOptionDto>();
        for (SysOrg org : all) {
            if (!ORG_TYPE_SCHOOL.equals(org.getOrgTypeId())) {
                continue;
            }
            // 跳过「小学部/初中部」等校区节点，只展示顶层学校（如宝山实验小学）
            if (hasOrgSchoolAncestor(org, byId)) {
                continue;
            }
            if (!hasDescendantType(org.getOrgId(), ORG_TYPE_GRADE, byParent)) {
                continue;
            }
            result.add(new OrgOptionDto(org.getOrgId(), org.getOrgName()));
        }
        result.sort(Comparator.comparing(OrgOptionDto::getOrgName, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    public List<OrgOptionDto> listGrades(String schoolOrgId) {
        SysOrg school = requireOrg(schoolOrgId);
        if (!ORG_TYPE_SCHOOL.equals(school.getOrgTypeId())) {
            throw new IllegalArgumentException("请选择学校");
        }
        Map<String, List<SysOrg>> byParent = groupByParent(activeOrgs());
        List<OrgOptionDto> result = new ArrayList<OrgOptionDto>();
        collectGradesUnderSchool(schoolOrgId, result, byParent, new HashSet<String>());
        result.sort(Comparator.comparing(OrgOptionDto::getOrgName, Comparator.nullsLast(String::compareTo))
                .thenComparing(OrgOptionDto::getOrgId));
        return result;
    }

    public List<OrgOptionDto> listClasses(String gradeOrgId) {
        requireOrg(gradeOrgId);
        return listChildrenByType(gradeOrgId, ORG_TYPE_CLASS);
    }

    public List<StudentSearchItemDto> searchStudents(String classOrgId, String name, String studentNo) {
        SysOrg classOrg = requireOrg(classOrgId);
        if (!ORG_TYPE_CLASS.equals(classOrg.getOrgTypeId())) {
            throw new IllegalArgumentException("请选择班级");
        }
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("请输入孩子姓名");
        }
        String trimmedName = name.trim();
        List<StudentSearchItemDto> result = new ArrayList<StudentSearchItemDto>();
        for (SysUser user : sysUserRepository.findAll()) {
            if (!STUDENT_ROLE.equals(user.getUserRoleId())) {
                continue;
            }
            if (!classOrgId.equals(user.getUserOrgId())) {
                continue;
            }
            if (!matchesStudentName(user, trimmedName)) {
                continue;
            }
            if (StringUtils.hasText(studentNo) && !studentNo.trim().equals(user.getLoginName())) {
                continue;
            }
            StudentSearchItemDto item = new StudentSearchItemDto();
            item.setUserId(user.getUserId());
            item.setUserName(user.getUserName());
            item.setLoginName(user.getLoginName());
            item.setClassOrgId(classOrgId);
            item.setClassName(classOrg.getOrgName());
            SysOrg gradeOrg = parentOrg(classOrg);
            if (gradeOrg != null) {
                item.setGradeName(gradeOrg.getOrgName());
            }
            result.add(item);
        }
        result.sort(Comparator.comparing(StudentSearchItemDto::getUserName, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    public SysUser requireStudentInClass(String childUserId, String classOrgId) {
        SysUser student = sysUserRepository.findById(childUserId)
                .orElseThrow(() -> new IllegalArgumentException("未找到该学生"));
        if (!STUDENT_ROLE.equals(student.getUserRoleId())) {
            throw new IllegalArgumentException("该用户不是学生");
        }
        if (!classOrgId.equals(student.getUserOrgId())) {
            throw new IllegalArgumentException("学生与所选班级不匹配");
        }
        return student;
    }

    /** 按班级 + 姓名（+ 可选学号）解析唯一学生，供绑定/注册提交时使用 */
    public SysUser resolveStudent(String classOrgId, String name, String studentNo) {
        List<StudentSearchItemDto> matches = searchStudents(classOrgId, name, studentNo);
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("未找到该学生，请核对姓名与班级");
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("找到多名学生，请填写学号");
        }
        return requireStudentInClass(matches.get(0).getUserId(), classOrgId);
    }

    public String resolveRootOrgId(String orgId) {
        SysOrg org = requireOrg(orgId);
        String rootSchoolId = null;
        SysOrg current = org;
        while (current != null) {
            if (ORG_TYPE_SCHOOL.equals(current.getOrgTypeId())) {
                rootSchoolId = current.getOrgId();
            }
            if (!StringUtils.hasText(current.getParentOrgId())) {
                break;
            }
            current = sysOrgRepository.findById(current.getParentOrgId()).orElse(null);
        }
        if (rootSchoolId == null) {
            throw new IllegalArgumentException("无法解析学校组织");
        }
        return rootSchoolId;
    }

    public BindOrgNames resolveBindOrgNames(String classOrgId) {
        SysOrg classOrg = requireOrg(classOrgId);
        SysOrg gradeOrg = parentOrg(classOrg);
        SysOrg schoolOrg = findTopSchool(classOrg);
        BindOrgNames names = new BindOrgNames();
        names.schoolName = schoolOrg != null ? schoolOrg.getOrgName() : null;
        names.gradeName = gradeOrg != null ? gradeOrg.getOrgName() : null;
        names.className = classOrg.getOrgName();
        return names;
    }

    /** 沿父链取最顶层 Org_School（真实学校名，而非「小学部」等校区） */
    private SysOrg findTopSchool(SysOrg start) {
        SysOrg top = ORG_TYPE_SCHOOL.equals(start.getOrgTypeId()) ? start : null;
        SysOrg current = start;
        while (current != null) {
            if (ORG_TYPE_SCHOOL.equals(current.getOrgTypeId())) {
                top = current;
            }
            if (!StringUtils.hasText(current.getParentOrgId())) {
                break;
            }
            current = sysOrgRepository.findById(current.getParentOrgId()).orElse(null);
        }
        return top;
    }

    private SysOrg parentOrg(SysOrg org) {
        if (org == null || !StringUtils.hasText(org.getParentOrgId())) {
            return null;
        }
        return sysOrgRepository.findById(org.getParentOrgId()).orElse(null);
    }

    private boolean matchesStudentName(SysUser user, String name) {
        return (StringUtils.hasText(user.getUserName()) && user.getUserName().contains(name))
                || (StringUtils.hasText(user.getUserNickName()) && user.getUserNickName().contains(name));
    }

    private List<OrgOptionDto> listChildrenByType(String parentOrgId, String orgTypeId) {
        List<OrgOptionDto> result = new ArrayList<OrgOptionDto>();
        for (SysOrg org : activeOrgs()) {
            if (parentOrgId.equals(org.getParentOrgId()) && orgTypeId.equals(org.getOrgTypeId())) {
                result.add(new OrgOptionDto(org.getOrgId(), org.getOrgName()));
            }
        }
        result.sort(Comparator.comparing(OrgOptionDto::getOrgName, Comparator.nullsLast(String::compareTo))
                .thenComparing(OrgOptionDto::getOrgId));
        return result;
    }

    private boolean hasOrgSchoolAncestor(SysOrg org, Map<String, SysOrg> byId) {
        if (!StringUtils.hasText(org.getParentOrgId())) {
            return false;
        }
        SysOrg parent = byId.get(org.getParentOrgId());
        if (parent == null) {
            return false;
        }
        if (ORG_TYPE_SCHOOL.equals(parent.getOrgTypeId())) {
            return true;
        }
        return hasOrgSchoolAncestor(parent, byId);
    }

    private boolean hasDescendantType(String orgId, String orgTypeId, Map<String, List<SysOrg>> byParent) {
        List<SysOrg> children = byParent.get(orgId);
        if (children == null) {
            return false;
        }
        for (SysOrg child : children) {
            if (orgTypeId.equals(child.getOrgTypeId())) {
                return true;
            }
            if (hasDescendantType(child.getOrgId(), orgTypeId, byParent)) {
                return true;
            }
        }
        return false;
    }

    private void collectGradesUnderSchool(String orgId, List<OrgOptionDto> result,
            Map<String, List<SysOrg>> byParent, Set<String> seen) {
        List<SysOrg> children = byParent.get(orgId);
        if (children == null) {
            return;
        }
        for (SysOrg child : children) {
            if (ORG_TYPE_GRADE.equals(child.getOrgTypeId())) {
                if (seen.add(child.getOrgId())) {
                    result.add(new OrgOptionDto(child.getOrgId(), child.getOrgName()));
                }
            } else if (ORG_TYPE_SCHOOL.equals(child.getOrgTypeId())) {
                collectGradesUnderSchool(child.getOrgId(), result, byParent, seen);
            }
        }
    }

    private Map<String, SysOrg> indexById(List<SysOrg> all) {
        Map<String, SysOrg> map = new HashMap<String, SysOrg>();
        for (SysOrg org : all) {
            if (StringUtils.hasText(org.getOrgId())) {
                map.put(org.getOrgId(), org);
            }
        }
        return map;
    }

    private Map<String, List<SysOrg>> groupByParent(List<SysOrg> all) {
        Map<String, List<SysOrg>> map = new HashMap<String, List<SysOrg>>();
        for (SysOrg org : all) {
            String parentId = StringUtils.hasText(org.getParentOrgId()) ? org.getParentOrgId() : "";
            if (!map.containsKey(parentId)) {
                map.put(parentId, new ArrayList<SysOrg>());
            }
            map.get(parentId).add(org);
        }
        return map;
    }

    private List<SysOrg> activeOrgs() {
        List<SysOrg> active = new ArrayList<SysOrg>();
        for (SysOrg org : sysOrgRepository.findAll()) {
            if (!"y".equalsIgnoreCase(org.getStatus())) {
                continue;
            }
            active.add(org);
        }
        return active;
    }

    private SysOrg requireOrg(String orgId) {
        if (!StringUtils.hasText(orgId)) {
            throw new IllegalArgumentException("组织不能为空");
        }
        return sysOrgRepository.findById(orgId.trim())
                .orElseThrow(() -> new IllegalArgumentException("组织不存在"));
    }

    public static class BindOrgNames {
        public String schoolName;
        public String gradeName;
        public String className;
    }
}
