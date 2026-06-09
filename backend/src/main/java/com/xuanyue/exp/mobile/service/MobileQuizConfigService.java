package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.dto.MobileQuizConfigDto;
import com.xuanyue.exp.mobile.dto.QuizConfigSaveRequest;
import com.xuanyue.exp.mobile.entity.MbQuizConfig;
import com.xuanyue.exp.mobile.repository.MbQuizConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class MobileQuizConfigService {

    private static final int DEFAULT_QUESTIONS_PER_DAY = 3;
    private static final int DEFAULT_BASE_POINTS = 10;
    private static final int DEFAULT_STREAK_BONUS = 5;
    private static final int MIN_QUESTIONS_PER_DAY = 1;
    private static final int MAX_QUESTIONS_PER_DAY = 10;

    private final MbQuizConfigRepository configRepository;
    private final MobileQuizQuestionAllocator questionAllocator;

    public MobileQuizConfigService(MbQuizConfigRepository configRepository,
                                   MobileQuizQuestionAllocator questionAllocator) {
        this.configRepository = configRepository;
        this.questionAllocator = questionAllocator;
    }

    public MbQuizConfig getEffectiveConfig() {
        MbQuizConfig config = configRepository.findById(MbQuizConfig.DEFAULT_ID).orElseGet(this::createDefaultConfig);
        normalizeConfig(config);
        return config;
    }

    public int getQuestionsPerDay() {
        return getEffectiveConfig().getQuestionsPerDay();
    }

    public int getBasePoints() {
        return getEffectiveConfig().getBasePoints();
    }

    public int getStreakBonus() {
        return getEffectiveConfig().getStreakBonus();
    }

    public boolean isEnabled() {
        return "y".equalsIgnoreCase(getEffectiveConfig().getEnabled());
    }

    public MobileQuizConfigDto getConfigView() {
        MbQuizConfig config = getEffectiveConfig();
        int eligible = questionAllocator.countEligibleQuestions();
        MobileQuizConfigDto dto = new MobileQuizConfigDto();
        dto.setQuestionsPerDay(config.getQuestionsPerDay());
        dto.setBasePoints(config.getBasePoints());
        dto.setStreakBonus(config.getStreakBonus());
        dto.setEnabled(isEnabled());
        dto.setEligibleQuestionCount(eligible);
        if (eligible > 0 && config.getQuestionsPerDay() > eligible) {
            dto.setPoolWarning("可用题库仅 " + eligible + " 题，实际每日将分配 " + eligible + " 题");
        } else if (eligible == 0) {
            dto.setPoolWarning("题库暂无可用题目，请先在实验题库中发布题目");
        }
        return dto;
    }

    @Transactional
    public MobileQuizConfigDto saveConfig(QuizConfigSaveRequest request, String operatorId) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        MbQuizConfig config = configRepository.findById(MbQuizConfig.DEFAULT_ID).orElseGet(this::createDefaultConfig);
        if (request.getQuestionsPerDay() != null) {
            int count = request.getQuestionsPerDay();
            if (count < MIN_QUESTIONS_PER_DAY || count > MAX_QUESTIONS_PER_DAY) {
                throw new IllegalArgumentException("每日题数须在 " + MIN_QUESTIONS_PER_DAY + "~" + MAX_QUESTIONS_PER_DAY + " 之间");
            }
            config.setQuestionsPerDay(count);
        }
        if (request.getBasePoints() != null) {
            if (request.getBasePoints() < 0) {
                throw new IllegalArgumentException("基础积分不能为负数");
            }
            config.setBasePoints(request.getBasePoints());
        }
        if (request.getStreakBonus() != null) {
            if (request.getStreakBonus() < 0) {
                throw new IllegalArgumentException("连对奖励不能为负数");
            }
            config.setStreakBonus(request.getStreakBonus());
        }
        if (request.getEnabled() != null) {
            config.setEnabled(request.getEnabled() ? "y" : "n");
        }
        if (StringUtils.hasText(operatorId)) {
            config.setUpdateBy(operatorId.trim());
        }
        config.setUpdateTime(new Date());
        normalizeConfig(config);
        configRepository.save(config);
        return getConfigView();
    }

    private MbQuizConfig createDefaultConfig() {
        MbQuizConfig config = new MbQuizConfig();
        config.setConfigId(MbQuizConfig.DEFAULT_ID);
        config.setQuestionsPerDay(DEFAULT_QUESTIONS_PER_DAY);
        config.setBasePoints(DEFAULT_BASE_POINTS);
        config.setStreakBonus(DEFAULT_STREAK_BONUS);
        config.setEnabled("y");
        config.setUpdateTime(new Date());
        return configRepository.save(config);
    }

    private void normalizeConfig(MbQuizConfig config) {
        if (config.getQuestionsPerDay() == null || config.getQuestionsPerDay() < MIN_QUESTIONS_PER_DAY) {
            config.setQuestionsPerDay(DEFAULT_QUESTIONS_PER_DAY);
        }
        if (config.getQuestionsPerDay() > MAX_QUESTIONS_PER_DAY) {
            config.setQuestionsPerDay(MAX_QUESTIONS_PER_DAY);
        }
        if (config.getBasePoints() == null || config.getBasePoints() < 0) {
            config.setBasePoints(DEFAULT_BASE_POINTS);
        }
        if (config.getStreakBonus() == null || config.getStreakBonus() < 0) {
            config.setStreakBonus(DEFAULT_STREAK_BONUS);
        }
        if (!StringUtils.hasText(config.getEnabled())) {
            config.setEnabled("y");
        }
    }
}
