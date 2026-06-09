package com.xuanyue.exp.data.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.data.entity.MaterialMsg;
import com.xuanyue.exp.data.entity.MaterialPic;
import com.xuanyue.exp.data.entity.MaterialLog;
import com.xuanyue.exp.data.repository.MaterialMsgRepository;
import com.xuanyue.exp.data.repository.MaterialPicRepository;
import com.xuanyue.exp.data.repository.MaterialLogRepository;
import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.data.service.MaterialMsgService;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaterialMsgServiceImpl implements MaterialMsgService {

    private final MaterialMsgRepository materialMsgRepository;
    private final MaterialLogRepository materialLogRepository;
    private final MaterialPicRepository materialPicRepository;
    private final SysUserRepository sysUserRepository;
    private final MinioStorageService minioStorageService;

    public MaterialMsgServiceImpl(MaterialMsgRepository materialMsgRepository, MaterialPicRepository materialPicRepository, 
          SysUserRepository sysUserRepository,MaterialLogRepository materialLogRepository, MinioStorageService minioStorageService ) {
        this.materialMsgRepository = materialMsgRepository;
        this.materialPicRepository = materialPicRepository;
        this.sysUserRepository = sysUserRepository;
        this.materialLogRepository = materialLogRepository;
        this.minioStorageService = minioStorageService;
    }

    @Override
    public PageResult<?> list(String keyword, String status, String isPublic, boolean publicMode, String currentUserId, int pageNum, int pageSize) {
        String kw = asString(keyword);
        String st = asString(status);
        String pub = asString(isPublic);
        String ownerId = asString(currentUserId);
        List<Map<String, Object>> records = materialMsgRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime")).stream()
                .filter(item -> !StringUtils.hasText(kw)
                        || containsIgnoreCase(item.getMaterialName(), kw)
                        || containsIgnoreCase(item.getMaterialNum(), kw)
                        || containsIgnoreCase(item.getExpPurpose(), kw)
                        || containsIgnoreCase(item.getSecurityComments(), kw)
                        || containsIgnoreCase(item.getAdditionalComments(), kw))
                .filter(item -> !StringUtils.hasText(st) || st.equals(item.getStatus()))
                .filter(item -> !StringUtils.hasText(pub) || pub.equals(item.getIsPublic()))
                .filter(item -> publicMode || isPublicOrOwner(item.getIsPublic(), item.getCreateUserId(), ownerId))
                .map(this::toView)
                .collect(Collectors.toList());
        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, records.size());
        int toIndex = Math.min(fromIndex + safePageSize, records.size());
        PageResult oPager =  new PageResult<>(records.size(), records.subList(fromIndex, toIndex));
        buildPreviewUrlMap(oPager);
        return oPager;
    }

    @Override
    public PageResult<?> listAll(String keyword, String status, String isPublic, String currentUserId, int pageNum, int pageSize) {
        String kw = asString(keyword);
        String st = asString(status);
        String pub = asString(isPublic);
        String ownerId = asString(currentUserId);
        List<Map<String, Object>> records = materialMsgRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime")).stream()
                .filter(item -> !StringUtils.hasText(kw)
                        || containsIgnoreCase(item.getMaterialName(), kw)
                        || containsIgnoreCase(item.getMaterialNum(), kw)
                        || containsIgnoreCase(item.getExpPurpose(), kw)
                        || containsIgnoreCase(item.getSecurityComments(), kw)
                        || containsIgnoreCase(item.getAdditionalComments(), kw))
                .filter(item -> !StringUtils.hasText(ownerId) || ownerId.equals(item.getCreateUserId()))
                .filter(item -> !StringUtils.hasText(st) || st.equals(item.getStatus()))
                .filter(item -> !StringUtils.hasText(pub) || pub.equals(item.getIsPublic()))
                .map(this::toView)
                .collect(Collectors.toList());
        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, records.size());
        int toIndex = Math.min(fromIndex + safePageSize, records.size());
        PageResult oPager = new PageResult<>(records.size(), records.subList(fromIndex, toIndex));
        buildPreviewUrlMap(oPager);
        return oPager;
    }

    private void buildPreviewUrlMap(PageResult<?> opager) {
        for(Object item : opager.getRecords()) {
            try {
                Map<String, Object> oMap = (Map<String, Object>) item;
                if(oMap.containsKey("mainPicUrl")) {
                    oMap.put("mainPicPreviewUrl", minioStorageService.buildPreviewUrl(oMap.get("mainPicUrl").toString()));
                }   
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object get(String id) {
        MaterialMsg item = materialMsgRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        Map<String, Object> view = toView(item);
        view.put("pics", materialPicRepository.findByMaterialIdOrderBySortOrderAscCreateTimeAsc(id).stream().map(pic -> {
            Map<String, Object> picView = new HashMap<>();
            picView.put("seqId", pic.getSeqId());
            picView.put("materialId", pic.getMaterialId());
            picView.put("materialUrl", pic.getMaterialUrl());
            try{
                picView.put("materialPreviewUrl", minioStorageService.buildPreviewUrl(pic.getMaterialUrl()));
            }catch(Exception e){
                e.printStackTrace();
            }
            picView.put("sortOrder", pic.getSortOrder());
            return picView;
        }).collect(Collectors.toList()));
        return view;
    }

    @Override
    @Transactional
    public MaterialMsg create(Map<String, Object> payload, String currentUserId) {
        MaterialMsg item = new MaterialMsg();
        item.setMaterialId(UUID.randomUUID().toString().replace("-", ""));
        applyPayload(item, payload, currentUserId);
        Date now = new Date();
        item.setCreateTime(now);
        item.setUpdateTime(now);
        materialMsgRepository.save(item);
        savePics(item.getMaterialId(), payload.get("pics") != null ? payload.get("pics") : payload.get("materialPics"));
        
        //log
        MaterialLog log = new MaterialLog();
        log.setLogId(UUID.randomUUID().toString().replace("-", ""));
        log.setMaterialId(item.getMaterialId());
        log.setLogType("create");
        log.setLogTypeName("创建");
        log.setLogUserId(currentUserId);
        log.setLogUserName(sysUserRepository.findById(currentUserId).map(SysUser::getUserName).orElse(currentUserId));
        log.setLogTime(new Date());
        materialLogRepository.save(log);
        return item;
    }

    @Override
    @Transactional
    public void update(String id, Map<String, Object> payload, String currentUserId) {
        MaterialMsg item = materialMsgRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        String oldIsPublic = item.getIsPublic();
        applyPayload(item, payload, currentUserId);
        materialMsgRepository.save(item);
        materialPicRepository.deleteByMaterialId(id);
        savePics(id, payload.get("pics") != null ? payload.get("pics") : payload.get("materialPics"));

        //log
        String newIsPublic = item.getIsPublic();
        MaterialLog log = new MaterialLog();
        log.setLogId(UUID.randomUUID().toString().replace("-", ""));
        log.setMaterialId(item.getMaterialId());
        if(oldIsPublic.equals(newIsPublic)){
            log.setLogType("update");
            log.setLogTypeName("更新");
        }else{
            if(newIsPublic.equals("y")){
                log.setLogType("public");
                log.setLogTypeName("公开");
            }else{
                log.setLogType("toPublic");
                log.setLogTypeName("待公开");
            }
        }
        log.setLogUserId(currentUserId);
        log.setLogUserName(sysUserRepository.findById(currentUserId).map(SysUser::getUserName).orElse(currentUserId));
        log.setLogTime(new Date());
        materialLogRepository.save(log);
    }

    @Override
    @Transactional
    public void updatePublic(String id, Map<String, Object> payload, String currentUserId) {
        MaterialMsg item = materialMsgRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        String oldIsPublic = item.getIsPublic();
        item.setIsPublic(defaultPublic(payload.get("isPublic")));
        materialMsgRepository.save(item);
        //log
        String newIsPublic = item.getIsPublic();
        MaterialLog log = new MaterialLog();
        log.setLogId(UUID.randomUUID().toString().replace("-", ""));
        log.setMaterialId(item.getMaterialId());
        if(oldIsPublic.equals(newIsPublic)){
            log.setLogType("update");
            log.setLogTypeName("更新");
        }else{
            if(newIsPublic.equals("y")){
                log.setLogType("public");
                log.setLogTypeName("公开");
            }else{
                log.setLogType("toPublic");
                log.setLogTypeName("待公开");
            }
        }
        log.setLogUserId(currentUserId);
        log.setLogUserName(sysUserRepository.findById(currentUserId).map(SysUser::getUserName).orElse(currentUserId));
        log.setLogTime(new Date());
        materialLogRepository.save(log);
    }

    @Override
    @Transactional
    public void delete(String id) {
        materialPicRepository.deleteByMaterialId(id);
        materialMsgRepository.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> listPics(String materialId) {
        return materialPicRepository.findByMaterialIdOrderBySortOrderAscCreateTimeAsc(materialId).stream().map(pic -> {
            Map<String, Object> picView = new HashMap<>();
            picView.put("seqId", pic.getSeqId());
            picView.put("materialId", pic.getMaterialId());
            picView.put("materialUrl", pic.getMaterialUrl());
            try{
              picView.put("materialPreviewUrl", minioStorageService.buildPreviewUrl(pic.getMaterialUrl()));
            }catch(Exception e){
                e.printStackTrace();
            }
            picView.put("sortOrder", pic.getSortOrder());
            return picView;
        }).collect(Collectors.toList());
    }

    @Transactional
    private void savePics(String materialId, Object picsObj) {
        List<Map<String, Object>> pics = toListMap(picsObj);
        if (pics.isEmpty()) return;
        Date now = new Date();
        List<MaterialPic> entities = new ArrayList<>();
        for (int i = 0; i < pics.size(); i++) {
            Map<String, Object> pic = pics.get(i);
            String url = asString(pic.get("materialUrl"));
            if (!StringUtils.hasText(url)) {
                url = asString(pic.get("url"));
            }
            if (!StringUtils.hasText(url)) continue;
            MaterialPic entity = new MaterialPic();
            entity.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            entity.setMaterialId(materialId);
            entity.setMaterialUrl(url);
            entity.setSortOrder(asInteger(pic.get("sortOrder"), i + 1));
            entity.setCreateTime(now);
            entities.add(entity);
        }
        materialPicRepository.saveAll(entities);
    }

    private void applyPayload(MaterialMsg item, Map<String, Object> payload, String currentUserId) {
        item.setMaterialName(requireText(payload.get("materialName"), "材料名称不能为空"));
        item.setMaterialPropId(requireText(payload.get("materialPropId"), "材料属性不能为空"));
        item.setMaterialTypeId(requireText(payload.get("materialTypeId"), "材料分类不能为空"));
        item.setMaterialNum(requireText(payload.get("materialNum"), "建议用量不能为空"));
        item.setMainPicUrl(asString(payload.get("mainPicUrl")));
        item.setExpPurpose(requireText(payload.get("expPurpose"), "实验用途不能为空"));
        item.setSecurityComments(requireText(payload.get("securityComments"), "安全说明不能为空"));
        item.setAdditionalComments(asString(payload.get("additionalComments")));
        item.setStatus(defaultStatus(payload.get("status")));
        item.setIsPublic(defaultPublic(payload.get("isPublic")));
        item.setCreateUserId(StringUtils.hasText(currentUserId) ? currentUserId : item.getCreateUserId());
        item.setUpdateUserId(StringUtils.hasText(currentUserId) ? currentUserId : item.getUpdateUserId());
        item.setUpdateTime(new Date());
    }

    private Map<String, Object> toView(MaterialMsg item) {
        Map<String, Object> view = new HashMap<>();
        view.put("materialId", item.getMaterialId());
        view.put("materialName", item.getMaterialName());
        view.put("materialPropId", item.getMaterialPropId());
        view.put("materialTypeId", item.getMaterialTypeId());
        view.put("materialNum", item.getMaterialNum());
        view.put("mainPicUrl", item.getMainPicUrl());
        view.put("expPurpose", item.getExpPurpose());
        view.put("securityComments", item.getSecurityComments());
        view.put("additionalComments", item.getAdditionalComments());
        view.put("status", item.getStatus());
        view.put("isPublic", item.getIsPublic());
        view.put("createUserId", item.getCreateUserId());
        view.put("createUserName", sysUserRepository.findById(item.getCreateUserId()).map(SysUser::getUserName).orElse(item.getCreateUserId()));
        view.put("createTime", item.getCreateTime());
        view.put("updateUserId", item.getUpdateUserId());
        view.put("updateTime", item.getUpdateTime());
        return view;
    }

    private List<Map<String, Object>> toListMap(Object value) {
        if (!(value instanceof List)) return new ArrayList<>();
        List<?> list = (List<?>) value;
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map) {
                result.add((Map<String, Object>) item);
            }
        }
        return result;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Integer asInteger(Object value, Integer defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) return defaultValue;
        return Integer.valueOf(String.valueOf(value));
    }

    private String defaultStatus(Object value) {
        String status = asString(value);
        return StringUtils.hasText(status) ? status : "y";
    }

    private String defaultPublic(Object value) {
        String v = asString(value);
        return StringUtils.hasText(v) ? v : "n";
    }

    private boolean isPublicOrOwner(String isPublic, String createUserId, String currentUserId) {
        boolean publicFlag = "y".equalsIgnoreCase(isPublic) || "1".equals(String.valueOf(isPublic));
        boolean ownerFlag = StringUtils.hasText(currentUserId) && StringUtils.hasText(createUserId) && currentUserId.equals(createUserId);
        return publicFlag || ownerFlag;
    }

    private String requireText(Object value, String message) {
        String text = asString(value);
        if (!StringUtils.hasText(text)) {
            throw new RuntimeException(message);
        }
        return text;
    }
}
