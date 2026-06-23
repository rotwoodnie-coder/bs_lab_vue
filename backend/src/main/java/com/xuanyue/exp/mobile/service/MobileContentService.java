package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.exp.entity.ExpMaterial;
import com.xuanyue.exp.exp.entity.ExpMaterialPic;
import com.xuanyue.exp.exp.entity.ExpReference;
import com.xuanyue.exp.exp.entity.ExpResult;
import com.xuanyue.exp.exp.entity.ExpScientist;
import com.xuanyue.exp.exp.entity.ExpStep;
import com.xuanyue.exp.exp.entity.ExpVideo;
import com.xuanyue.exp.exp.repository.ExpMaterialPicRepository;
import com.xuanyue.exp.exp.repository.ExpMaterialRepository;
import com.xuanyue.exp.exp.repository.ExpReferenceRepository;
import com.xuanyue.exp.exp.repository.ExpResultRepository;
import com.xuanyue.exp.exp.repository.ExpScientistRepository;
import com.xuanyue.exp.exp.repository.ExpStepRepository;
import com.xuanyue.exp.exp.repository.ExpVideoRepository;
import com.xuanyue.exp.exp.service.ExpStandardService;
import com.xuanyue.exp.mobile.dto.MobileExpMaterialDto;
import com.xuanyue.exp.mobile.dto.MobileExpVideoDto;
import com.xuanyue.exp.mobile.support.MobileMediaUrlSupport;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 移动端实验内容读取：只读共享表，媒体 URL 在 mobile 层 enrich。
 */
@Service
public class MobileContentService {

    private final ExpStandardService standardService;
    private final ExpVideoRepository videoRepository;
    private final ExpStepRepository stepRepository;
    private final ExpMaterialRepository materialRepository;
    private final ExpMaterialPicRepository materialPicRepository;
    private final ExpResultRepository resultRepository;
    private final ExpReferenceRepository referenceRepository;
    private final ExpScientistRepository scientistRepository;
    private final MinioStorageService minioStorageService;

    public MobileContentService(ExpStandardService standardService,
                                ExpVideoRepository videoRepository,
                                ExpStepRepository stepRepository,
                                ExpMaterialRepository materialRepository,
                                ExpMaterialPicRepository materialPicRepository,
                                ExpResultRepository resultRepository,
                                ExpReferenceRepository referenceRepository,
                                ExpScientistRepository scientistRepository,
                                MinioStorageService minioStorageService) {
        this.standardService = standardService;
        this.videoRepository = videoRepository;
        this.stepRepository = stepRepository;
        this.materialRepository = materialRepository;
        this.materialPicRepository = materialPicRepository;
        this.resultRepository = resultRepository;
        this.referenceRepository = referenceRepository;
        this.scientistRepository = scientistRepository;
        this.minioStorageService = minioStorageService;
    }

    public Map<String, Object> getDetail(String expId) {
        Map<String, Object> detail = standardService.getDetailView(expId);
        if (detail == null) {
            return null;
        }
        enrichDetailMap(detail);
        return detail;
    }

    public List<MobileExpVideoDto> listVideos(String expId) {
        return videoRepository.findByExpIdOrderBySortOrderAsc(expId).stream()
                .map(this::toVideoDto)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listSteps(String expId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ExpStep step : stepRepository.findByExpIdOrderBySortOrderAsc(expId)) {
            Map<String, Object> row = new HashMap<>();
            row.put("stepId", step.getStepId());
            row.put("expId", step.getExpId());
            row.put("stepName", step.getStepName());
            row.put("stepComments", MobileMediaUrlSupport.enrichRichText(minioStorageService, step.getStepComments()));
            row.put("sortOrder", step.getSortOrder());
            list.add(row);
        }
        return list;
    }

    public List<MobileExpMaterialDto> listMaterials(String expId) {
        return materialRepository.findByExpIdOrderBySortOrderAsc(expId).stream()
                .map(this::toMaterialDto)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listResults(String expId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ExpResult result : resultRepository.findByExpIdOrderBySortOrderAsc(expId)) {
            Map<String, Object> row = new HashMap<>();
            row.put("resultId", result.getResultId());
            row.put("expId", result.getExpId());
            row.put("resultName", result.getResultName());
            row.put("resultComments", MobileMediaUrlSupport.enrichRichText(minioStorageService, result.getResultComments()));
            row.put("sortOrder", result.getSortOrder());
            list.add(row);
        }
        return list;
    }

    public List<Map<String, Object>> listReferences(String expId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ExpReference reference : referenceRepository.findByExpIdOrderBySortOrderAsc(expId)) {
            Map<String, Object> row = new HashMap<>();
            row.put("referenceId", reference.getReferenceId());
            row.put("expId", reference.getExpId());
            row.put("referenceName", reference.getReferenceName());
            row.put("referenceSource", reference.getReferenceSource());
            row.put("referenceComments", MobileMediaUrlSupport.enrichRichText(minioStorageService, reference.getReferenceComments()));
            row.put("sortOrder", reference.getSortOrder());
            list.add(row);
        }
        return list;
    }

    public List<Map<String, Object>> listScientists(String expId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ExpScientist scientist : scientistRepository.findByExpIdOrderBySortOrderAsc(expId)) {
            Map<String, Object> row = new HashMap<>();
            row.put("scientistId", scientist.getScientistId());
            row.put("expId", scientist.getExpId());
            row.put("scientistName", scientist.getScientistName());
            row.put("storyName", scientist.getStoryName());
            row.put("storyComments", MobileMediaUrlSupport.enrichRichText(minioStorageService, scientist.getStoryComments()));
            row.put("sortOrder", scientist.getSortOrder());
            list.add(row);
        }
        return list;
    }

    private void enrichDetailMap(Map<String, Object> detail) {
        enrichMapField(detail, "expPrinciple");
        enrichMapField(detail, "expCaution");
        enrichMapField(detail, "expDanger");
        enrichMapField(detail, "simulatorUrl");
        if (detail.get("simulatorUrl") instanceof String) {
            String simulatorUrl = (String) detail.get("simulatorUrl");
            if (StringUtils.hasText(simulatorUrl) && !simulatorUrl.startsWith("http")) {
                detail.put("simulatorPreviewUrl", MobileMediaUrlSupport.resolve(minioStorageService, simulatorUrl));
            } else {
                detail.put("simulatorPreviewUrl", simulatorUrl);
            }
        }
    }

    private void enrichMapField(Map<String, Object> detail, String key) {
        Object value = detail.get(key);
        if (value instanceof String) {
            detail.put(key, MobileMediaUrlSupport.enrichRichText(minioStorageService, (String) value));
        }
    }

    private MobileExpVideoDto toVideoDto(ExpVideo video) {
        MobileExpVideoDto dto = new MobileExpVideoDto();
        dto.setSeqId(video.getSeqId());
        dto.setExpId(video.getExpId());
        dto.setVideoUrl(video.getVideoUrl());
        dto.setPreviewUrl(MobileMediaUrlSupport.resolve(minioStorageService, video.getVideoUrl()));
        dto.setSortOrder(video.getSortOrder());
        dto.setFileId(video.getFileId());
        return dto;
    }

    private String resolveMaterialMainPicUrl(ExpMaterial material) {
        List<String> candidates = new ArrayList<>();
        if (StringUtils.hasText(material.getMainPicUrl())) {
            candidates.add(material.getMainPicUrl().trim());
        }
        for (ExpMaterialPic pic : materialPicRepository.findByExpMaterialIdOrderBySortOrderAsc(material.getExpMaterialId())) {
            if (StringUtils.hasText(pic.getMaterialUrl())) {
                candidates.add(pic.getMaterialUrl().trim());
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return MobileMediaUrlSupport.pickBestMediaUrl(candidates.toArray(new String[0]));
    }

    private MobileExpMaterialDto toMaterialDto(ExpMaterial material) {
        MobileExpMaterialDto dto = new MobileExpMaterialDto();
        dto.setExpMaterialId(material.getExpMaterialId());
        dto.setExpId(material.getExpId());
        dto.setMaterialId(material.getMaterialId());
        dto.setMaterialName(material.getMaterialName());
        dto.setMaterialPropId(material.getMaterialPropId());
        dto.setMaterialTypeId(material.getMaterialTypeId());
        dto.setMaterialNum(material.getMaterialNum());

        String mainPicUrl = resolveMaterialMainPicUrl(material);
        dto.setMainPicUrl(mainPicUrl);
        dto.setMainPicPreviewUrl(MobileMediaUrlSupport.resolve(minioStorageService, mainPicUrl));
        dto.setExpPurpose(material.getExpPurpose());
        dto.setSecurityComments(material.getSecurityComments());
        dto.setAdditionalComments(material.getAdditionalComments());
        dto.setSortOrder(material.getSortOrder());
        return dto;
    }
}
