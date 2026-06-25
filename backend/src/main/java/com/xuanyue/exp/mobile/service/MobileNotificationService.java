package com.xuanyue.exp.mobile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.mobile.dto.MobileUserPreferencesDto;
import com.xuanyue.exp.mobile.entity.MbParentChild;
import com.xuanyue.exp.mobile.entity.MobileExpHomework;
import com.xuanyue.exp.mobile.repository.MbParentChildRepository;
import com.xuanyue.exp.mobile.repository.MobileExpHomeworkRepository;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.mobile.support.MobileTeacherClassScope;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.entity.SysMsg;
import com.xuanyue.exp.system.repository.SysMsgRepository;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * MobileNotificationService — 重构版
 *
 * 通知方法改用 MobileExpHomework / ExpMsg 代替 MbTask / MbWork。
 */
@Service
public class MobileNotificationService {

    public static final String MSG_TYPE_TASK = "task";
    public static final String MSG_TYPE_SYSTEM = "system";
    public static final String MSG_TYPE_BIND = "bind";
    public static final String MSG_TYPE_GRADE = "grade";

    private static final String STUDENT_ROLE = "Student";
    private static final String TEACHER_ROLE = "Teacher";
    private static final SimpleDateFormat DEADLINE_FMT = new SimpleDateFormat("M月d日");

    private final SysMsgRepository sysMsgRepository;
    private final SysUserRepository sysUserRepository;
    private final MbParentChildRepository parentChildRepository;
    private final MobileExpHomeworkRepository homeworkRepository;
    private final SysOrgRepository sysOrgRepository;
    private final MobileSettingsService settingsService;
    private final ObjectMapper objectMapper;

    public MobileNotificationService(SysMsgRepository sysMsgRepository,
                                     SysUserRepository sysUserRepository,
                                     MbParentChildRepository parentChildRepository,
                                     MobileExpHomeworkRepository homeworkRepository,
                                     SysOrgRepository sysOrgRepository,
                                     MobileSettingsService settingsService,
                                     ObjectMapper objectMapper) {
        this.sysMsgRepository = sysMsgRepository;
        this.sysUserRepository = sysUserRepository;
        this.parentChildRepository = parentChildRepository;
        this.homeworkRepository = homeworkRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.settingsService = settingsService;
        this.objectMapper = objectMapper;
    }

    /* ════════════════════════════════════════
       提醒未提交学生
       ════════════════════════════════════════ */

    @Transactional
    public int sendTaskReminders(String senderUserId, MobileExpHomework hw, List<String> studentUserIds) {
        if (hw == null || studentUserIds == null || studentUserIds.isEmpty()) return 0;
        String teacherName = resolveSenderName(senderUserId);
        String taskTitle = safe(resolveExpName(hw.getTeacherExpId()));
        String deadlineLabel = formatDeadline(hw.getRequireDate());
        int count = 0;
        for (String studentId : studentUserIds) {
            if (!StringUtils.hasText(studentId)) continue;
            if (wantsTaskNotify(studentId)) {
                String studentName = resolveUserName(studentId);
                String title = teacherName + "提醒你提交作品";
                String preview = "「" + taskTitle + "」尚未提交，请尽快完成";
                String body = buildReminderBody(studentName, taskTitle, deadlineLabel, teacherName);
                saveMessage(studentId, senderUserId, hw.getHomeworkId(), title, preview, body);
                count++;
            }
            notifyParentsForChild(studentId, senderUserId, hw, teacherName, deadlineLabel, true);
        }
        return count;
    }

    /* ════════════════════════════════════════
       布置作业通知
       ════════════════════════════════════════ */

    @Transactional
    public int sendTaskAssigned(String senderUserId, MobileExpHomework hw) {
        if (hw == null || !StringUtils.hasText(hw.getClassId())) return 0;
        List<String> studentIds = listStudentIdsInClass(hw.getClassId());
        if (studentIds.isEmpty()) return 0;
        String teacherName = resolveSenderName(senderUserId);
        String taskTitle = safe(resolveExpName(hw.getTeacherExpId()));
        String deadlineLabel = formatDeadline(hw.getRequireDate());
        int count = 0;
        for (String studentId : studentIds) {
            if (wantsTaskNotify(studentId)) {
                String title = "新实验任务：" + taskTitle;
                String preview = teacherName + " 发布了新任务" + (StringUtils.hasText(deadlineLabel) ? "，截止 " + deadlineLabel : "");
                String body = buildAssignedBody(taskTitle, deadlineLabel, teacherName, "");
                saveMessage(studentId, senderUserId, hw.getHomeworkId(), title, preview, body);
                count++;
            }
            notifyParentsForChild(studentId, senderUserId, hw, teacherName, deadlineLabel, false);
        }
        return count;
    }

    /* ════════════════════════════════════════
       批阅结果通知
       ════════════════════════════════════════ */

    @Transactional
    public void sendWorkReviewedToStudent(String teacherUserId, ExpMsg msg, String rating, String comment) {
        if (msg == null || !StringUtils.hasText(msg.getCreateUserId())) return;
        String studentId = msg.getCreateUserId();
        String teacherName = resolveSenderName(teacherUserId);
        String workTitle = safe(msg.getExpName());
        boolean rejected = "fail".equalsIgnoreCase(safe(rating));
        String title = rejected ? "作品未通过评价" : "作品已评价";
        String ratingLabel = ratingLabel(rating);
        String preview = teacherName + " 评价了「" + workTitle + "」：" + ratingLabel;
        StringBuilder body = new StringBuilder();
        body.append(teacherName).append(" 已评价你的作品「").append(workTitle).append("」。");
        body.append("\n评级：").append(ratingLabel);
        if (StringUtils.hasText(comment)) body.append("\n评语：").append(comment.trim());
        if (rejected) body.append("\n\n请根据老师意见修改后重新提交。");
        else body.append("\n\n可在「我的任务」或作品墙查看详情。");
        String linkRoute = "/works/" + msg.getExpId();
        saveMessageWithRoute(studentId, teacherUserId, msg.getExpId(), MSG_TYPE_GRADE, title, preview, body.toString(), linkRoute);
    }

    /* ════════════════════════════════════════
       绑定审核通知
       ════════════════════════════════════════ */

    @Transactional
    public void sendExpAuditResult(String reviewerUserId, String creatorUserId, String expName,
                                   String status, String confirmComments) {
        if (!StringUtils.hasText(creatorUserId) || !"n".equals(safe(status))) return;
        String reviewerName = resolveSenderName(reviewerUserId);
        String title = "实验审核未通过";
        String expLabel = StringUtils.hasText(expName) ? expName : "实验";
        String reason = StringUtils.hasText(confirmComments) ? confirmComments.trim() : "未填写原因";
        String preview = "「" + expLabel + "」审核未通过：" + reason;
        String body = reviewerName + " 驳回了你提交的实验「" + expLabel + "」。"
                + "\n驳回原因：" + reason
                + "\n\n请修改后重新提交审核。";
        saveMessage(creatorUserId, reviewerUserId, null, MSG_TYPE_SYSTEM, title, preview, body);
    }

    @Transactional
    public void sendBindApproved(String senderUserId, String parentUserId, String childName, String classLabel) {
        if (!StringUtils.hasText(parentUserId)) return;
        String teacherName = resolveSenderName(senderUserId);
        String title = "孩子绑定已通过";
        String preview = "已成功绑定 " + childName + (StringUtils.hasText(classLabel) ? "（" + classLabel + "）" : "");
        String body = teacherName + " 已审核通过您与「" + childName + "」的绑定申请。"
                + (StringUtils.hasText(classLabel) ? "\n班级：" + classLabel : "")
                + "\n\n若账号也已激活，您现在可以使用家长端完整功能。";
        saveMessage(parentUserId, senderUserId, null, MSG_TYPE_SYSTEM, title, preview, body);
    }

    @Transactional
    public void sendBindRejected(String senderUserId, String parentUserId, String childName, String classLabel,
                                 String rejectReason) {
        if (!StringUtils.hasText(parentUserId)) return;
        String teacherName = resolveSenderName(senderUserId);
        String title = "孩子绑定未通过";
        String preview = childName + " 的绑定申请被驳回：" + safe(rejectReason);
        String body = teacherName + " 驳回了您与「" + childName + "」的绑定申请。"
                + (StringUtils.hasText(classLabel) ? "\n班级：" + classLabel : "")
                + "\n驳回原因：" + safe(rejectReason)
                + "\n\n您可以核对信息后重新提交绑定申请。";
        saveMessage(parentUserId, senderUserId, null, MSG_TYPE_SYSTEM, title, preview, body);
    }

    @Transactional
    public void notifyTeachersOfBindApplication(String parentUserId, MbParentChild bind, SysUser child) {
        if (bind == null || !"pending".equalsIgnoreCase(safe(bind.getBindStatus()))) return;
        String parentName = resolveUserName(parentUserId);
        String childName = child != null ? displayName(child) : "学生";
        String classLabel = formatBindClassLabel(bind);
        String title = "新的家长绑定申请";
        String preview = parentName + " 申请绑定 " + childName
                + (StringUtils.hasText(classLabel) ? "（" + classLabel + "）" : "");
        String body = preview + "\n\n请前往「我的 → 绑定审核」处理该申请。";

        Set<String> notified = new HashSet<>();
        for (SysUser user : sysUserRepository.findAll()) {
            if (!isTeacher(user) || !"y".equalsIgnoreCase(safe(user.getStatus()))) continue;
            List<MobileExpHomework> teacherHws = homeworkRepository
                    .findByTearcherUserIdAndCreateTimeIsNotNullOrderByCreateTimeDesc(user.getUserId());
            Set<String> scope = new HashSet<>();
            for (MobileExpHomework hw : teacherHws) {
                if (StringUtils.hasText(hw.getClassId())) scope.add(hw.getClassId());
            }
            if (!MobileTeacherClassScope.isBindInScope(bind, scope, sysUserRepository::findById)) continue;
            if (notified.add(user.getUserId())) {
                saveBindAuditMessage(user.getUserId(), parentUserId, bind.getBindId(), title, preview, body);
            }
        }
    }

    /* ════════════════════════════════════════
       内部方法
       ════════════════════════════════════════ */

    private void saveBindAuditMessage(String receiverUserId, String senderUserId, String bindId,
                                      String title, String preview, String body) {
        SysMsg msg = new SysMsg();
        msg.setMsgId(MobileIds.newId());
        msg.setReceiverUserId(receiverUserId);
        msg.setSenderUserId(senderUserId);
        msg.setMsgTypeId(MSG_TYPE_BIND);
        msg.setMsgContent(buildContentJson(title, preview, body, "/parent-binds"));
        msg.setReadTag("0");
        msg.setSendTime(new Date());
        if (StringUtils.hasText(bindId)) msg.setLinkId(bindId);
        sysMsgRepository.save(msg);
    }

    private String formatBindClassLabel(MbParentChild bind) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(bind.getSchoolName())) parts.add(bind.getSchoolName());
        if (StringUtils.hasText(bind.getGradeName())) parts.add(bind.getGradeName());
        if (StringUtils.hasText(bind.getClassName())) parts.add(bind.getClassName());
        return String.join(" · ", parts);
    }

    private boolean isTeacher(SysUser user) {
        return user != null && StringUtils.hasText(user.getUserRoleId())
                && TEACHER_ROLE.equalsIgnoreCase(user.getUserRoleId());
    }

    // 保留 MbTask 版本的重载方法，供 MobileTaskService.createTask() 使用
    // 但 MobileTaskService 不再调用，暂保留兼容签名
    @Deprecated
    public int sendTaskReminders(String senderUserId, Object oldTask, List<String> studentUserIds) {
        return 0;
    }

    @Deprecated
    public int sendTaskAssigned(String senderUserId, Object oldTask) {
        return 0;
    }

    private void notifyParentsForChild(String childUserId, String senderUserId, MobileExpHomework hw,
                                       String teacherName, String deadlineLabel, boolean reminder) {
        List<MbParentChild> binds = parentChildRepository.findByChildUserIdOrderByIsDefaultDesc(childUserId);
        if (binds.isEmpty()) return;
        String childName = resolveUserName(childUserId);
        String taskTitle = safe(resolveExpName(hw.getTeacherExpId()));
        for (MbParentChild bind : binds) {
            if (bind == null || !"approved".equalsIgnoreCase(safe(bind.getBindStatus()))) continue;
            String parentId = bind.getParentUserId();
            if (!StringUtils.hasText(parentId) || !wantsParentAssistNotify(parentId)) continue;
            String title;
            String preview;
            String body;
            if (reminder) {
                title = childName + " 尚未提交作品";
                preview = "「" + taskTitle + "」待提交 · " + teacherName + " 已发送提醒";
                body = childName + " 的实验任务「" + taskTitle + "」尚未提交。"
                        + (StringUtils.hasText(deadlineLabel) ? "\n截止时间：" + deadlineLabel : "")
                        + "\n请协助孩子尽快完成并上传成果。";
            } else {
                title = childName + " 有新实验任务";
                preview = teacherName + " 发布了「" + taskTitle + "」"
                        + (StringUtils.hasText(deadlineLabel) ? "，截止 " + deadlineLabel : "");
                body = childName + " 收到新实验任务「" + taskTitle + "」。"
                        + (StringUtils.hasText(deadlineLabel) ? "\n截止时间：" + deadlineLabel : "")
                        + "\n可在「任务」中查看详情并协助上传。";
            }
            saveMessage(parentId, senderUserId, hw.getHomeworkId(), title, preview, body);
        }
    }

    private void saveMessage(String receiverUserId, String senderUserId, String linkId,
                             String title, String preview, String body) {
        saveMessage(receiverUserId, senderUserId, linkId, MSG_TYPE_TASK, title, preview, body);
    }

    private void saveMessage(String receiverUserId, String senderUserId, String linkId, String msgType,
                             String title, String preview, String body) {
        SysMsg msg = new SysMsg();
        msg.setMsgId(MobileIds.newId());
        msg.setReceiverUserId(receiverUserId);
        msg.setSenderUserId(senderUserId);
        msg.setMsgTypeId(msgType);
        msg.setMsgContent(buildContentJson(title, preview, body));
        msg.setReadTag("0");
        msg.setSendTime(new Date());
        if (StringUtils.hasText(linkId)) msg.setLinkId(linkId);
        sysMsgRepository.save(msg);
    }

    private void saveMessageWithRoute(String receiverUserId, String senderUserId, String linkId, String msgType,
                                      String title, String preview, String body, String linkRoute) {
        SysMsg msg = new SysMsg();
        msg.setMsgId(MobileIds.newId());
        msg.setReceiverUserId(receiverUserId);
        msg.setSenderUserId(senderUserId);
        msg.setMsgTypeId(msgType);
        msg.setMsgContent(buildContentJson(title, preview, body, linkRoute));
        msg.setReadTag("0");
        msg.setSendTime(new Date());
        if (StringUtils.hasText(linkId)) msg.setLinkId(linkId);
        sysMsgRepository.save(msg);
    }

    private String buildContentJson(String title, String preview, String body) {
        return buildContentJson(title, preview, body, null);
    }

    private String buildContentJson(String title, String preview, String body, String linkRoute) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("preview", preview);
        payload.put("body", body);
        if (StringUtils.hasText(linkRoute)) payload.put("linkRoute", linkRoute.trim());
        try { return objectMapper.writeValueAsString(payload); }
        catch (Exception e) { return title + "\n" + body; }
    }

    private String buildReminderBody(String studentName, String taskTitle, String deadlineLabel, String teacherName) {
        StringBuilder sb = new StringBuilder();
        sb.append(studentName).append("，").append(teacherName).append(" 提醒你尽快提交实验任务「").append(taskTitle).append("」。");
        if (StringUtils.hasText(deadlineLabel)) sb.append("\n截止时间：").append(deadlineLabel);
        sb.append("\n请前往「我的任务」完成实验并上传成果。");
        return sb.toString();
    }

    private String buildAssignedBody(String taskTitle, String deadlineLabel, String teacherName, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append(teacherName).append(" 发布了新实验任务「").append(taskTitle).append("」。");
        if (StringUtils.hasText(deadlineLabel)) sb.append("\n截止时间：").append(deadlineLabel);
        if (StringUtils.hasText(description)) sb.append("\n\n").append(description.trim());
        sb.append("\n\n请前往「我的任务」查看详情并开始实验。");
        return sb.toString();
    }

    private List<String> listStudentIdsInClass(String classOrgId) {
        if (!StringUtils.hasText(classOrgId)) return Collections.emptyList();
        List<String> ids = new ArrayList<>();
        for (SysUser user : sysUserRepository.findAll()) {
            if (!STUDENT_ROLE.equals(user.getUserRoleId())) continue;
            if (!"y".equalsIgnoreCase(safe(user.getStatus()))) continue;
            if (classOrgId.equals(user.getUserOrgId())) ids.add(user.getUserId());
        }
        return ids;
    }

    private boolean wantsTaskNotify(String userId) {
        MobileUserPreferencesDto prefs = settingsService.getPreferences(userId);
        return prefs == null || prefs.getNotify() == null || prefs.getNotify().isTask();
    }

    private boolean wantsParentAssistNotify(String userId) {
        MobileUserPreferencesDto prefs = settingsService.getPreferences(userId);
        return prefs == null || prefs.getNotify() == null || prefs.getNotify().isParentAssist();
    }

    private String resolveSenderName(String userId) {
        if (!StringUtils.hasText(userId)) return "老师";
        return sysUserRepository.findById(userId).map(this::displayName).orElse("老师");
    }

    private String resolveUserName(String userId) {
        if (!StringUtils.hasText(userId)) return "同学";
        return sysUserRepository.findById(userId).map(this::displayName).orElse("同学");
    }

    private String resolveExpName(String expId) {
        return "实验任务";
    }

    private String displayName(SysUser user) {
        if (StringUtils.hasText(user.getUserNickName())) return user.getUserNickName();
        return user.getUserName() != null ? user.getUserName() : "用户";
    }

    private String formatDeadline(String deadline) {
        if (!StringUtils.hasText(deadline)) return "";
        return deadline.trim();
    }

    private String ratingLabel(String rating) {
        if (!StringUtils.hasText(rating)) return "已评价";
        switch (rating.trim().toLowerCase()) {
            case "excellent": return "优秀";
            case "good": return "良好";
            case "pass": return "合格";
            case "fail": return "不合格";
            default: return rating.trim();
        }
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
