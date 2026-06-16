package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.MobileTaskListItemDto;
import com.xuanyue.exp.mobile.dto.ResearcherExpAuditItemDto;
import com.xuanyue.exp.mobile.dto.TeacherTaskSummaryDto;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MobileInboxService {

    private static final Set<String> AUDIT_ROLES = new HashSet<>(Arrays.asList(
            "researcher", "sys_admin", "school_admin"));

    private final SysUserRepository sysUserRepository;
    private final MobileTaskService taskService;
    private final MobileTeacherService teacherService;
    private final MobileResearcherExpAuditService researcherExpAuditService;

    public MobileInboxService(SysUserRepository sysUserRepository,
                              MobileTaskService taskService,
                              MobileTeacherService teacherService,
                              MobileResearcherExpAuditService researcherExpAuditService) {
        this.sysUserRepository = sysUserRepository;
        this.taskService = taskService;
        this.teacherService = teacherService;
        this.researcherExpAuditService = researcherExpAuditService;
    }

    @Transactional(readOnly = true)
    public PageResult<MobileTaskListItemDto> list(String userId, String childUserId, String status, int page, int size) {
        SysUser user = requireUser(userId);
        String resolvedStatus = resolveStatus(status);
        List<MobileTaskListItemDto> items;
        String role = normalizeRole(user.getUserRoleId());
        if (isTeacherRole(role)) {
            items = listTeacherItems(userId, resolvedStatus);
        } else if (isAuditRole(role)) {
            items = listResearcherItems(userId, resolvedStatus);
        } else {
            items = taskService.listMergedStudentItems(userId, childUserId, resolvedStatus);
        }
        items.sort(Comparator.comparingLong(MobileTaskListItemDto::getSortTime).reversed());
        return paginate(items, page, size);
    }

    private List<MobileTaskListItemDto> listTeacherItems(String userId, String status) {
        if ("cancelled".equals(status)) {
            List<MobileTaskListItemDto> items = new ArrayList<>();
            for (TeacherTaskSummaryDto task : teacherService.listTeacherTasks(userId, "cancelled")) {
                items.add(toTeacherCancelledItem(task));
            }
            return items;
        }
        List<MobileTaskListItemDto> items = new ArrayList<>();
        for (TeacherTaskSummaryDto task : teacherService.listTeacherTasks(userId)) {
            boolean needsAction = task.getPendingReview() > 0
                    || task.getSubmitted() < task.getTotalStudents();
            if ("pending".equals(status) && !needsAction) {
                continue;
            }
            if ("done".equals(status) && needsAction) {
                continue;
            }
            items.add(toTeacherItem(task, needsAction));
        }
        return items;
    }

    private MobileTaskListItemDto toTeacherCancelledItem(TeacherTaskSummaryDto task) {
        MobileTaskListItemDto item = new MobileTaskListItemDto();
        item.setId(task.getTaskId());
        item.setCategory("class");
        item.setKind("teacher-class-cancelled");
        item.setTitle(task.getTitle());
        String classLabel = StringUtils.hasText(task.getClassName()) ? task.getClassName() : "班级任务";
        int submitted = task.getSubmitted();
        int total = task.getTotalStudents();
        StringBuilder desc = new StringBuilder(classLabel);
        desc.append(" · 取消前已提交 ").append(submitted).append("/").append(total);
        item.setDesc(desc.toString());
        item.setLink("/tasks/" + task.getTaskId() + "/summary");
        item.setState("cancelled");
        item.setStateLabel("已取消");
        item.setBadgeClass("badge-slate");
        item.setSortTime(task.getSortTime() > 0 ? task.getSortTime() : System.currentTimeMillis());
        return item;
    }

    private MobileTaskListItemDto toTeacherItem(TeacherTaskSummaryDto task, boolean needsAction) {
        MobileTaskListItemDto item = new MobileTaskListItemDto();
        item.setId(task.getTaskId());
        item.setCategory("class");
        item.setKind("teacher-class");
        item.setTitle(task.getTitle());
        String classLabel = StringUtils.hasText(task.getClassName()) ? task.getClassName() : "班级任务";
        int submitted = task.getSubmitted();
        int total = task.getTotalStudents();
        int pendingReview = task.getPendingReview();
        StringBuilder desc = new StringBuilder(classLabel);
        desc.append(" · 已提交 ").append(submitted).append("/").append(total);
        if (pendingReview > 0) {
            desc.append(" · ").append(pendingReview).append(" 份待批阅");
        }
        item.setDesc(desc.toString());
        item.setLink("/tasks/" + task.getTaskId() + "/summary");
        item.setState(needsAction ? "pending" : "done");
        if (pendingReview > 0) {
            item.setStateLabel("待批阅");
            item.setBadgeClass("badge-warning");
        } else if (submitted < total) {
            item.setStateLabel("待跟进");
            item.setBadgeClass("badge-info");
        } else if (needsAction) {
            item.setStateLabel("待处理");
            item.setBadgeClass("badge-warning");
        } else {
            item.setStateLabel("已完成");
            item.setBadgeClass("badge-success");
        }
        item.setSortTime(task.getSortTime() > 0 ? task.getSortTime() : System.currentTimeMillis());
        return item;
    }

    private List<MobileTaskListItemDto> listResearcherItems(String userId, String status) {
        List<MobileTaskListItemDto> items = new ArrayList<>();
        if ("pending".equals(status)) {
            PageResult<ResearcherExpAuditItemDto> page = researcherExpAuditService.listPending(userId, null, 1, 50);
            if (page.getRecords() != null) {
                for (ResearcherExpAuditItemDto row : page.getRecords()) {
                    items.add(toResearcherPendingItem(row));
                }
            }
            return items;
        }
        for (ResearcherExpAuditItemDto row : researcherExpAuditService.listProcessed(userId, 50)) {
            items.add(toResearcherDoneItem(row));
        }
        return items;
    }

    private MobileTaskListItemDto toResearcherPendingItem(ResearcherExpAuditItemDto row) {
        MobileTaskListItemDto item = new MobileTaskListItemDto();
        item.setId(row.getExpId());
        item.setCategory("audit");
        item.setKind("exp-audit");
        item.setTitle(row.getExpName());
        item.setDesc((row.getExpTypeLabel() != null ? row.getExpTypeLabel() : "实验")
                + " · " + (row.getSubmitterName() != null ? row.getSubmitterName() : "提交人未知"));
        item.setLink("/content-audits/" + row.getExpId());
        item.setState("pending");
        item.setStateLabel("待审核");
        item.setBadgeClass("badge-warning");
        item.setSortTime(parseSortTime(row.getSubmitTime()));
        return item;
    }

    private MobileTaskListItemDto toResearcherDoneItem(ResearcherExpAuditItemDto row) {
        MobileTaskListItemDto item = new MobileTaskListItemDto();
        item.setId(row.getExpId());
        item.setCategory("audit");
        item.setKind("exp-audit-done");
        item.setTitle(row.getExpName());
        item.setDesc((row.getExpTypeLabel() != null ? row.getExpTypeLabel() : "实验")
                + " · " + statusLabel(row.getStatus()));
        item.setLink("/content-audits/" + row.getExpId());
        item.setState("done");
        item.setStateLabel(statusLabel(row.getStatus()));
        item.setBadgeClass("y".equalsIgnoreCase(safe(row.getStatus())) ? "badge-success" : "badge-danger");
        item.setSortTime(parseSortTime(row.getSubmitTime()));
        return item;
    }

    private String statusLabel(String status) {
        if ("y".equalsIgnoreCase(safe(status))) {
            return "已通过";
        }
        if ("n".equalsIgnoreCase(safe(status))) {
            return "已驳回";
        }
        return "已处理";
    }

    private long parseSortTime(String time) {
        if (!StringUtils.hasText(time)) {
            return System.currentTimeMillis();
        }
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time.trim()).getTime();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private SysUser requireUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("请先登录");
        }
        return sysUserRepository.findById(userId.trim())
                .orElseThrow(() -> new IllegalArgumentException("登录用户不存在"));
    }

    private String resolveStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "pending";
        }
        String value = status.trim().toLowerCase();
        if ("pending".equals(value)) {
            return "pending";
        }
        if ("cancelled".equals(value) || "canceled".equals(value)) {
            return "cancelled";
        }
        return "done";
    }

    private <T> PageResult<T> paginate(List<T> all, int page, int size) {
        int safeSize = Math.max(size, 1);
        int from = Math.max(page - 1, 0) * safeSize;
        if (from >= all.size()) {
            return new PageResult<>(all.size(), new ArrayList<T>());
        }
        int to = Math.min(from + safeSize, all.size());
        return new PageResult<>(all.size(), all.subList(from, to));
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }

    private static String normalizeRole(String role) {
        return safe(role).toLowerCase();
    }

    private static boolean isTeacherRole(String role) {
        return "teacher".equals(normalizeRole(role));
    }

    private static boolean isAuditRole(String role) {
        return AUDIT_ROLES.contains(normalizeRole(role));
    }
}
