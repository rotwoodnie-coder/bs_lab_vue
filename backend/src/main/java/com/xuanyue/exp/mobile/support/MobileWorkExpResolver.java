package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbWork;
import com.xuanyue.exp.mobile.repository.MbTaskRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class MobileWorkExpResolver {

    private final MbTaskRepository taskRepository;
    private final ExpMsgRepository expMsgRepository;

    public MobileWorkExpResolver(MbTaskRepository taskRepository, ExpMsgRepository expMsgRepository) {
        this.taskRepository = taskRepository;
        this.expMsgRepository = expMsgRepository;
    }

    public String resolve(MbWork work) {
        if (work == null) {
            return null;
        }
        if (StringUtils.hasText(work.getSourceExpId())) {
            return work.getSourceExpId().trim();
        }
        String fromTask = resolveFromTaskId(work.getTaskId());
        if (StringUtils.hasText(fromTask)) {
            return fromTask;
        }
        return matchExpIdByTitle(work.getTitle());
    }

    public String resolveFromTaskId(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return null;
        }
        Optional<MbTask> taskOpt = taskRepository.findById(taskId.trim());
        if (taskOpt.isPresent() && StringUtils.hasText(taskOpt.get().getVideoId())) {
            return taskOpt.get().getVideoId().trim();
        }
        return null;
    }

    public String matchExpIdByTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return null;
        }
        String normalized = title.trim();
        Optional<ExpMsg> exact = expMsgRepository.findFirstByExpNameAndStatus(normalized, "y");
        if (exact.isPresent()) {
            return exact.get().getExpId();
        }
        List<ExpMsg> fuzzy = expMsgRepository.findTitleMatches("y", normalized, PageRequest.of(0, 20));
        if (fuzzy.isEmpty()) {
            return null;
        }
        return fuzzy.stream()
                .max(Comparator.comparingInt(e -> e.getExpName() != null ? e.getExpName().length() : 0))
                .map(ExpMsg::getExpId)
                .orElse(null);
    }
}
