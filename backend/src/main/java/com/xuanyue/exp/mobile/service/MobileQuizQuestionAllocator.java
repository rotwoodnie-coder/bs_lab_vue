package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.exp.entity.ExpQuestion;
import com.xuanyue.exp.exp.entity.ExpQuestionSelect;
import com.xuanyue.exp.exp.repository.ExpQuestionRepository;
import com.xuanyue.exp.exp.repository.ExpQuestionSelectRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 根据用户与日期，从题库自动分配当日题目（无需人工排期）。
 * 同一用户在同一天始终得到相同题目；不同用户、不同日期会轮转不同题目。
 */
@Service
public class MobileQuizQuestionAllocator {

    private final ExpQuestionRepository questionRepository;
    private final ExpQuestionSelectRepository questionSelectRepository;

    public MobileQuizQuestionAllocator(ExpQuestionRepository questionRepository,
                                       ExpQuestionSelectRepository questionSelectRepository) {
        this.questionRepository = questionRepository;
        this.questionSelectRepository = questionSelectRepository;
    }

    public Optional<String> resolveQuestionId(String userId, LocalDate quizDate) {
        List<String> ids = resolveQuestionIds(userId, quizDate, 1);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    public List<String> resolveQuestionIds(String userId, LocalDate quizDate, int count) {
        if (!StringUtils.hasText(userId) || quizDate == null || count <= 0) {
            return Collections.emptyList();
        }
        List<String> pool = loadEligibleQuestionIds();
        if (pool.isEmpty()) {
            return Collections.emptyList();
        }
        int target = Math.min(count, pool.size());
        List<String> result = new ArrayList<>(target);
        Set<Integer> usedIndices = new HashSet<>();
        for (int slot = 0; slot < target; slot++) {
            int index = selectIndex(userId.trim(), quizDate, slot, pool.size());
            int attempts = 0;
            while (usedIndices.contains(index) && attempts < pool.size()) {
                index = (index + 1) % pool.size();
                attempts++;
            }
            usedIndices.add(index);
            result.add(pool.get(index));
        }
        return result;
    }

    public boolean isEligibleQuestion(String questionId) {
        if (!StringUtils.hasText(questionId)) {
            return false;
        }
        Optional<ExpQuestion> questionOpt = questionRepository.findById(questionId.trim());
        if (!questionOpt.isPresent() || !"y".equalsIgnoreCase(questionOpt.get().getStatus())) {
            return false;
        }
        return hasValidOptions(questionOpt.get().getQuestionId());
    }

    public int countEligibleQuestions() {
        return loadEligibleQuestionIds().size();
    }

    private List<String> loadEligibleQuestionIds() {
        List<ExpQuestion> questions = questionRepository.findByStatusOrderByQuestionIdAsc("y");
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        for (ExpQuestion question : questions) {
            if (question == null || !StringUtils.hasText(question.getQuestionId())) {
                continue;
            }
            if (hasValidOptions(question.getQuestionId())) {
                ids.add(question.getQuestionId());
            }
        }
        return ids;
    }

    private boolean hasValidOptions(String questionId) {
        List<ExpQuestionSelect> selects = questionSelectRepository
                .findByQuestionIdOrderBySortOrderAscSelectIdAsc(questionId);
        if (selects == null || selects.isEmpty()) {
            return false;
        }
        boolean hasContent = false;
        boolean hasRight = false;
        for (ExpQuestionSelect select : selects) {
            if (select != null && StringUtils.hasText(select.getSelectContent())) {
                hasContent = true;
            }
            if (select != null && "y".equalsIgnoreCase(select.getIsRight())) {
                hasRight = true;
            }
        }
        return hasContent && hasRight;
    }

    private int selectIndex(String userId, LocalDate quizDate, int slot, int poolSize) {
        String key = userId + "|" + quizDate + "|" + slot;
        return Math.floorMod(key.hashCode(), poolSize);
    }
}
