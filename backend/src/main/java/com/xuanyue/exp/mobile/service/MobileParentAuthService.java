package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.dto.FieldValidationErrorDto;
import com.xuanyue.exp.mobile.dto.LoginNameAvailabilityDto;
import com.xuanyue.exp.mobile.dto.ParentRegisterRequest;
import com.xuanyue.exp.mobile.dto.ParentRegisterResultDto;
import com.xuanyue.exp.mobile.entity.MbParentChild;
import com.xuanyue.exp.mobile.repository.MbParentChildRepository;
import com.xuanyue.exp.mobile.support.MobileFieldValidationException;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class MobileParentAuthService {

    private static final String PARENT_ROLE = "Parent";
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private final SysUserRepository sysUserRepository;
    private final MbParentChildRepository parentChildRepository;
    private final MobileOrgBindService orgBindService;
    private final MobileNotificationService notificationService;

    public MobileParentAuthService(SysUserRepository sysUserRepository,
                                   MbParentChildRepository parentChildRepository,
                                   MobileOrgBindService orgBindService,
                                   MobileNotificationService notificationService) {
        this.sysUserRepository = sysUserRepository;
        this.parentChildRepository = parentChildRepository;
        this.orgBindService = orgBindService;
        this.notificationService = notificationService;
    }

    public LoginNameAvailabilityDto checkLoginName(String loginName) {
        if (!StringUtils.hasText(loginName)) {
            return new LoginNameAvailabilityDto(false, "请输入用户名");
        }
        String trimmed = loginName.trim();
        if (trimmed.length() < 2) {
            return new LoginNameAvailabilityDto(false, "用户名至少 2 个字符");
        }
        Optional<SysUser> existing = sysUserRepository.findByLoginName(trimmed);
        if (!existing.isPresent()) {
            return new LoginNameAvailabilityDto(true, "用户名可用");
        }
        SysUser user = existing.get();
        if ("n".equalsIgnoreCase(user.getStatus())) {
            return new LoginNameAvailabilityDto(true, "该用户名曾注册未通过，可重新提交");
        }
        if ("t".equalsIgnoreCase(user.getStatus())) {
            return new LoginNameAvailabilityDto(false, "该用户名正在审核中");
        }
        return new LoginNameAvailabilityDto(false, "用户名已被占用");
    }

    @Transactional
    public ParentRegisterResultDto register(ParentRegisterRequest request) {
        validateRegisterRequest(request);

        String loginName = request.getLoginName().trim();
        LoginNameAvailabilityDto availability = checkLoginName(loginName);
        if (!availability.isAvailable()) {
            throw new MobileFieldValidationException(
                    "请修正以下项",
                    Collections.singletonList(new FieldValidationErrorDto("loginName", availability.getMessage())));
        }

        SysUser student = resolveStudentForBind(
                request.getClassOrgId().trim(),
                request.getChildName().trim(),
                request.getStudentNo(),
                request.getChildUserId());
        String rootOrgId = orgBindService.resolveRootOrgId(request.getClassOrgId().trim());
        MobileOrgBindService.BindOrgNames orgNames = orgBindService.resolveBindOrgNames(request.getClassOrgId().trim());

        SysUser parent = resolveOrCreateParent(loginName, request, rootOrgId);
        createPendingBind(parent.getUserId(), student, request, orgNames);

        ParentRegisterResultDto result = new ParentRegisterResultDto();
        result.setUserId(parent.getUserId());
        result.setMessage("注册申请已提交，账号与孩子绑定均待学校审核，通过后可使用用户名登录");
        result.setChildName(student.getUserName());
        result.setSchoolName(orgNames.schoolName);
        result.setGradeName(orgNames.gradeName);
        result.setClassName(orgNames.className);
        result.setBindStatus("pending");
        return result;
    }

    private SysUser resolveOrCreateParent(String loginName, ParentRegisterRequest request, String rootOrgId) {
        Optional<SysUser> existing = sysUserRepository.findByLoginName(loginName);
        if (existing.isPresent() && "n".equalsIgnoreCase(existing.get().getStatus())) {
            SysUser user = existing.get();
            user.setUserName(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : loginName);
            user.setUserNickName(request.getNickname().trim());
            user.setLoginPwd(PASSWORD_ENCODER.encode(request.getPassword()));
            user.setUserPhone(trimOrNull(request.getUserPhone()));
            user.setUserRoleId(PARENT_ROLE);
            user.setRootOrgId(rootOrgId);
            user.setStatus("t");
            user.setUpdateTime(new Date());
            sysUserRepository.save(user);
            return user;
        }

        SysUser user = new SysUser();
        user.setUserId(MobileIds.newId());
        user.setLoginName(loginName);
        user.setUserName(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : loginName);
        user.setUserNickName(request.getNickname().trim());
        user.setLoginPwd(PASSWORD_ENCODER.encode(request.getPassword()));
        user.setUserPhone(trimOrNull(request.getUserPhone()));
        user.setUserRoleId(PARENT_ROLE);
        user.setRootOrgId(rootOrgId);
        user.setStatus("t");
        user.setCreateTime(new Date());
        sysUserRepository.save(user);
        return user;
    }

    private void createPendingBind(String parentUserId, SysUser student, ParentRegisterRequest request,
                                   MobileOrgBindService.BindOrgNames orgNames) {
        String classOrgId = request.getClassOrgId().trim();
        Optional<MbParentChild> existing = parentChildRepository.findByParentUserIdAndChildUserId(
                parentUserId, student.getUserId());
        MbParentChild bind;
        if (existing.isPresent()) {
            bind = existing.get();
            if ("approved".equals(bind.getBindStatus())) {
                throw new IllegalArgumentException("该孩子已绑定");
            }
            bind.setBindStatus("pending");
            bind.setClassOrgId(classOrgId);
            bind.setSchoolName(orgNames.schoolName);
            bind.setGradeName(orgNames.gradeName);
            bind.setClassName(orgNames.className);
            bind.setRelation(StringUtils.hasText(request.getRelation()) ? request.getRelation().trim() : "家长");
            bind.setConfirmUserId(null);
            bind.setConfirmTime(null);
            bind.setRejectReason(null);
            bind.setUpdateTime(new Date());
            parentChildRepository.save(bind);
            notificationService.notifyTeachersOfBindApplication(parentUserId, bind, student);
            return;
        }

        bind = new MbParentChild();
        bind.setBindId(MobileIds.newId("bind"));
        bind.setParentUserId(parentUserId);
        bind.setChildUserId(student.getUserId());
        bind.setRelation(StringUtils.hasText(request.getRelation()) ? request.getRelation().trim() : "家长");
        bind.setIsDefault(parentChildRepository.countByParentUserId(parentUserId) == 0 ? "y" : "n");
        bind.setClassOrgId(classOrgId);
        bind.setSchoolName(orgNames.schoolName);
        bind.setGradeName(orgNames.gradeName);
        bind.setClassName(orgNames.className);
        bind.setBindStatus("pending");
        bind.setCreateTime(new Date());
        bind.setUpdateTime(new Date());
        parentChildRepository.save(bind);
        notificationService.notifyTeachersOfBindApplication(parentUserId, bind, student);
    }

    private void validateRegisterRequest(ParentRegisterRequest request) {
        List<FieldValidationErrorDto> errors = collectRegisterErrors(request);
        if (!errors.isEmpty()) {
            throw new MobileFieldValidationException("请修正以下项", errors);
        }
    }

    private List<FieldValidationErrorDto> collectRegisterErrors(ParentRegisterRequest request) {
        List<FieldValidationErrorDto> errors = new ArrayList<>();
        if (request == null) {
            errors.add(new FieldValidationErrorDto("_form", "请求不能为空"));
            return errors;
        }
        if (!StringUtils.hasText(request.getLoginName())) {
            errors.add(new FieldValidationErrorDto("loginName", "请输入登录用户名"));
        } else if (!request.getLoginName().trim().matches("^1\\d{10}$")) {
            errors.add(new FieldValidationErrorDto("loginName", "手机号格式不正确"));
        }
        if (!StringUtils.hasText(request.getPassword())) {
            errors.add(new FieldValidationErrorDto("password", "请设置密码"));
        } else if (request.getPassword().length() < 8) {
            errors.add(new FieldValidationErrorDto("password", "密码不少于 8 位"));
        } else if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            errors.add(new FieldValidationErrorDto("password", "密码需同时包含字母和数字"));
        }
        if (!StringUtils.hasText(request.getNickname())) {
            errors.add(new FieldValidationErrorDto("nickname", "请输入家长姓名"));
        }
        if (!StringUtils.hasText(request.getRelation())) {
            errors.add(new FieldValidationErrorDto("relation", "请选择与孩子的关系"));
        }
        if (!StringUtils.hasText(request.getClassOrgId())) {
            errors.add(new FieldValidationErrorDto("classOrgId", "请选择班级"));
        }
        if (!StringUtils.hasText(request.getChildName())) {
            errors.add(new FieldValidationErrorDto("childName", "请输入孩子姓名"));
        }
        if (StringUtils.hasText(request.getUserPhone()) && !request.getUserPhone().trim().matches("^1\\d{10}$")) {
            errors.add(new FieldValidationErrorDto("userPhone", "手机号格式不正确"));
        }
        return errors;
    }

    private String trimOrNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private SysUser resolveStudentForBind(String classOrgId, String childName, String studentNo, String childUserId) {
        if (StringUtils.hasText(childUserId)) {
            SysUser student = orgBindService.requireStudentInClass(childUserId.trim(), classOrgId);
            if (!StringUtils.hasText(childName) || !student.getUserName().contains(childName.trim())) {
                throw new IllegalArgumentException("学生信息与姓名不匹配");
            }
            return student;
        }
        return orgBindService.resolveStudent(classOrgId, childName, studentNo);
    }
}
