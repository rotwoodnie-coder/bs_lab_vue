package com.xuanyue.exp.mobile.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanyue.exp.mobile.dto.MobileGrowthDto;
import com.xuanyue.exp.mobile.entity.MbGrowthPlan;
import com.xuanyue.exp.mobile.repository.MbGrowthPlanRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class MobileGrowthPlanSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MbGrowthPlanRepository growthPlanRepository;

    public MobileGrowthPlanSupport(MbGrowthPlanRepository growthPlanRepository) {
        this.growthPlanRepository = growthPlanRepository;
    }

    public MobileGrowthDto.Plan resolvePlan(String userId) {
        if (!StringUtils.hasText(userId)) {
            return defaultPlanDto();
        }
        return growthPlanRepository.findById(userId.trim())
                .map(this::toPlanDto)
                .orElseGet(this::defaultPlanDto);
    }

    public MobileGrowthDto.Plan defaultPlanDto() {
        MobileGrowthDto.Plan plan = new MobileGrowthDto.Plan();
        plan.setContentKeys(Arrays.asList("exp", "work", "badge", "quiz"));
        plan.setVisibilityKey("parent");
        plan.setRangeKey("term");
        plan.setContent(buildPlanContentLabel(plan.getContentKeys()));
        plan.setVisibility("本人 + 家长");
        plan.setRange("本学期");
        return plan;
    }

    public MobileGrowthDto.Plan toPlanDto(MbGrowthPlan entity) {
        MobileGrowthDto.Plan plan = new MobileGrowthDto.Plan();
        List<String> keys = Arrays.asList("exp", "work", "badge", "quiz");
        if (StringUtils.hasText(entity.getContentKeysJson())) {
            try {
                keys = MAPPER.readValue(entity.getContentKeysJson(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {
            }
        }
        plan.setContentKeys(keys);
        plan.setVisibilityKey(entity.getVisibility());
        plan.setRangeKey(entity.getRange());
        plan.setContent(buildPlanContentLabel(keys));
        plan.setVisibility(visibilityLabel(entity.getVisibility()));
        plan.setRange(rangeLabel(entity.getRange()));
        return plan;
    }

    public String buildPlanContentLabel(List<String> keys) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("exp", "实验");
        map.put("work", "作品");
        map.put("badge", "勋章");
        map.put("quiz", "答题");
        map.put("rank", "排名");
        if (keys == null || keys.isEmpty()) {
            return "";
        }
        return keys.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.joining(" · "));
    }

    public String visibilityLabel(String key) {
        if ("self".equals(key)) {
            return "本人";
        }
        if ("class".equals(key)) {
            return "本人 + 家长 + 班级";
        }
        return "本人 + 家长";
    }

    public String rangeLabel(String key) {
        if ("year".equals(key)) {
            return "本学年";
        }
        if ("all".equals(key)) {
            return "全部记录";
        }
        return "本学期";
    }
}
