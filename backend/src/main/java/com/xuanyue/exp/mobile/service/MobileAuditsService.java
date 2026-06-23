package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.admin.service.MobileParentRegistrationAdminService;
import com.xuanyue.exp.mobile.dto.MobileAuditSummaryDto;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class MobileAuditsService {

    private static final Set<String> TEACHER_ROLES = new HashSet<>(Arrays.asList("Teacher"));
    private static final Set<String> RESEARCHER_ROLES = new HashSet<>(Arrays.asList(
            "Researcher", "Sys_Admin", "School_Admin"));

    private static final Set<String> ADMIN_ROLES = new HashSet<>(Arrays.asList("Sys_Admin", "School_Admin"));

    private final MobileTeacherService teacherService;
    private final MobileTeacherParentBindService parentBindService;
    private final MobileResearcherExpAuditService researcherExpAuditService;
    private final MobileStudentWorkService studentWorkService;
    private final MobileParentRegistrationAdminService parentRegistrationAdminService;
    private final SysUserRepository sysUserRepository;

    public MobileAuditsService(MobileTeacherService teacherService,
                               MobileTeacherParentBindService parentBindService,
                               MobileResearcherExpAuditService researcherExpAuditService,
                               MobileStudentWorkService studentWorkService,
                               MobileParentRegistrationAdminService parentRegistrationAdminService,
                               SysUserRepository sysUserRepository) {
        this.teacherService = teacherService;
        this.parentBindService = parentBindService;
        this.researcherExpAuditService = researcherExpAuditService;
        this.studentWorkService = studentWorkService;
        this.parentRegistrationAdminService = parentRegistrationAdminService;
        this.sysUserRepository = sysUserRepository;
    }

    @Transactional(readOnly = true)
    public MobileAuditSummaryDto getSummary(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("请先登录");
        }
        SysUser user = sysUserRepository.findById(userId.trim())
                .orElseThrow(() -> new IllegalArgumentException("登录用户不存在"));
        String role = safe(user.getUserRoleId());
        MobileAuditSummaryDto dto = new MobileAuditSummaryDto();
        if (TEACHER_ROLES.contains(role)) {
            dto.setPendingWorkReviews(teacherService.countPendingReviews(userId));
            dto.setPendingParentBinds(parentBindService.countPending(userId));
        }
        if (RESEARCHER_ROLES.contains(role)) {
            dto.setPendingExpAudits(researcherExpAuditService.countPending(userId));
        }
        if (ADMIN_ROLES.contains(role)) {
            dto.setPendingWorkReviews(studentWorkService.countPendingWorkReviewsForAdmin(userId));
            dto.setPendingParentRegistrations(parentRegistrationAdminService.countPending(userId));
        }
        return dto;
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
