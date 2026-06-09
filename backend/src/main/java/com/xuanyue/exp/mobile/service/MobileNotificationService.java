package com.xuanyue.exp.mobile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanyue.exp.mobile.dto.MobileUserPreferencesDto;
import com.xuanyue.exp.mobile.entity.MbParentChild;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.repository.MbParentChildRepository;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
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

@Service
public class MobileNotificationService {

    public static final String MSG_TYPE_TASK = "task";
    public static final String MSG_TYPE_SYSTEM = "system";
    public static final String MSG_TYPE_BIND = "bind";

    private static final String STUDENT_ROLE = "Student";
    private static final String TEACHER_ROLE = "Teacher";
    private static final SimpleDateFormat DEADLINE_FMT = new SimpleDateFormat("M月d日");

    private final SysMsgRepository sysMsgRepository;
    private final SysUserRepository sysUserRepository;
    private final MbParentChildRepository parentChildRepository;
    private final MbTaskRepository taskRepository;
    private final SysOrgRepository sysOrgRepository;
    private final MobileSettingsService settingsService;
    private final ObjectMapper objectMapper;

    public MobileNotificationService(SysMsgRepository sysMsgRepository,
                                     SysUserRepository sysUserRepository,
                                     MbParentChildRepository parentChildRepository,
                                     MbTaskRepository taskRepository,
                                     SysOrgRepository sysOrgRepository,
                                     MobileSettingsService settingsService,
                                     ObjectMapper objectMapper) {
        this.sysMsgRepository = sysMsgRepository;
        this.sysUserRepository = sysUserRepository;
        this.parentChildRepository = parentChildRepository;
        this.taskRepository = taskRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.settingsService = settingsService;
        this.objectMapper = objectMapper;
    }

    /**
     * 教师一键提醒：向未提交学生（及开启协助提醒的家长）发送站内消息。
     *
     * @return 实际通知到的学生人数
     */
    @Transactional
    public int sendTaskReminders(String senderUserId, MbTask task, List<String> studentUserIds) {
        if (task == null || studentUserIds == null || studentUserIds.isEmpty()) {
            return 0;
        }
        String teacherName = resolveSenderName(senderUserId);
        String taskTitle = safe(task.getTitle());
        String deadlineLabel = formatDeadline(task.getDeadline());
        int count = 0;
        for (String studentId : studentUserIds) {
            if (!StringUtils.hasText(studentId)) {
                continue;
            }
            if (wantsTaskNotify(studentId)) {
                String studentName = resolveUserName(studentId);
                String title = teacherName + "提醒你提交作业";
                String preview = "「" + taskTitle + "」尚未提交，请尽快完成";
                String body = buildReminderBody(studentName, taskTitle, deadlineLabel, teacherName);
                saveMessage(studentId, senderUserId, task.getTaskId(), title, preview, body);
                count++;
            }
            notifyParentsForChild(studentId, senderUserId, task, teacherName, deadlineLabel, true);
        }
        return count;
    }

    @Transactional
    public void sendBindApproved(String senderUserId, String parentUserId, String childName, String classLabel) {
        if (!StringUtils.hasText(parentUserId)) {
            return;
        }
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
        if (!StringUtils.hasText(parentUserId)) {
            return;
        }
        String teacherName = resolveSenderName(senderUserId);
        String title = "孩子绑定未通过";
        String preview = childName + " 的绑定申请被驳回：" + safe(rejectReason);
        String body = teacherName + " 驳回了您与「" + childName + "」的绑定申请。"
                + (StringUtils.hasText(classLabel) ? "\n班级：" + classLabel : "")
                + "\n驳回原因：" + safe(rejectReason)
                + "\n\n您可以核对信息后重新提交绑定申请。";
        saveMessage(parentUserId, senderUserId, null, MSG_TYPE_SYSTEM, title, preview, body);
    }

    /** 家长提交绑定申请后，通知该班级范围内的教师前往审核。 */
    @Transactional
    public void notifyTeachersOfBindApplication(String parentUserId, MbParentChild bind, SysUser child) {
        if (bind == null || !"pending".equalsIgnoreCase(safe(bind.getBindStatus()))) {
            return;
        }
        String parentName = resolveUserName(parentUserId);
        String childName = child != null ? displayName(child) : "学生";
        String classLabel = formatBindClassLabel(bind);
        String title = "新的家长绑定申请";
        String preview = parentName + " 申请绑定 " + childName
                + (StringUtils.hasText(classLabel) ? "（" + classLabel + "）" : "");
        String body = preview + "\n\n请前往「我的 → 绑定审核」处理该申请。";

        Set<String> notified = new HashSet<>();
        for (SysUser user : sysUserRepository.findAll()) {
            if (!isTeacher(user) || !"y".equalsIgnoreCase(safe(user.getStatus()))) {
                continue;
            }
            List<MbTask> tasks = taskRepository.findByTeacherUserIdAndStatusOrderByCreateTimeDesc(user.getUserId(), "y");
            Set<String> scope = MobileTeacherClassScope.resolveClassOrgIds(user, tasks, sysOrgRepository);
            if (!MobileTeacherClassScope.isBindInScope(bind, scope, sysUserRepository::findById)) {
                continue;
            }
            if (notified.add(user.getUserId())) {
                saveBindAuditMessage(user.getUserId(), parentUserId, bind.getBindId(), title, preview, body);
            }
        }
    }

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
        if (StringUtils.hasText(bindId)) {
            msg.setLinkId(bindId);
        }
        sysMsgRepository.save(msg);
    }

    private String formatBindClassLabel(MbParentChild bind) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(bind.getSchoolName())) {
            parts.add(bind.getSchoolName());
        }
        if (StringUtils.hasText(bind.getGradeName())) {
            parts.add(bind.getGradeName());
        }
        if (StringUtils.hasText(bind.getClassName())) {
            parts.add(bind.getClassName());
        }
        return String.join(" · ", parts);
    }

    private boolean isTeacher(SysUser user) {
        if (user == null || !StringUtils.hasText(user.getUserRoleId())) {
            return false;
        }
        return TEACHER_ROLE.equalsIgnoreCase(user.getUserRoleId());
    }

    /**
     * 布置作业后通知班级学生（及家长）。
     *
     * @return 实际通知到的学生人数
     */
    @Transactional
    public int sendTaskAssigned(String senderUserId, MbTask task) {
        if (task == null || !StringUtils.hasText(task.getClassOrgId())) {
            return 0;
        }
        List<String> studentIds = listStudentIdsInClass(task.getClassOrgId());
        if (studentIds.isEmpty()) {
            return 0;
        }
        String teacherName = resolveSenderName(senderUserId);
        String taskTitle = safe(task.getTitle());
        String deadlineLabel = formatDeadline(task.getDeadline());
        int count = 0;
        for (String studentId : studentIds) {
            if (wantsTaskNotify(studentId)) {
                String title = "新实验作业：" + taskTitle;
                String preview = teacherName + " 布置了新任务" + (StringUtils.hasText(deadlineLabel) ? "，截止 " + deadlineLabel : "");
                String body = buildAssignedBody(taskTitle, deadlineLabel, teacherName, task.getDescription());
                saveMessage(studentId, senderUserId, task.getTaskId(), title, preview, body);
                count++;
            }
            notifyParentsForChild(studentId, senderUserId, task, teacherName, deadlineLabel, false);
        }
        return count;
    }

    private void notifyParentsForChild(String childUserId, String senderUserId, MbTask task,
                                       String teacherName, String deadlineLabel, boolean reminder) {
        List<MbParentChild> binds = parentChildRepository.findByChildUserIdOrderByIsDefaultDesc(childUserId);
        if (binds.isEmpty()) {
            return;
        }
        String childName = resolveUserName(childUserId);
        String taskTitle = safe(task.getTitle());
        for (MbParentChild bind : binds) {
            if (bind == null || !"approved".equalsIgnoreCase(safe(bind.getBindStatus()))) {
                continue;
            }
            String parentId = bind.getParentUserId();
            if (!StringUtils.hasText(parentId) || !wantsParentAssistNotify(parentId)) {
                continue;
            }
            String title;
            String preview;
            String body;
            if (reminder) {
                title = childName + " 尚未提交作业";
                preview = "「" + taskTitle + "」待提交 · " + teacherName + " 已发送提醒";
                body = childName + " 的实验作业「" + taskTitle + "」尚未提交。"
                        + (StringUtils.hasText(deadlineLabel) ? "\n截止时间：" + deadlineLabel : "")
                        + "\n请协助孩子尽快完成并上传成果。";
            } else {
                title = childName + " 有新实验作业";
                preview = teacherName + " 布置了「" + taskTitle + "」"
                        + (StringUtils.hasText(deadlineLabel) ? "，截止 " + deadlineLabel : "");
                body = childName + " 收到新实验作业「" + taskTitle + "」。"
                        + (StringUtils.hasText(deadlineLabel) ? "\n截止时间：" + deadlineLabel : "")
                        + "\n可在「任务」中查看详情并协助上传。";
            }
            saveMessage(parentId, senderUserId, task.getTaskId(), title, preview, body);
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
        if (StringUtils.hasText(linkId)) {
            msg.setLinkId(linkId);
        }
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
        if (StringUtils.hasText(linkRoute)) {
            payload.put("linkRoute", linkRoute.trim());
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return title + "\n" + body;
        }
    }

    private String buildReminderBody(String studentName, String taskTitle, String deadlineLabel, String teacherName) {
        StringBuilder sb = new StringBuilder();
        sb.append(studentName).append("，").append(teacherName).append(" 提醒你尽快提交实验作业「").append(taskTitle).append("」。");
        if (StringUtils.hasText(deadlineLabel)) {
            sb.append("\n截止时间：").append(deadlineLabel);
        }
        sb.append("\n请前往「我的任务」完成实验并上传成果。");
        return sb.toString();
    }

    private String buildAssignedBody(String taskTitle, String deadlineLabel, String teacherName, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append(teacherName).append(" 布置了新实验作业「").append(taskTitle).append("」。");
        if (StringUtils.hasText(deadlineLabel)) {
            sb.append("\n截止时间：").append(deadlineLabel);
        }
        if (StringUtils.hasText(description)) {
            sb.append("\n\n").append(description.trim());
        }
        sb.append("\n\n请前往「我的任务」查看详情并开始实验。");
        return sb.toString();
    }

    private List<String> listStudentIdsInClass(String classOrgId) {
        if (!StringUtils.hasText(classOrgId)) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        for (SysUser user : sysUserRepository.findAll()) {
            if (!STUDENT_ROLE.equals(user.getUserRoleId())) {
                continue;
            }
            if (!"y".equalsIgnoreCase(safe(user.getStatus()))) {
                continue;
            }
            if (classOrgId.equals(user.getUserOrgId())) {
                ids.add(user.getUserId());
            }
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
        if (!StringUtils.hasText(userId)) {
            return "老师";
        }
        return sysUserRepository.findById(userId)
                .map(this::displayName)
                .orElse("老师");
    }

    private String resolveUserName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "同学";
        }
        return sysUserRepository.findById(userId)
                .map(this::displayName)
                .orElse("同学");
    }

    private String displayName(SysUser user) {
        if (StringUtils.hasText(user.getUserNickName())) {
            return user.getUserNickName();
        }
        return user.getUserName() != null ? user.getUserName() : "用户";
    }

    private String formatDeadline(Date deadline) {
        if (deadline == null) {
            return "";
        }
        try {
            return DEADLINE_FMT.format(deadline);
        } catch (Exception e) {
            return "";
        }
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
