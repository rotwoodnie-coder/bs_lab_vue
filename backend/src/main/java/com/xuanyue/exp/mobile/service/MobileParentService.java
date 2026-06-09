package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.dto.ParentBindApplicationsDto;
import com.xuanyue.exp.mobile.dto.ParentBindRequest;
import com.xuanyue.exp.mobile.dto.ParentBindResultDto;
import com.xuanyue.exp.mobile.dto.ParentChildListItemDto;
import com.xuanyue.exp.mobile.entity.MbParentChild;
import com.xuanyue.exp.mobile.repository.MbParentChildRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.repository.MbWorkRepository;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.mobile.support.MobileParentAccessService;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MobileParentService {

    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final MbParentChildRepository parentChildRepository;
    private final MobileOrgBindService orgBindService;
    private final SysUserRepository sysUserRepository;
    private final MbTaskSubmissionRepository taskSubmissionRepository;
    private final MbWorkRepository workRepository;
    private final MobileParentAccessService parentAccessService;
    private final MobileNotificationService notificationService;

    public MobileParentService(MbParentChildRepository parentChildRepository,
                               MobileOrgBindService orgBindService,
                               SysUserRepository sysUserRepository,
                               MbTaskSubmissionRepository taskSubmissionRepository,
                               MbWorkRepository workRepository,
                               MobileParentAccessService parentAccessService,
                               MobileNotificationService notificationService) {
        this.parentChildRepository = parentChildRepository;
        this.orgBindService = orgBindService;
        this.sysUserRepository = sysUserRepository;
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.workRepository = workRepository;
        this.parentAccessService = parentAccessService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ParentBindResultDto bindChild(String userId, ParentBindRequest request) {
        if (request == null || !StringUtils.hasText(request.getClassOrgId())) {
            throw new IllegalArgumentException("请选择班级");
        }
        if (!StringUtils.hasText(request.getChildName())) {
            throw new IllegalArgumentException("请填写孩子姓名");
        }

        String parentId = MobileUserContext.resolveParentId(userId);
        if (parentId == null || parentId.trim().isEmpty()) {
            throw new IllegalArgumentException("请先登录");
        }

        SysUser student = resolveStudentForBind(
                request.getClassOrgId().trim(),
                request.getChildName().trim(),
                request.getStudentNo(),
                request.getChildUserId());
        String classOrgId = request.getClassOrgId().trim();
        MobileOrgBindService.BindOrgNames orgNames = orgBindService.resolveBindOrgNames(classOrgId);

        Optional<MbParentChild> existing = parentChildRepository.findByParentUserIdAndChildUserId(parentId, student.getUserId());
        if (existing.isPresent()) {
            MbParentChild bind = existing.get();
            if ("approved".equalsIgnoreCase(safe(bind.getBindStatus()))) {
                throw new IllegalArgumentException("该孩子已绑定");
            }
            if ("pending".equalsIgnoreCase(safe(bind.getBindStatus()))) {
                throw new IllegalArgumentException("该孩子的绑定申请审核中，请耐心等待");
            }
            bind.setBindStatus("pending");
            bind.setClassOrgId(classOrgId);
            bind.setSchoolName(orgNames.schoolName);
            bind.setGradeName(orgNames.gradeName);
            bind.setClassName(orgNames.className);
            bind.setRelation(StringUtils.hasText(request.getRelation()) ? request.getRelation().trim() : bind.getRelation());
            clearAuditFields(bind);
            bind.setUpdateTime(new Date());
            parentChildRepository.save(bind);
            notificationService.notifyTeachersOfBindApplication(parentId, bind, student);
            return toBindResult(bind, student.getUserName(), "绑定申请已提交，等待学校审核");
        }

        MbParentChild bind = new MbParentChild();
        bind.setBindId(MobileIds.newId("bind"));
        bind.setParentUserId(parentId);
        bind.setChildUserId(student.getUserId());
        bind.setRelation(StringUtils.hasText(request.getRelation()) ? request.getRelation().trim() : "家长");
        bind.setIsDefault(parentChildRepository.countByParentUserId(parentId) == 0 ? "y" : "n");
        bind.setClassOrgId(classOrgId);
        bind.setSchoolName(orgNames.schoolName);
        bind.setGradeName(orgNames.gradeName);
        bind.setClassName(orgNames.className);
        bind.setBindStatus("pending");
        bind.setCreateTime(new Date());
        bind.setUpdateTime(new Date());
        parentChildRepository.save(bind);

        notificationService.notifyTeachersOfBindApplication(parentId, bind, student);
        return toBindResult(bind, student.getUserName(), "绑定申请已提交，等待学校审核");
    }

    @Transactional(readOnly = true)
    public List<ParentBindResultDto> listPendingBinds(String userId) {
        String parentId = MobileUserContext.resolveParentId(userId);
        if (parentId == null || parentId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<MbParentChild> binds = parentChildRepository.findByParentUserIdAndBindStatusOrderByIsDefaultDesc(parentId, "pending");
        List<ParentBindResultDto> result = new ArrayList<>();
        for (MbParentChild bind : binds) {
            result.add(toBindResult(bind, resolveChildName(bind.getChildUserId()), null));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ParentBindApplicationsDto listBindApplications(String userId) {
        String parentId = MobileUserContext.resolveParentId(userId);
        ParentBindApplicationsDto dto = new ParentBindApplicationsDto();

        SysUser parent = sysUserRepository.findById(parentId).orElse(null);
        if (parent != null) {
            dto.setAccountStatus(parent.getStatus());
            dto.setAccountStatusLabel(resolveAccountStatusLabel(parent.getStatus()));
        } else {
            dto.setAccountStatus("");
            dto.setAccountStatusLabel("");
        }

        List<MbParentChild> binds = parentChildRepository.findByParentUserIdOrderByIsDefaultDesc(parentId);
        List<ParentBindResultDto> apps = new ArrayList<>();
        for (MbParentChild bind : binds) {
            apps.add(toBindResult(bind, resolveChildName(bind.getChildUserId()), null));
        }
        dto.setApplications(apps);
        return dto;
    }

    private String resolveAccountStatusLabel(String status) {
        if ("t".equalsIgnoreCase(safe(status))) {
            return "账号审核中";
        }
        if ("n".equalsIgnoreCase(safe(status))) {
            return "账号未通过审核";
        }
        if ("y".equalsIgnoreCase(safe(status))) {
            return "账号已激活";
        }
        return "账号状态未知";
    }

    private void clearAuditFields(MbParentChild bind) {
        bind.setConfirmUserId(null);
        bind.setConfirmTime(null);
        bind.setRejectReason(null);
    }

    private ParentBindResultDto toBindResult(MbParentChild bind, String childName, String message) {
        ParentBindResultDto dto = new ParentBindResultDto();
        dto.setBindId(bind.getBindId());
        dto.setChildUserId(bind.getChildUserId());
        dto.setChildName(childName);
        dto.setBindStatus(bind.getBindStatus());
        dto.setSchoolName(bind.getSchoolName());
        dto.setGradeName(bind.getGradeName());
        dto.setClassName(bind.getClassName());
        dto.setRelation(bind.getRelation());
        dto.setRejectReason(bind.getRejectReason());
        dto.setSubmitTime(formatTime(bind.getCreateTime()));
        if (StringUtils.hasText(message)) {
            dto.setMessage(message);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ParentChildListItemDto> listChildren(String userId) {
        parentAccessService.requireParentFeatureAccess(userId);
        String parentId = MobileUserContext.resolveParentId(userId);
        List<MbParentChild> binds = parentChildRepository.findByParentUserIdOrderByIsDefaultDesc(parentId);
        List<ParentChildListItemDto> result = new ArrayList<>();
        for (MbParentChild bind : binds) {
            if (!"approved".equalsIgnoreCase(safe(bind.getBindStatus()))) {
                continue;
            }
            ParentChildListItemDto item = new ParentChildListItemDto();
            item.setId(bind.getChildUserId());
            item.setName(resolveChildName(bind.getChildUserId()));
            item.setAvatar(item.getName().isEmpty() ? "孩" : item.getName().substring(0, 1));
            item.setCurrent("y".equalsIgnoreCase(safe(bind.getIsDefault())));
            item.setClassLabel(bind.getClassName());
            item.setBindStatus(bind.getBindStatus());
            long pending = taskSubmissionRepository.countByStudentUserIdAndStateNot(bind.getChildUserId(), "done");
            long completed = taskSubmissionRepository.countByStudentUserIdAndStateIn(
                    bind.getChildUserId(), java.util.Arrays.asList("done", "submitted", "reviewed"));
            item.setPending((int) pending);
            item.setCompleted((int) completed);
            item.setWorks((int) workRepository.countByStudentUserIdAndStatus(bind.getChildUserId(), "y"));
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void setDefaultChild(String userId, String childUserId) {
        String parentId = MobileUserContext.resolveParentId(userId);
        parentAccessService.requireApprovedBind(parentId, childUserId);
        List<MbParentChild> binds = parentChildRepository.findByParentUserIdOrderByIsDefaultDesc(parentId);
        for (MbParentChild bind : binds) {
            bind.setIsDefault(childUserId.equals(bind.getChildUserId()) ? "y" : "n");
            parentChildRepository.save(bind);
        }
    }

    private String resolveChildName(String childUserId) {
        return sysUserRepository.findById(childUserId)
                .map(SysUser::getUserName)
                .orElse("孩子");
    }

    private String safe(String value) {
        return value != null ? value.trim() : "";
    }

    private String formatTime(java.util.Date time) {
        return time == null ? "" : TIME_FMT.format(time);
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
