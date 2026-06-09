package com.xuanyue.exp.mobile.service;



import com.xuanyue.exp.common.PageResult;

import com.xuanyue.exp.mobile.dto.TeacherParentBindAuditRequest;

import com.xuanyue.exp.mobile.dto.TeacherParentBindItemDto;

import com.xuanyue.exp.mobile.entity.MbParentChild;

import com.xuanyue.exp.mobile.entity.MbTask;

import com.xuanyue.exp.mobile.repository.MbParentChildRepository;

import com.xuanyue.exp.mobile.repository.MbTaskRepository;

import com.xuanyue.exp.mobile.support.MobileTeacherClassScope;

import com.xuanyue.exp.mobile.support.MobileUserContext;

import com.xuanyue.exp.system.entity.SysUser;

import com.xuanyue.exp.system.repository.SysOrgRepository;

import com.xuanyue.exp.system.repository.SysUserRepository;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;



import java.text.SimpleDateFormat;

import java.util.*;

import java.util.stream.Collectors;



@Service

public class MobileTeacherParentBindService {



    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");



    private final MbParentChildRepository parentChildRepository;

    private final MbTaskRepository taskRepository;

    private final SysUserRepository sysUserRepository;

    private final SysOrgRepository sysOrgRepository;

    private final MobileNotificationService notificationService;



    public MobileTeacherParentBindService(MbParentChildRepository parentChildRepository,

                                          MbTaskRepository taskRepository,

                                          SysUserRepository sysUserRepository,

                                          SysOrgRepository sysOrgRepository,

                                          MobileNotificationService notificationService) {

        this.parentChildRepository = parentChildRepository;

        this.taskRepository = taskRepository;

        this.sysUserRepository = sysUserRepository;

        this.sysOrgRepository = sysOrgRepository;

        this.notificationService = notificationService;

    }



    @Transactional(readOnly = true)

    public int countPending(String userId) {

        return listForTeacher(userId, "pending").size();

    }



    @Transactional(readOnly = true)

    public PageResult<TeacherParentBindItemDto> list(String userId, String status, int page, int size) {

        List<TeacherParentBindItemDto> all = listForTeacher(userId, status);

        int safeSize = Math.max(size, 1);

        int from = Math.max(page - 1, 0) * safeSize;

        if (from >= all.size()) {

            return new PageResult<>(all.size(), new ArrayList<>());

        }

        int to = Math.min(from + safeSize, all.size());

        return new PageResult<>(all.size(), all.subList(from, to));

    }



    @Transactional

    public TeacherParentBindItemDto audit(String userId, String bindId, TeacherParentBindAuditRequest request) {

        if (!StringUtils.hasText(bindId)) {

            throw new IllegalArgumentException("绑定记录不存在");

        }

        if (request == null || !StringUtils.hasText(request.getAction())) {

            throw new IllegalArgumentException("请指定审核操作");

        }

        String action = request.getAction().trim().toLowerCase();

        if (!"approve".equals(action) && !"reject".equals(action)) {

            throw new IllegalArgumentException("无效的审核操作");

        }

        if ("reject".equals(action) && !StringUtils.hasText(request.getRejectReason())) {

            throw new IllegalArgumentException("请填写驳回原因");

        }



        MbParentChild bind = parentChildRepository.findById(bindId.trim())

                .orElseThrow(() -> new IllegalArgumentException("绑定记录不存在"));

        if (!"pending".equalsIgnoreCase(safe(bind.getBindStatus()))) {

            throw new IllegalArgumentException("该申请已审核，无法重复操作");

        }



        String teacherId = MobileUserContext.resolveTeacherId(userId);

        SysUser teacher = sysUserRepository.findById(teacherId).orElse(null);

        List<MbTask> teacherTasks = taskRepository.findByTeacherUserIdAndStatusOrderByCreateTimeDesc(teacherId, "y");

        Set<String> teacherClassOrgIds = MobileTeacherClassScope.resolveClassOrgIds(teacher, teacherTasks, sysOrgRepository);



        if (!MobileTeacherClassScope.isBindInScope(bind, teacherClassOrgIds, sysUserRepository::findById)) {

            throw new IllegalArgumentException("无权审核该绑定申请");

        }



        Date now = new Date();

        bind.setConfirmUserId(teacherId);

        bind.setConfirmTime(now);

        bind.setUpdateTime(now);



        SysUser parent = sysUserRepository.findById(bind.getParentUserId()).orElse(null);

        SysUser child = sysUserRepository.findById(bind.getChildUserId()).orElse(null);

        String childName = displayName(child);

        String classLabel = formatClassLabel(bind);



        if ("approve".equals(action)) {

            bind.setBindStatus("approved");

            bind.setRejectReason(null);

            parentChildRepository.save(bind);

            notificationService.sendBindApproved(teacherId, bind.getParentUserId(), childName, classLabel);

        } else {

            bind.setBindStatus("rejected");

            bind.setRejectReason(request.getRejectReason().trim());

            parentChildRepository.save(bind);

            notificationService.sendBindRejected(teacherId, bind.getParentUserId(), childName, classLabel,

                    bind.getRejectReason());

        }



        return toItemDto(bind, parent, child);

    }



    private List<TeacherParentBindItemDto> listForTeacher(String userId, String status) {
        String teacherId = MobileUserContext.resolveTeacherId(userId);
        SysUser teacher = sysUserRepository.findById(teacherId).orElse(null);
        List<MbTask> teacherTasks = taskRepository.findByTeacherUserIdAndStatusOrderByCreateTimeDesc(teacherId, "y");
        Set<String> teacherClassOrgIds = MobileTeacherClassScope.resolveClassOrgIds(teacher, teacherTasks, sysOrgRepository);
        if (teacherClassOrgIds.isEmpty()) {
            return Collections.emptyList();
        }

        String statusFilter = StringUtils.hasText(status) ? status.trim().toLowerCase() : "pending";
        List<MbParentChild> binds = loadBindsForTeacherScope(teacherClassOrgIds, statusFilter);
        Map<String, SysUser> userById = sysUserRepository.findAll().stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u, (a, b) -> a));

        return binds.stream()
                .map(b -> toItemDto(b, userById.get(b.getParentUserId()), userById.get(b.getChildUserId())))
                .collect(Collectors.toList());
    }

    /**
     * 家长移动端申请写入 class_org_id；教师端按所管班级直接拉取对应 pending/approved/rejected 记录。
     */
    private List<MbParentChild> loadBindsForTeacherScope(Set<String> teacherClassOrgIds, String statusFilter) {
        List<String> classOrgIdList = new ArrayList<>(teacherClassOrgIds);
        List<MbParentChild> binds = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        if ("all".equals(statusFilter)) {
            for (String classOrgId : classOrgIdList) {
                appendScopedBinds(binds, seen, parentChildRepository.findByClassOrgIdInAndBindStatusIgnoreCaseOrderByCreateTimeDesc(
                        Collections.singletonList(classOrgId), "pending"));
                appendScopedBinds(binds, seen, parentChildRepository.findByClassOrgIdInAndBindStatusIgnoreCaseOrderByCreateTimeDesc(
                        Collections.singletonList(classOrgId), "approved"));
                appendScopedBinds(binds, seen, parentChildRepository.findByClassOrgIdInAndBindStatusIgnoreCaseOrderByCreateTimeDesc(
                        Collections.singletonList(classOrgId), "rejected"));
            }
            appendLegacyBindsWithoutClassOrg(binds, seen, teacherClassOrgIds, null);
        } else {
            appendScopedBinds(binds, seen, parentChildRepository
                    .findByClassOrgIdInAndBindStatusIgnoreCaseOrderByCreateTimeDesc(classOrgIdList, statusFilter));
            appendLegacyBindsWithoutClassOrg(binds, seen, teacherClassOrgIds, statusFilter);
        }

        binds.sort(Comparator.comparing(MbParentChild::getCreateTime, Comparator.nullsLast(Date::compareTo)).reversed());
        return binds;
    }

    private void appendScopedBinds(List<MbParentChild> target, Set<String> seen, List<MbParentChild> source) {
        if (source == null || source.isEmpty()) {
            return;
        }
        for (MbParentChild bind : source) {
            if (bind == null || !StringUtils.hasText(bind.getBindId()) || !seen.add(bind.getBindId())) {
                continue;
            }
            target.add(bind);
        }
    }

    private void appendLegacyBindsWithoutClassOrg(List<MbParentChild> target, Set<String> seen,
                                                  Set<String> teacherClassOrgIds, String statusFilter) {
        List<MbParentChild> legacyPool = StringUtils.hasText(statusFilter)
                ? parentChildRepository.findByBindStatusIgnoreCaseOrderByCreateTimeDesc(statusFilter)
                : parentChildRepository.findAll();
        for (MbParentChild bind : legacyPool) {
            if (bind == null || !StringUtils.hasText(bind.getBindId()) || !seen.add(bind.getBindId())) {
                continue;
            }
            if (StringUtils.hasText(bind.getClassOrgId())) {
                continue;
            }
            if (MobileTeacherClassScope.isChildInScope(bind.getChildUserId(), teacherClassOrgIds, sysUserRepository::findById)) {
                target.add(bind);
            }
        }
    }



    private TeacherParentBindItemDto toItemDto(MbParentChild bind, SysUser parent, SysUser child) {

        TeacherParentBindItemDto dto = new TeacherParentBindItemDto();

        dto.setBindId(bind.getBindId());

        dto.setBindStatus(bind.getBindStatus());

        dto.setSubmitTime(formatTime(bind.getCreateTime()));

        dto.setParentUserId(bind.getParentUserId());

        dto.setParentName(displayName(parent));

        dto.setParentPhoneMasked(maskPhone(parent != null ? parent.getUserPhone() : null));

        dto.setRelation(bind.getRelation());

        dto.setChildUserId(bind.getChildUserId());

        dto.setChildName(displayName(child));

        dto.setStudentNo(child != null ? safe(child.getLoginName()) : "");

        dto.setSchoolName(bind.getSchoolName());

        dto.setGradeName(bind.getGradeName());

        dto.setClassName(bind.getClassName());

        dto.setRejectReason(bind.getRejectReason());

        return dto;

    }



    private String formatClassLabel(MbParentChild bind) {

        List<String> parts = new ArrayList<>();

        if (StringUtils.hasText(bind.getSchoolName())) parts.add(bind.getSchoolName());

        if (StringUtils.hasText(bind.getGradeName())) parts.add(bind.getGradeName());

        if (StringUtils.hasText(bind.getClassName())) parts.add(bind.getClassName());

        return String.join(" · ", parts);

    }



    private String displayName(SysUser user) {

        if (user == null) {

            return "用户";

        }

        if (StringUtils.hasText(user.getUserNickName())) {

            return user.getUserNickName();

        }

        return StringUtils.hasText(user.getUserName()) ? user.getUserName() : "用户";

    }



    private String maskPhone(String phone) {

        if (!StringUtils.hasText(phone) || phone.length() < 7) {

            return "";

        }

        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);

    }



    private String formatTime(Date time) {

        return time == null ? "" : TIME_FMT.format(time);

    }



    private static String safe(String value) {

        return value != null ? value.trim() : "";

    }

}

