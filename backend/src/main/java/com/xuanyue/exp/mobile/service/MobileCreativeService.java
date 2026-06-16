package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.mobile.dto.MobileTaskDto;
import com.xuanyue.exp.mobile.dto.MobileTaskListItemDto;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * MobileCreativeService — 重构版
 *
 * 创意实验：使用 exp_msg (exp_type='student', exp_task_type='self') 存储，
 * 方法签名与返回 DTO 不变。
 */
@Service
public class MobileCreativeService {

    private final ExpMsgRepository expMsgRepository;
    private final MobileStudentWorkService studentWorkService;

    public MobileCreativeService(ExpMsgRepository expMsgRepository,
                                 MobileStudentWorkService studentWorkService) {
        this.expMsgRepository = expMsgRepository;
        this.studentWorkService = studentWorkService;
    }

    public StartResult start(String userId) {
        String studentId = MobileUserContext.resolveStudentId(userId);
        if (!StringUtils.hasText(studentId)) {
            throw new IllegalArgumentException("请先登录");
        }

        // 检查是否已有进行中的创意实验草稿
        Optional<ExpMsg> inProgress = expMsgRepository
                .findTopByCreateUserIdAndStatusAndExpTypeOrderByCreateTimeDesc(studentId, "draft", "student");
        if (inProgress.isPresent() && "self".equals(inProgress.get().getExpTaskType())) {
            return StartResult.inProgress(studentWorkService.toTaskDto(inProgress.get()));
        }

        MobileTaskDto dto = studentWorkService.startStudentTask(userId, "creative", null);
        return StartResult.created(dto);
    }

    public MobileTaskListItemDto buildStartCard() {
        return studentWorkService.buildStartCard("creative");
    }

    public Optional<InProgressMatch> findInProgressCreative(String studentId) {
        if (!StringUtils.hasText(studentId)) return Optional.empty();
        Optional<ExpMsg> inProgress = expMsgRepository
                .findTopByCreateUserIdAndStatusAndExpTypeOrderByCreateTimeDesc(studentId, "draft", "student");
        if (inProgress.isPresent() && "self".equals(inProgress.get().getExpTaskType())) {
            return Optional.of(new InProgressMatch(inProgress.get()));
        }
        return Optional.empty();
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
        public final ExpMsg msg;

        public InProgressMatch(ExpMsg msg) {
            this.msg = msg;
        }
    }
}
