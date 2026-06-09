package com.xuanyue.exp.mobile.admin.service;

import com.xuanyue.exp.mobile.admin.dto.MobileBadgeDefDto;
import com.xuanyue.exp.mobile.admin.dto.MobileBadgeGrantRequest;
import com.xuanyue.exp.mobile.admin.dto.MobileBadgeProgressItemDto;
import com.xuanyue.exp.mobile.admin.support.MobileAdminAuthSupport;
import com.xuanyue.exp.mobile.entity.MbBadgeDef;
import com.xuanyue.exp.mobile.entity.MbBadgeProgress;
import com.xuanyue.exp.mobile.repository.MbBadgeDefRepository;
import com.xuanyue.exp.mobile.repository.MbBadgeProgressRepository;
import com.xuanyue.exp.mobile.service.MobileBadgeGrantService;
import com.xuanyue.exp.mobile.support.MobileIds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MobileBadgeAdminService {

    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final MobileAdminAuthSupport authSupport;
    private final MbBadgeDefRepository badgeDefRepository;
    private final MbBadgeProgressRepository badgeProgressRepository;
    private final MobileBadgeGrantService badgeGrantService;

    public MobileBadgeAdminService(MobileAdminAuthSupport authSupport,
                                   MbBadgeDefRepository badgeDefRepository,
                                   MbBadgeProgressRepository badgeProgressRepository,
                                   MobileBadgeGrantService badgeGrantService) {
        this.authSupport = authSupport;
        this.badgeDefRepository = badgeDefRepository;
        this.badgeProgressRepository = badgeProgressRepository;
        this.badgeGrantService = badgeGrantService;
    }

    public List<MobileBadgeDefDto> listDefinitions() {
        authSupport.requireBadgeManager();
        return badgeDefRepository.findAll().stream()
                .sorted((a, b) -> {
                    int sa = a.getSortOrder() != null ? a.getSortOrder() : 0;
                    int sb = b.getSortOrder() != null ? b.getSortOrder() : 0;
                    return Integer.compare(sa, sb);
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MobileBadgeDefDto saveDefinition(MobileBadgeDefDto dto) {
        authSupport.requireBadgeManager();
        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            throw new IllegalArgumentException("勋章名称不能为空");
        }
        MbBadgeDef entity;
        if (StringUtils.hasText(dto.getBadgeId())) {
            entity = badgeDefRepository.findById(dto.getBadgeId().trim())
                    .orElseGet(MbBadgeDef::new);
            entity.setBadgeId(dto.getBadgeId().trim());
        } else {
            entity = new MbBadgeDef();
            entity.setBadgeId(MobileIds.newId("badge"));
        }
        entity.setIcon(StringUtils.hasText(dto.getIcon()) ? dto.getIcon().trim() : "🏅");
        entity.setTitle(dto.getTitle().trim());
        entity.setDescription(dto.getDescription());
        entity.setCriteriaType(dto.getCriteriaType());
        entity.setCriteriaValue(dto.getCriteriaValue());
        entity.setRewardPoints(dto.getRewardPoints() != null ? dto.getRewardPoints() : 0);
        entity.setActionRoute(dto.getActionRoute());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus().trim() : "y");
        badgeDefRepository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public void deleteDefinition(String badgeId) {
        authSupport.requireBadgeManager();
        if (!StringUtils.hasText(badgeId)) {
            throw new IllegalArgumentException("badgeId 不能为空");
        }
        badgeDefRepository.deleteById(badgeId.trim());
    }

    public List<MobileBadgeProgressItemDto> listProgress(String userId) {
        authSupport.requireBadgeManager();
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        Map<String, MbBadgeDef> defs = badgeDefRepository.findAll().stream()
                .collect(Collectors.toMap(MbBadgeDef::getBadgeId, d -> d, (a, b) -> a));
        List<MbBadgeProgress> progressList = badgeProgressRepository.findByUserId(userId.trim());
        List<MobileBadgeProgressItemDto> items = new ArrayList<MobileBadgeProgressItemDto>();
        for (MbBadgeProgress progress : progressList) {
            MbBadgeDef def = defs.get(progress.getBadgeId());
            MobileBadgeProgressItemDto item = new MobileBadgeProgressItemDto();
            item.setBadgeId(progress.getBadgeId());
            item.setEarned(progress.getEarned());
            item.setProgressCurrent(progress.getProgressCurrent());
            item.setProgressTarget(progress.getProgressTarget());
            if (progress.getEarnedTime() != null) {
                item.setEarnedTime(TIME_FMT.format(progress.getEarnedTime()));
            }
            if (def != null) {
                item.setTitle(def.getTitle());
                item.setIcon(def.getIcon());
            }
            items.add(item);
        }
        return items;
    }

    @Transactional
    public void grant(MobileBadgeGrantRequest request) {
        authSupport.requireSysAdmin();
        if (request == null || !StringUtils.hasText(request.getUserId()) || !StringUtils.hasText(request.getBadgeId())) {
            throw new IllegalArgumentException("userId 与 badgeId 不能为空");
        }
        boolean earned = request.getEarned() == null || Boolean.TRUE.equals(request.getEarned());
        badgeGrantService.manualGrant(request.getUserId().trim(), request.getBadgeId().trim(), earned);
    }

    private MobileBadgeDefDto toDto(MbBadgeDef entity) {
        MobileBadgeDefDto dto = new MobileBadgeDefDto();
        dto.setBadgeId(entity.getBadgeId());
        dto.setIcon(entity.getIcon());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCriteriaType(entity.getCriteriaType());
        dto.setCriteriaValue(entity.getCriteriaValue());
        dto.setRewardPoints(entity.getRewardPoints());
        dto.setActionRoute(entity.getActionRoute());
        dto.setSortOrder(entity.getSortOrder());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
