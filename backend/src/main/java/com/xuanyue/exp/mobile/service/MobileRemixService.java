package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.mobile.dto.MobileTaskDto;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Optional;

/**
 * MobileRemixService — 重构版
 *
 * 拍同款：使用 exp_msg (exp_type='student', exp_task_type='tk') 存储，
 * 方法签名与返回 DTO 不变。
 */
@Service
public class MobileRemixService {

    private final ExpMsgRepository expMsgRepository;
    private final MobileStudentWorkService studentWorkService;
    private final SysUserRepository sysUserRepository;

    public MobileRemixService(ExpMsgRepository expMsgRepository,
                              MobileStudentWorkService studentWorkService,
                              SysUserRepository sysUserRepository) {
        this.expMsgRepository = expMsgRepository;
        this.studentWorkService = studentWorkService;
        this.sysUserRepository = sysUserRepository;
    }

    public StartResult start(String userId, String expId, String workId) {
        expId = resolveExpId(expId, workId);
        if (!StringUtils.hasText(expId)) {
            throw new IllegalArgumentException("无法识别该作品关联的实验");
        }
        String studentId = MobileUserContext.resolveStudentId(userId);
        if (!StringUtils.hasText(studentId)) {
            throw new IllegalArgumentException("请先登录");
        }

        // 检查是否已有进行中的拍同款草稿
        Optional<ExpMsg> inProgress = expMsgRepository
                .findTopByCreateUserIdAndStatusAndExpTypeOrderByCreateTimeDesc(studentId, "draft", "student");
        if (inProgress.isPresent() && "tk".equals(inProgress.get().getExpTaskType())) {
            return StartResult.inProgress(studentWorkService.toTaskDto(inProgress.get()));
        }

        // 创建新的拍同款草稿
        MobileTaskDto dto = studentWorkService.startStudentTask(userId, "remix", expId);
        return StartResult.created(dto);
    }

    private String resolveExpId(String expId, String workId) {
        if (StringUtils.hasText(expId)) {
            return expId.trim();
        }
        if (StringUtils.hasText(workId)) {
            return expMsgRepository.findById(workId.trim())
                    .map(ExpMsg::getLinkExpId)
                    .orElse(null);
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
}
