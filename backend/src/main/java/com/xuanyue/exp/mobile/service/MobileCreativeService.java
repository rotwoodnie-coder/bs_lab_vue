package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.dto.MobileTaskDto;
import com.xuanyue.exp.mobile.dto.MobileTaskListItemDto;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
import com.xuanyue.exp.mobile.repository.MbTaskSubmissionRepository;
import com.xuanyue.exp.mobile.support.MobileEntityMapper;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.mobile.support.MobileJsonUtils;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MobileCreativeService {

    private static final List<String> CREATIVE_REQUIREMENTS = Arrays.asList(
            "自由选题完成一次实验或探究",
            "至少上传 1 张照片或 1 段短视频",
            "提交后归入作品墙「创意实验」"
    );
    private static final List<String> CREATIVE_STEPS = Arrays.asList(
            "确定创意主题并动手实验",
            "拍摄过程与成果",
            "上传作品完成创意任务"
    );

    private final MbTaskRepository taskRepository;
    private final MbTaskSubmissionRepository submissionRepository;
    private final MobileTaskEnricher taskEnricher;

    public MobileCreativeService(MbTaskRepository taskRepository,
                                   MbTaskSubmissionRepository submissionRepository,
                                   MobileTaskEnricher taskEnricher) {
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.taskEnricher = taskEnricher;
    }

    public StartResult start(String userId) {
        String studentId = MobileUserContext.resolveStudentId(userId);
        Optional<InProgressMatch> inProgress = findInProgressCreative(studentId);
        if (inProgress.isPresent()) {
            InProgressMatch match = inProgress.get();
            return StartResult.inProgress(toDto(match.task, match.submission));
        }

        Date now = new Date();
        MbTask task = buildCreativeTask(now);
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

        task.setUploadQuery("type=creative&taskId=" + task.getTaskId());
        taskRepository.save(task);

        return StartResult.created(toDto(task, submission));
    }

    public MobileTaskListItemDto buildStartCard() {
        MobileTaskListItemDto item = new MobileTaskListItemDto();
        item.setId("creative-start");
        item.setCategory("creative");
        item.setKind("creative-start");
        item.setTitle("开始创意实验");
        item.setDesc("自由上传实验成果，不关联老师布置，提交后归入作品墙「创意实验」");
        item.setState("pending");
        item.setStateLabel("待开始");
        item.setBadgeClass("badge-warning");
        item.setLink("/upload?type=creative");
        item.setSortTime(System.currentTimeMillis());
        return item;
    }

    private MobileTaskDto toDto(MbTask task, MbTaskSubmission submission) {
        MobileTaskDto dto = MobileEntityMapper.toTaskDto(task, submission);
        taskEnricher.enrich(dto, task);
        return dto;
    }

    public Optional<InProgressMatch> findInProgressCreative(String studentId) {
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
            if (!"creative".equals(task.getTaskType()) || !"y".equals(task.getStatus())) {
                continue;
            }
            return Optional.of(new InProgressMatch(task, sub));
        }
        return Optional.empty();
    }

    private MbTask buildCreativeTask(Date now) {
        MbTask task = new MbTask();
        task.setTaskId(MobileIds.newId("task"));
        task.setTitle("创意实验");
        task.setDescription("自由完成一次实验探究并上传成果。");
        task.setTaskType("creative");
        task.setTeacherHint("自主创意 · 非老师布置");
        task.setTeacherHintClass("tint-cyan");
        task.setTaskTypeLabel("自愿完成");
        task.setRequirementsJson(MobileJsonUtils.toJson(CREATIVE_REQUIREMENTS));
        task.setStepsJson(MobileJsonUtils.toJson(CREATIVE_STEPS));
        task.setStatus("y");
        task.setCreateTime(now);
        return task;
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

    public static final class InProgressMatch {
        public final MbTask task;
        public final MbTaskSubmission submission;

        public InProgressMatch(MbTask task, MbTaskSubmission submission) {
            this.task = task;
            this.submission = submission;
        }
    }
}
