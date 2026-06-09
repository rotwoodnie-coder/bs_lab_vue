package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.data.entity.MaterialPic;
import com.xuanyue.exp.data.entity.MaterialMsg;
import com.xuanyue.exp.data.repository.MaterialMsgRepository;
import com.xuanyue.exp.data.repository.MaterialPicRepository;
import com.xuanyue.exp.exp.entity.ExpMaterial;
import com.xuanyue.exp.exp.entity.ExpMaterialPic;
import com.xuanyue.exp.exp.repository.ExpMaterialPicRepository;
import com.xuanyue.exp.exp.repository.ExpMaterialRepository;
import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.exp.service.ExpMaterialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExpMaterialServiceImpl implements ExpMaterialService {

    private final ExpMaterialRepository repository;
    private final ExpMaterialPicRepository picRepository;
    private final MaterialMsgRepository materialMsgRepository;
    private final MaterialPicRepository materialPicRepository;
    private final MinioStorageService minioStorageService;

    public ExpMaterialServiceImpl(ExpMaterialRepository repository,
                                  ExpMaterialPicRepository picRepository,
                                  MaterialMsgRepository materialMsgRepository,
                                  MaterialPicRepository materialPicRepository,
                                  MinioStorageService minioStorageService) {
        this.repository = repository;
        this.picRepository = picRepository;
        this.materialMsgRepository = materialMsgRepository;
        this.materialPicRepository = materialPicRepository;
        this.minioStorageService = minioStorageService;
    }

    @Override
    public List<?> listByExpId(String expId) {
        List lst = repository.findByExpIdOrderBySortOrderAsc(expId);
        for (Object item : lst) {
            ExpMaterial material = (ExpMaterial) item;
            material.setMainPicPreviewUrl(minioStorageService.buildPreviewUrl(material.getMainPicUrl()));
        }
        return lst;
    }

    @Override
    @Transactional
    public void saveBatch(String expId, List<Map<String, Object>> materials) {
        repository.deleteByExpId(expId);
        for (int i = 0; i < materials.size(); i++) {
            saveMaterial(expId, materials.get(i), i + 1);
        }
    }

    @Override
    @Transactional
    public String saveOne(String expId, Map<String, Object> material) {
        Integer sortOrder = parseOrder(material.get("sortOrder"), 1);
        return saveMaterial(expId, material, sortOrder);
    }

    @Override
    @Transactional
    public void updateOne(String expMaterialId, Map<String, Object> material) {
        ExpMaterial entity = repository.findById(expMaterialId).orElseThrow(() -> new RuntimeException("记录不存在"));
        String materialNum = asString(material.get("materialNum"));
        if (StringUtils.hasText(materialNum)) {
            entity.setMaterialNum(materialNum);
        }
        String materialName = asString(material.get("materialName"));
        if (StringUtils.hasText(materialName)) {
            entity.setMaterialName(materialName);
        }
        String materialPropId = asString(material.get("materialPropId"));
        if (StringUtils.hasText(materialPropId)) {
            entity.setMaterialPropId(materialPropId);
        }
        String materialTypeId = asString(material.get("materialTypeId"));
        if (StringUtils.hasText(materialTypeId)) {
            entity.setMaterialTypeId(materialTypeId);
        }
        String mainPicUrl = asString(material.get("mainPicUrl"));
        if (StringUtils.hasText(mainPicUrl)) {
            entity.setMainPicUrl(mainPicUrl);
        }
        String expPurpose = asString(material.get("expPurpose"));
        if (StringUtils.hasText(expPurpose)) {
            entity.setExpPurpose(expPurpose);
        }
        String securityComments = asString(material.get("securityComments"));
        if (StringUtils.hasText(securityComments)) {
            entity.setSecurityComments(securityComments);
        }
        String additionalComments = asString(material.get("additionalComments"));
        if (StringUtils.hasText(additionalComments)) {
            entity.setAdditionalComments(additionalComments);
        }
        repository.save(entity);
    }

    @Override
    public List<?> listPicsByExpMaterialId(String expMaterialId) {
        List lst = picRepository.findByExpMaterialIdOrderBySortOrderAsc(expMaterialId);
        for (Object item : lst) {
            ExpMaterialPic pic = (ExpMaterialPic) item;
            pic.setMaterialPreviewUrl(minioStorageService.buildPreviewUrl(pic.getMaterialUrl()));
        }
        return lst;
    }

    @Override
    @Transactional
    public void delete(String expMaterialId) {
        picRepository.deleteByExpMaterialId(expMaterialId);
        repository.deleteById(expMaterialId);
    }

    private String saveMaterial(String expId, Map<String, Object> material, Integer sortOrder) {
        String materialId = asString(material.get("materialId"));
        MaterialMsg source = materialMsgRepository.findById(materialId).orElse(null);
        ExpMaterial entity = new ExpMaterial();
        entity.setExpMaterialId(UUID.randomUUID().toString().replace("-", ""));
        entity.setExpId(expId);
        entity.setMaterialId(materialId);
        entity.setSortOrder(sortOrder);
        entity.setMaterialName(asString(material.get("materialName")));
        entity.setMaterialPropId(asString(material.get("materialPropId")));
        entity.setMaterialTypeId(asString(material.get("materialTypeId")));
        entity.setMaterialNum(asString(material.get("materialNum")));
        entity.setMainPicUrl(asString(material.get("mainPicUrl")));
        entity.setExpPurpose(asString(material.get("expPurpose")));
        entity.setSecurityComments(asString(material.get("securityComments")));
        entity.setAdditionalComments(asString(material.get("additionalComments")));
        if (source != null) {
            entity.setMaterialName(defaultIfBlank(entity.getMaterialName(), source.getMaterialName()));
            entity.setMaterialPropId(defaultIfBlank(entity.getMaterialPropId(), source.getMaterialPropId()));
            entity.setMaterialTypeId(defaultIfBlank(entity.getMaterialTypeId(), source.getMaterialTypeId()));
            entity.setMaterialNum(defaultIfBlank(entity.getMaterialNum(), source.getMaterialNum()));
            entity.setMainPicUrl(defaultIfBlank(entity.getMainPicUrl(), source.getMainPicUrl()));
            entity.setExpPurpose(defaultIfBlank(entity.getExpPurpose(), source.getExpPurpose()));
            entity.setSecurityComments(defaultIfBlank(entity.getSecurityComments(), source.getSecurityComments()));
            entity.setAdditionalComments(defaultIfBlank(entity.getAdditionalComments(), source.getAdditionalComments()));
        }
        repository.save(entity);

        List<MaterialPic> sourcePics = materialPicRepository.findByMaterialIdOrderBySortOrderAscCreateTimeAsc(materialId);
        List<ExpMaterialPic> pics = new ArrayList<>();
        for (int i = 0; i < sourcePics.size(); i++) {
            MaterialPic pic = sourcePics.get(i);
            ExpMaterialPic expPic = new ExpMaterialPic();
            expPic.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            expPic.setExpMaterialId(entity.getExpMaterialId());
            expPic.setMaterialId(materialId);
            expPic.setMaterialUrl(pic.getMaterialUrl());
            expPic.setSortOrder(pic.getSortOrder() != null ? pic.getSortOrder() : i + 1);
            pics.add(expPic);
        }
        picRepository.saveAll(pics);
        return entity.getExpMaterialId();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Integer parseOrder(Object value, Integer defaultValue) {
        String text = asString(value);
        if (!StringUtils.hasText(text)) return defaultValue;
        return Integer.valueOf(text);
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}
