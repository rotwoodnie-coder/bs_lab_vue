package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.mobile.dto.MobileTaskDto;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.entity.MbWork;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.support.MobileEntityMapper;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.mobile.support.MobileJsonUtils;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MobileRemixService {

    private static final List<String> REMIX_REQUIREMENTS = Arrays.asList(
            "拍摄与参考视频同主题的实验过程",
            "至少 1 张照片或 1 段短视频",
            "上传后归入作品墙「拍同款」"
    );
    private static final List<String> REMIX_STEPS = Arrays.asList(
            "回看参考视频，了解步骤",
            "动手完成同款实验并拍摄",
            "上传作品完成拍同款"
    );

    private final ExpMsgRepository expMsgRepository;
    private final MbTaskRepository taskRepository;
    private final MbTaskSubmissionRepository submissionRepository;
    private final SysUserRepository sysUserRepository;
    private final MobileWorkService workService;
    private final MobileTaskEnricher taskEnricher;

    public MobileRemixService(ExpMsgRepository expMsgRepository,
                              MbTaskRepository taskRepository,
                              MbTaskSubmissionRepository submissionRepository,
                              SysUserRepository sysUserRepository,
                              MobileWorkService workService,
                              MobileTaskEnricher taskEnricher) {
        this.expMsgRepository = expMsgRepository;
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.sysUserRepository = sysUserRepository;
        this.workService = workService;
        this.taskEnricher = taskEnricher;
    }

    public StartResult start(String userId, String expId, String workId) {
        expId = resolveExpId(expId, workId);
        if (!StringUtils.hasText(expId)) {
            throw new IllegalArgumentException("无法识别该作品关联的实验");
        }
        String studentId = MobileUserContext.resolveStudentId(userId);
        ExpMsg exp = expMsgRepository.findById(expId.trim())
                .orElseThrow(() -> new IllegalArgumentException("实验不存在"));

        Optional<InProgressMatch> inProgress = findInProgressRemix(studentId, expId.trim());
        if (inProgress.isPresent()) {
            InProgressMatch match = inProgress.get();
            MobileTaskDto dto = toEnrichedDto(match.task, match.submission);
            return StartResult.inProgress(dto);
        }

        Date now = new Date();
        MbTask task = buildRemixTask(exp, expId.trim(), now);
        taskRepository.save(task);

        MbTaskSubmission submission = new MbTaskSubmission();
        submission.setSubmissionId(MobileIds.newId("sub"));
        submission.setTaskId(task.getTaskId());
        submission.setStudentUserId(studentId);
        submission.setState("pending");
        submission.setStateLabel("待完成");
        submission.setBadgeClass("badge-warning");
        submission.setCreateTime(now);
        submission.setUpdateTime(now);
        submissionRepository.save(submission);

        task.setUploadQuery("type=remix&taskId=" + task.getTaskId());
        taskRepository.save(task);

        return StartResult.created(toEnrichedDto(task, submission));
    }

    private MobileTaskDto toEnrichedDto(MbTask task, MbTaskSubmission submission) {
        MobileTaskDto dto = MobileEntityMapper.toTaskDto(task, submission);
        taskEnricher.enrich(dto, task);
        return dto;
    }

    private String resolveExpId(String expId, String workId) {
        if (StringUtils.hasText(expId)) {
            return expId.trim();
        }
        if (!StringUtils.hasText(workId)) {
            return null;
        }
        Optional<MbWork> workOpt = workService.findWork(workId.trim());
        if (!workOpt.isPresent()) {
            throw new IllegalArgumentException("作品不存在");
        }
        MbWork work = workOpt.get();
        String resolved = workService.resolveExpIdForWork(work);
        if (StringUtils.hasText(resolved)) {
            workService.persistSourceExpId(work, resolved);
        }
        return resolved;
    }

    private Optional<InProgressMatch> findInProgressRemix(String studentId, String expId) {
        List<MbTaskSubmission> subs = submissionRepository.findByStudentUserIdOrderByUpdateTimeDesc(studentId);
        for (MbTaskSubmission sub : subs) {
            if ("done".equals(sub.getState())) {
                continue;
            }
            Optional<MbTask> taskOpt = taskRepository.findById(sub.getTaskId());
            if (!taskOpt.isPresent()) {
                continue;
            }
            MbTask task = taskOpt.get();
            if (!"remix".equals(task.getTaskType()) || !expId.equals(task.getVideoId())) {
                continue;
            }
            if (!"y".equals(task.getStatus())) {
                continue;
            }
            return Optional.of(new InProgressMatch(task, sub));
        }
        return Optional.empty();
    }

    private MbTask buildRemixTask(ExpMsg exp, String expId, Date now) {
        String expName = StringUtils.hasText(exp.getExpName()) ? exp.getExpName().trim() : "实验";
        MbTask task = new MbTask();
        task.setTaskId(MobileIds.newId("task"));
        task.setTitle("拍同款 · " + expName);
        task.setDescription("参考下方实验视频，拍摄并上传你的同款实验作品。");
        task.setTaskType("remix");
        task.setVideoId(expId);
        task.setVideoTitle(expName);
        task.setVideoMeta(buildVideoMeta(exp));
        task.setTeacherHint("来自视频「拍同款」· 非老师布置作业");
        task.setTeacherHintClass("tint-amber");
        task.setTaskTypeLabel("自愿完成");
        task.setRequirementsJson(MobileJsonUtils.toJson(REMIX_REQUIREMENTS));
        task.setStepsJson(MobileJsonUtils.toJson(REMIX_STEPS));
        task.setStatus("y");
        task.setCreateTime(now);
        return task;
    }

    private String buildVideoMeta(ExpMsg exp) {
        String author = resolveUserName(exp.getCreateUserId());
        if (StringUtils.hasText(author)) {
            return author + " · 科学探究";
        }
        return "科学探究";
    }

    private String resolveUserName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        Optional<SysUser> user = sysUserRepository.findById(userId);
        if (user.isPresent()) {
            SysUser u = user.get();
            if (StringUtils.hasText(u.getUserNickName())) {
                return u.getUserNickName();
            }
            if (StringUtils.hasText(u.getUserName())) {
                return u.getUserName();
            }
        }
        return null;
    }

    public static final class StartResult {
        private final boolean inProgress;
        private final MobileTaskDto task;

        private StartResult(boolean inProgress, MobileTaskDto task) {
            this.inProgress = inProgress;
            this.task = task;
        }

        public static StartResult created(MobileTaskDto task) {
            return new StartResult(false, task);
        }

        public static StartResult inProgress(MobileTaskDto task) {
            return new StartResult(true, task);
        }

        public boolean isInProgress() { return inProgress; }
        public MobileTaskDto getTask() { return task; }
    }

    private static final class InProgressMatch {
        private final MbTask task;
        private final MbTaskSubmission submission;

        private InProgressMatch(MbTask task, MbTaskSubmission submission) {
            this.task = task;
            this.submission = submission;
        }
    }
}
