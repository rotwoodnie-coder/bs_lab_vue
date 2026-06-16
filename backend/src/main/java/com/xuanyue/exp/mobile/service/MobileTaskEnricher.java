package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.exp.entity.ExpMaterial;
import com.xuanyue.exp.exp.entity.ExpStep;
import com.xuanyue.exp.exp.repository.ExpMaterialRepository;
import com.xuanyue.exp.exp.repository.ExpStepRepository;
import com.xuanyue.exp.exp.entity.ExpSimulator;
import com.xuanyue.exp.exp.repository.ExpSimulatorRepository;
import com.xuanyue.exp.exp.service.ExpStandardService;
import com.xuanyue.exp.mobile.dto.MobileTaskDto;
import com.xuanyue.exp.mobile.dto.MobileTaskExpBriefDto;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.support.MobileJsonUtils;
import com.xuanyue.exp.mobile.support.MobileTextUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MobileTaskEnricher {

    private static final List<String> REMIX_COMPLETION_GUIDE = Arrays.asList(
            "回看参考视频，对照实验资料完成同款实验",
            "拍摄实验过程，上传至少一张照片或短视频",
            "提交后由老师审核，通过后展示在作品墙「拍同款」"
    );

    private static final List<String> CREATIVE_COMPLETION_GUIDE = Arrays.asList(
            "确定创意主题并完成实验探究",
            "拍摄过程与成果，上传至少一张照片或短视频",
            "提交后由老师审核，通过后展示在作品墙「创意实验」"
    );

    private static final List<String> SIMULATOR_COMPLETION_FALLBACK = Arrays.asList(
            "完成在线模拟实验操作",
            "整理实验截图或心得后提交"
    );

    private static final List<String> HOMEWORK_COMPLETION_FALLBACK = Arrays.asList(
            "观看关联视频并完成实验操作",
            "整理成果照片或短视频后提交"
    );

    private final ExpStandardService expStandardService;
    private final ExpMaterialRepository materialRepository;
    private final ExpStepRepository stepRepository;
    private final ExpSimulatorRepository simulatorRepository;

    public MobileTaskEnricher(ExpStandardService expStandardService,
                              ExpMaterialRepository materialRepository,
                              ExpStepRepository stepRepository,
                              ExpSimulatorRepository simulatorRepository) {
        this.expStandardService = expStandardService;
        this.materialRepository = materialRepository;
        this.stepRepository = stepRepository;
        this.simulatorRepository = simulatorRepository;
    }

    public void enrich(MobileTaskDto dto, MbTask task) {
        if (dto == null || task == null) {
            return;
        }
        if (StringUtils.hasText(task.getVideoId())) {
            if ("simulator".equalsIgnoreCase(task.getTaskType())
                    || simulatorRepository.existsById(task.getVideoId().trim())) {
                dto.setExpBrief(buildSimulatorBrief(task.getVideoId().trim()));
            } else {
                dto.setExpBrief(buildExpBrief(task.getVideoId().trim()));
            }
        }
        dto.setCompletionGuide(buildCompletionGuide(task));
    }

    public MobileTaskExpBriefDto buildExpBrief(String expId) {
        MobileTaskExpBriefDto brief = new MobileTaskExpBriefDto();
        if (!StringUtils.hasText(expId)) {
            return brief;
        }
        Map<String, Object> detail = expStandardService.getDetailView(expId);
        if (detail != null) {
            brief.setCurriculumLine(buildCurriculumLine(detail));
            brief.setSafetyLine(buildSafetyLine(detail));
        }

        List<ExpMaterial> materials = materialRepository.findByExpIdOrderBySortOrderAsc(expId);
        if (materials != null && !materials.isEmpty()) {
            String names = materials.stream()
                    .map(ExpMaterial::getMaterialName)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .stream()
                    .collect(Collectors.joining("、"));
            if (StringUtils.hasText(names)) {
                brief.setMaterialsLine(names);
            }
        }

        List<ExpStep> steps = stepRepository.findByExpIdOrderBySortOrderAsc(expId);
        if (steps != null && !steps.isEmpty()) {
            String line = steps.stream()
                    .map(ExpStep::getStepName)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.joining(" → "));
            if (StringUtils.hasText(line)) {
                brief.setStepsLine(line);
            }
        }
        return brief;
    }

    public MobileTaskExpBriefDto buildSimulatorBrief(String simulatorId) {
        MobileTaskExpBriefDto brief = new MobileTaskExpBriefDto();
        if (!StringUtils.hasText(simulatorId)) {
            return brief;
        }
        ExpSimulator simulator = simulatorRepository.findById(simulatorId.trim()).orElse(null);
        if (simulator == null) {
            return brief;
        }
        if (StringUtils.hasText(simulator.getComments())) {
            brief.setStepsLine(MobileTextUtils.toPlainOneLine(simulator.getComments(), 200));
        }
        return brief;
    }

    private String buildCurriculumLine(Map<String, Object> detail) {
        List<String> parts = new ArrayList<>();
        appendCurriculumPart(parts, "教材", detail.get("coursebookName"));
        appendCurriculumPart(parts, "单元", detail.get("unitName"));
        appendCurriculumPart(parts, "章", detail.get("chapterName"));
        appendCurriculumPart(parts, "节", detail.get("sectionName"));
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }

    private void appendCurriculumPart(List<String> parts, String label, Object value) {
        String text = value == null ? null : String.valueOf(value).trim();
        if (StringUtils.hasText(text)) {
            parts.add(label + "：" + text);
        }
    }

    private String buildSafetyLine(Map<String, Object> detail) {
        String danger = MobileTextUtils.toPlainOneLine(asString(detail.get("expDanger")), 80);
        String caution = MobileTextUtils.toPlainOneLine(asString(detail.get("expCaution")), 80);
        if (StringUtils.hasText(danger) && StringUtils.hasText(caution)) {
            return danger + "；" + caution;
        }
        if (StringUtils.hasText(danger)) {
            return danger;
        }
        return caution;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public List<String> buildCompletionGuide(MbTask task) {
        if ("remix".equals(task.getTaskType())) {
            return new ArrayList<>(REMIX_COMPLETION_GUIDE);
        }
        if ("creative".equals(task.getTaskType())) {
            return new ArrayList<>(CREATIVE_COMPLETION_GUIDE);
        }
        if ("simulator".equals(task.getTaskType()) || isSimulatorVideoId(task.getVideoId())) {
            LinkedHashSet<String> lines = new LinkedHashSet<>();
            collectGuideLines(lines, task);
            if (lines.isEmpty()) {
                return new ArrayList<>(SIMULATOR_COMPLETION_FALLBACK);
            }
            return trimGuide(lines);
        }
        LinkedHashSet<String> lines = new LinkedHashSet<>();
        collectGuideLines(lines, task);
        if (lines.isEmpty()) {
            return new ArrayList<>(HOMEWORK_COMPLETION_FALLBACK);
        }
        return trimGuide(lines);
    }

    private void collectGuideLines(LinkedHashSet<String> lines, MbTask task) {
        List<String> requirements = MobileJsonUtils.parseStringList(task.getRequirementsJson());
        List<String> steps = MobileJsonUtils.parseStringList(task.getStepsJson());
        if (requirements != null) {
            for (String item : requirements) {
                addGuideLine(lines, item);
            }
        }
        if (steps != null) {
            for (String item : steps) {
                addGuideLine(lines, item);
            }
        }
    }

    private List<String> trimGuide(LinkedHashSet<String> lines) {
        List<String> result = new ArrayList<>(lines);
        if (result.size() > 4) {
            return result.subList(0, 4);
        }
        return result;
    }

    private void addGuideLine(LinkedHashSet<String> lines, String raw) {
        if (!StringUtils.hasText(raw)) {
            return;
        }
        String line = raw.trim();
        if (line.startsWith("•")) {
            line = line.substring(1).trim();
        }
        if (StringUtils.hasText(line)) {
            lines.add(line);
        }
    }

    private boolean isSimulatorVideoId(String videoId) {
        return StringUtils.hasText(videoId) && simulatorRepository.existsById(videoId.trim());
    }
}
