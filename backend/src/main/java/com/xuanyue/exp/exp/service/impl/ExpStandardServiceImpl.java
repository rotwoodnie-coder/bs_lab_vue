package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.entity.ExpLog;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.exp.repository.ExpLogRepository;
import com.xuanyue.exp.exp.service.ExpGradeService;
import com.xuanyue.exp.edu.service.CoursebookService;
import com.xuanyue.exp.edu.service.CoursebookContentService;
import com.xuanyue.exp.edu.entity.DataCoursebook;
import com.xuanyue.exp.edu.entity.DataCoursebookContent;
import com.xuanyue.exp.exp.service.ExpStandardService;
import com.xuanyue.exp.data.entity.DataSchoolSubject;
import com.xuanyue.exp.data.entity.DataSchoolSemester;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import com.xuanyue.exp.data.repository.DataSchoolSubjectRepository;
import com.xuanyue.exp.data.repository.DataSchoolSemesterRepository;
import com.xuanyue.exp.data.repository.DataSchoolGradeRepository;
import com.xuanyue.exp.data.entity.DataSchoolGrade;
import com.xuanyue.exp.exp.repository.ExpSimulatorRepository;
import com.xuanyue.exp.exp.entity.ExpSimulator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.xuanyue.exp.common.storage.minio.MinioStorageService;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpStandardServiceImpl implements ExpStandardService {

    private final ExpMsgRepository repository;
    private final ExpLogRepository logRepository;
    private final SysUserRepository sysUserRepository;
    private final ExpGradeService expGradeService;
    private final ExpSimulatorRepository expSimulatorRepository;
    private final CoursebookService expCoursebookService;
    private final CoursebookContentService expCoursebookContentService;
    private final DataSchoolSubjectRepository expSubjectService;
    private final DataSchoolSemesterRepository expSemesterService;
    private final DataSchoolGradeRepository gradeRepository;
    private final MinioStorageService minioStorageService;

    public ExpStandardServiceImpl(ExpMsgRepository repository, SysUserRepository sysUserRepository, ExpGradeService expGradeService, ExpLogRepository logRepository
                  ,CoursebookService expCoursebookService, CoursebookContentService expCoursebookContentService
                  ,DataSchoolSubjectRepository expSubjectService, DataSchoolSemesterRepository expSemesterService
                  ,DataSchoolGradeRepository gradeRepository, MinioStorageService minioStorageService
                  ,ExpSimulatorRepository expSimulatorRepository) {
        this.repository = repository;
        this.sysUserRepository = sysUserRepository;
        this.expGradeService = expGradeService;
        this.logRepository = logRepository;
        this.expCoursebookService = expCoursebookService;
        this.expCoursebookContentService = expCoursebookContentService; 
        this.expSubjectService = expSubjectService;
        this.expSemesterService = expSemesterService;
        this.gradeRepository = gradeRepository;
        this.minioStorageService = minioStorageService;
        this.expSimulatorRepository = expSimulatorRepository;
    }

    @Override
    public List<Map<String, Object>> list(String keyword, String status, String subjectId, String schoolLevelId, String gradeId, String chooseType, String expType,String currentUserId,String notstatus) {
        return repository.findAll(spec(keyword, status, subjectId, schoolLevelId, gradeId, chooseType, expType,currentUserId,notstatus)).stream().map(this::toMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> pageStandard(int pageNum, int pageSize, String keyword, String status, String subjectId, String schoolLevelId, String gradeId, String chooseType, String expType,String currentUserId,String notstatus) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        List<Map<String, Object>> lstMap =  repository.findAll(spec(keyword, status, subjectId, schoolLevelId, gradeId, chooseType, expType,currentUserId,notstatus), pageable).getContent().stream().map(this::toMap).collect(Collectors.toList());
        for(Map<String, Object> map : lstMap){
            map.put("gradeNames", expGradeService.listGradeNamesString(map.get("expId").toString()));
            if(map.get("simulatorId")!=null){
            String simulatorId = asString(map.get("simulatorId"));
            if(simulatorId!=null){
                ExpSimulator oSimulator = expSimulatorRepository.findById(simulatorId).orElse(null);
                if(oSimulator!=null){
                    map.put("simulatorName", oSimulator.getSimulatorName());
                    map.put("simulatorUrl", oSimulator.getSimulatorUrl());
                    if(oSimulator.getSimulatorUrl()!=null && StringUtils.hasText(oSimulator.getSimulatorUrl()) && !oSimulator.getSimulatorUrl().startsWith("http")){
                        map.put("simulatorPreviewUrl", minioStorageService.buildPreviewUrl(oSimulator.getSimulatorUrl()));
                    }else{
                        map.put("simulatorPreviewUrl", oSimulator.getSimulatorUrl());
                    }
                }
            }
           }
        }
        return lstMap;
    }

    @Override
    public List<Map<String, Object>> pageTeach(int pageNum, int pageSize, String keyword, String status, String subjectId, String schoolLevelId, String gradeId, String chooseType, String expType,String currentUserId,String notstatus) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        List<Map<String, Object>> lstMap =  repository.findAll(spec(keyword, status, subjectId, schoolLevelId, gradeId, chooseType, expType,currentUserId,notstatus), pageable).getContent().stream().map(this::toMap).collect(Collectors.toList());
        for(Map<String, Object> map : lstMap){
           if(map.get("simulatorId")!=null){
            String simulatorId = asString(map.get("simulatorId"));
            if(simulatorId!=null){
                ExpSimulator oSimulator = expSimulatorRepository.findById(simulatorId).orElse(null);
                if(oSimulator!=null){
                    map.put("simulatorName", oSimulator.getSimulatorName());
                    map.put("simulatorUrl", oSimulator.getSimulatorUrl());
                    if(oSimulator.getSimulatorUrl()!=null && StringUtils.hasText(oSimulator.getSimulatorUrl()) && !oSimulator.getSimulatorUrl().startsWith("http")){
                        map.put("simulatorPreviewUrl", minioStorageService.buildPreviewUrl(oSimulator.getSimulatorUrl()));
                    }else{
                        map.put("simulatorPreviewUrl", oSimulator.getSimulatorUrl());
                    }
                }
            }
           }
        }
        return lstMap;
    }

    @Override
    public long count(String keyword, String status, String subjectId, String schoolLevelId, String gradeId, String chooseType, String expType,String currentUserId,String notstatus) {
        return repository.count(spec(keyword, status, subjectId, schoolLevelId, gradeId, chooseType, expType,currentUserId,notstatus));
    }

    @Override
    public Map<String, Object> get(String id) {
        Optional<ExpMsg> optional = repository.findById(id);
         Map<String, Object> mapDetail = optional.map(this::toMap).orElse(null);

        //获取标准实验名称
        if(mapDetail.get("linkExpId")!=null ){
            String linkExpId = asString(mapDetail.get("linkExpId"));
            if(linkExpId!=null){
                ExpMsg oStandardExp = repository.findById(linkExpId).orElse(null);
                if(oStandardExp!=null){
                    mapDetail.put("linkExpName", oStandardExp.getExpName());
                }
            }
        }
        return mapDetail;
    }

    @Override
    public Map<String, Object> getDetailView(String id) {
        Optional<ExpMsg> optional = repository.findById(id);
        Map mapDetail = optional.map(this::toMap).orElse(null);
        System.out.println("mapDetail: " );
        //获取标准实验名称
        if(mapDetail.get("linkExpId")!=null ){
            String linkExpId = asString(mapDetail.get("linkExpId"));
            if(linkExpId!=null){
                ExpMsg oStandardExp = repository.findById(linkExpId).orElse(null);
                if(oStandardExp!=null){
                    mapDetail.put("linkExpName", oStandardExp.getExpName());
                }
            }
        }

        System.out.println("mapDetail9: " );
        //获取年级信息
        if(mapDetail.get("gradeId")!=null ){
            String gradeId = asString(mapDetail.get("gradeId"));
            if(gradeId!=null){
                DataSchoolGrade oGrade = gradeRepository.findById(gradeId).orElse(null);
                if(oGrade!=null){
                    mapDetail.put("gradeName", oGrade.getGradeName());
                }
            }
        }
        System.out.println("mapDetail8: " );
        List<String> gradeNames = expGradeService.listGradeNames(id);
        String strGradeNames= "";
        if(gradeNames!=null && gradeNames.size()>0){
            strGradeNames = String.join(",", gradeNames);    
        }
        mapDetail.put("gradeNames", strGradeNames);

        System.out.println("mapDetail7: " );
        //获取学科信息
        if(mapDetail.get("subjectId")!=null ){
            String subjectId = asString(mapDetail.get("subjectId"));
            if(subjectId!=null){
                DataSchoolSubject oSubject = expSubjectService.findById(subjectId).orElse(null);
                if(oSubject!=null){
                    mapDetail.put("subjectName", oSubject.getSubjectName());
                }
            }   
        }

        System.out.println("mapDetail6: " );
        //semester_id
        if(mapDetail.get("semesterId")!=null ){
            String semesterId = asString(mapDetail.get("semesterId"));
            if(semesterId!=null){
                DataSchoolSemester oSemester = expSemesterService.findById(semesterId).orElse(null);
                if(oSemester!=null){
                    mapDetail.put("semesterName", oSemester.getSemesterName());
                }
            }   
        }

        System.out.println("mapDetail5: " );
        //coursebook_id
        if(mapDetail.get("coursebookId")!=null ){
            String coursebookId = asString(mapDetail.get("coursebookId"));
            if(coursebookId!=null){
                DataCoursebook oCoursebook = expCoursebookService.findById(coursebookId);
                if(oCoursebook!=null){
                    mapDetail.put("coursebookName", oCoursebook.getCoursebookName());
                }
            }   
        }

        System.out.println("mapDetail4: " );
        //unit_id
        if(mapDetail.get("unitId")!=null ){
            String unitId = asString(mapDetail.get("unitId"));
            if(unitId!=null){
                DataCoursebookContent oUnit = expCoursebookContentService.findById(unitId);
                if(oUnit!=null){
                    mapDetail.put("unitName", oUnit.getContentName());
                }
            }   
        }

        System.out.println("mapDetail3: " );
        //chapter_id
        if(mapDetail.get("chapterId")!=null ){
            String chapterId = asString(mapDetail.get("chapterId"));
            if(chapterId!=null){
                DataCoursebookContent oUnit = expCoursebookContentService.findById(chapterId);
                if(oUnit!=null){
                    mapDetail.put("chapterName", oUnit.getContentName());
                }
            }   
        }

        System.out.println("mapDetail2: " );
        //section_id
        /*
        if(mapDetail.get("sectionId")!=null ){
            String sectionId = asString(mapDetail.get("sectionId"));
            if(sectionId!=null){
                DataCoursebookContent oUnit = expCoursebookContentService.findById(sectionId);
                if(oUnit!=null){
                    mapDetail.put("sectionName", oUnit.getContentName());
                }
            }   
        }*/
        System.out.println("mapDetail1: " );
        return mapDetail;
    }

    @Override
    @Transactional
    public String create(Map<String, Object> payload) {
        ExpMsg entity = new ExpMsg();
        entity.setExpId(UUID.randomUUID().toString().replace("-", ""));
        copy(entity, payload);
        if (!StringUtils.hasText(entity.getStatus())) entity.setStatus("c");
        String currentUserId = getCurrentUserId();
        entity.setCreateUserId(currentUserId);
        entity.setUpdateUserId(currentUserId);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        repository.save(entity);
        expGradeService.saveGrades(entity.getExpId(), toStringList(payload.get("gradeIds"), payload.get("grade_ids")));
        
        //log
        ExpLog expLog = new ExpLog();
        expLog.setLogId(UUID.randomUUID().toString().replace("-", ""));
        expLog.setExpId(entity.getExpId());
        expLog.setLogType("create");
        expLog.setLogTypeName("创建");
        expLog.setLogTime(new Date());
        expLog.setLogUserId(currentUserId);
        expLog.setLogUserName(resolveUserName(currentUserId));
        logRepository.save(expLog);

        return entity.getExpId();
    }

    @Override
    @Transactional
    public void update(String id, Map<String, Object> payload) {
        ExpMsg entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("实验不存在"));
        String oldStatus = entity.getStatus();
        copy(entity, payload);
        entity.setUpdateUserId(getCurrentUserId());
        entity.setUpdateTime(new Date());
        repository.save(entity);
        if (payload.get("gradeIds") != null) {
            expGradeService.saveGrades(id, toStringList(payload.get("gradeIds"), payload.get("grade_ids")));
        }

        //log
        String newStatus = entity.getStatus();
        if(!oldStatus.equals(newStatus)){
            ExpLog logExp = new ExpLog();
            logExp.setLogId(UUID.randomUUID().toString().replace("-", ""));
            logExp.setExpId(entity.getExpId());
            if(newStatus.equals("c")){
                logExp.setLogType("draf");
                logExp.setLogTypeName("草稿");
            }else if(newStatus.equals("t")){
                logExp.setLogType("toConfirm");
                logExp.setLogTypeName("提交审核");
            }else if(newStatus.equals("y")){
                logExp.setLogType("pass");
                logExp.setLogTypeName("审核通过");
                logExp.setLogComments(entity.getConfirmComments());
            }else if(newStatus.equals("n")){
                logExp.setLogType("reject");
                logExp.setLogTypeName("审核不通过");
                logExp.setLogComments(entity.getConfirmComments());
            }
            logExp.setLogTime(new Date());
            logExp.setLogUserId(getCurrentUserId());
            logExp.setLogUserName(resolveUserName(getCurrentUserId()));
            logRepository.save(logExp);
        }
    }

    @Override
    @Transactional
    public void updateAudit(String id, Map<String, Object> payload) {
        ExpMsg entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("实验不存在"));
        String oldStatus = entity.getStatus();
        String newStatus = asString(payload.get("status"));

        //log
        if(!oldStatus.equals(newStatus)){
            entity.setStatus(newStatus);
            entity.setConfirmUserId(getCurrentUserId());
            entity.setConfirmTime(new Date());
            entity.setConfirmComments(asString(payload.get("confirmComments")));
            entity.setUpdateUserId(getCurrentUserId());
            entity.setUpdateTime(new Date());
            repository.save(entity); 

            ExpLog logExp = new ExpLog();
            logExp.setLogId(UUID.randomUUID().toString().replace("-", ""));
            logExp.setExpId(entity.getExpId());
            if(newStatus.equals("c")){
                logExp.setLogType("draf");
                logExp.setLogTypeName("草稿");
            }else if(newStatus.equals("t")){
                logExp.setLogType("toConfirm");
                logExp.setLogTypeName("提交审核");
            }else if(newStatus.equals("y")){
                logExp.setLogType("pass");
                logExp.setLogTypeName("审核通过");
                logExp.setLogComments(entity.getConfirmComments());
            }else if(newStatus.equals("n")){
                logExp.setLogType("reject");
                logExp.setLogTypeName("审核不通过");
                logExp.setLogComments(entity.getConfirmComments());
            }
            logExp.setLogTime(new Date());
            logExp.setLogUserId(getCurrentUserId());
            logExp.setLogUserName(resolveUserName(getCurrentUserId()));
            logRepository.save(logExp);
        }
    }

    @Override
    @Transactional
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public Map<String, Object> findLatestDraftByCurrentUser(String expType) {
        String currentUserId = getCurrentUserId();
        //"standard" "teach"
        return repository.findTopByCreateUserIdAndStatusAndExpTypeOrderByCreateTimeDesc(currentUserId, "c", expType)
                .map(this::toMap)
                .orElse(null);
    }

    private void copy(ExpMsg entity, Map<String, Object> payload) {
        entity.setExpName(asString(payload.get("expName"), payload.get("exp_name"), entity.getExpName()));
        entity.setChooseType(asString(payload.get("chooseType"), payload.get("choose_type"), entity.getChooseType()));
        entity.setSubjectId(asString(payload.get("subjectId"), payload.get("subject_id"), entity.getSubjectId()));
        entity.setSchoolLevelId(asString(payload.get("schoolLevelId"), payload.get("school_level_id"), entity.getSchoolLevelId()));
        entity.setGradeId(asString(payload.get("gradeId"), payload.get("grade_id"), entity.getGradeId()));
        entity.setSemesterId(asString(payload.get("semesterId"), payload.get("semester_id"), entity.getSemesterId()));
        entity.setDifficultyId(asString(payload.get("difficultyId"), payload.get("difficulty_id"), entity.getDifficultyId()));
        entity.setExpPrinciple(asString(payload.get("expPrinciple"), payload.get("exp_principle"), entity.getExpPrinciple()));
        entity.setExpCaution(asString(payload.get("expCaution"), payload.get("exp_caution"), entity.getExpCaution()));
        entity.setExpDanger(asString(payload.get("expDanger"), payload.get("exp_danger"), entity.getExpDanger()));
        entity.setClassHour(asBigDecimal(payload.get("classHour"), payload.get("class_hour"), entity.getClassHour()));
        entity.setSimulatorUrl(asString(payload.get("simulatorUrl"), payload.get("simulator_url"), entity.getSimulatorUrl()));
        entity.setSimulatorId(asString(payload.get("simulatorId"), payload.get("simulator_id"), entity.getSimulatorId()));
        entity.setCoursebookId(asString(payload.get("coursebookId"), payload.get("coursebook_id"), entity.getCoursebookId()));
        entity.setUnitId(asString(payload.get("unitId"), payload.get("unit_id"), entity.getUnitId()));
        entity.setChapterId(asString(payload.get("chapterId"), payload.get("chapter_id"), entity.getChapterId()));
        entity.setSectionId(asString(payload.get("sectionId"), payload.get("section_id"), entity.getSectionId()));
        entity.setLinkExpId(asString(payload.get("linkExpId"), payload.get("link_exp_id"), entity.getLinkExpId()));
        entity.setExpType(asString(payload.get("expType"), payload.get("exp_type"), entity.getExpType()));
        entity.setExpTaskType(asString(payload.get("expTaskType"), payload.get("exp_task_type"), entity.getExpTaskType()));
        entity.setStatus(asString(payload.get("status"), payload.get("status"), entity.getStatus()));
        entity.setCollectionNum(asInteger(entity.getCollectionNum(), new Integer(0)));
        entity.setEvaluateNum(asInteger(entity.getEvaluateNum(), new Integer(0)));
        entity.setLikeNum(asInteger(entity.getLikeNum(), new Integer(0)));
        entity.setNotlikeNum(asInteger(entity.getNotlikeNum(), new Integer(0)));
    }

    private Specification<ExpMsg> spec(String keyword, String status, String subjectId, String schoolLevelId, String gradeId, String chooseType, String expType,String currentUserId,String notstatus) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("expId"), like),
                        cb.like(root.get("expName"), like),
                        cb.like(root.get("expCaution"), like),
                        cb.like(root.get("expDanger"), like)
                ));
            }
            if (StringUtils.hasText(status)) predicate = cb.and(predicate, cb.equal(root.get("status"), status.trim()));
            if (StringUtils.hasText(notstatus)) predicate = cb.and(predicate, cb.notEqual(root.get("status"), notstatus.trim()));
            if (StringUtils.hasText(subjectId)) predicate = cb.and(predicate, cb.equal(root.get("subjectId"), subjectId.trim()));
            if (StringUtils.hasText(schoolLevelId)) predicate = cb.and(predicate, cb.equal(root.get("schoolLevelId"), schoolLevelId.trim()));
            if (StringUtils.hasText(gradeId)) predicate = cb.and(predicate, cb.equal(root.get("gradeId"), gradeId.trim()));
            if (StringUtils.hasText(chooseType)) predicate = cb.and(predicate, cb.equal(root.get("chooseType"), chooseType.trim()));    
            if (StringUtils.hasText(expType)) predicate = cb.and(predicate, cb.equal(root.get("expType"), expType.trim()));
            if (StringUtils.hasText(currentUserId)) predicate = cb.and(predicate, cb.equal(root.get("createUserId"), currentUserId.trim()));
            query.orderBy(cb.desc(root.get("createTime")));
            return predicate;
        };
    }

    private Map<String, Object> toMap(ExpMsg item) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("expId", item.getExpId());
        result.put("expName", item.getExpName());
        result.put("chooseType", item.getChooseType());
        result.put("subjectId", item.getSubjectId());
        result.put("schoolLevelId", item.getSchoolLevelId());
        result.put("gradeId", item.getGradeId());
        result.put("semesterId", item.getSemesterId());
        result.put("gradeIds", expGradeService.listGradeIds(item.getExpId()));
        result.put("gradeNames", expGradeService.listGradeNames(item.getExpId()));
        result.put("difficultyId", item.getDifficultyId());
        result.put("expPrinciple", item.getExpPrinciple());
        result.put("expCaution", item.getExpCaution());
        result.put("expDanger", item.getExpDanger());
        result.put("classHour", item.getClassHour());
        result.put("simulatorUrl", item.getSimulatorUrl());
        result.put("simulatorId", item.getSimulatorId());
        result.put("coursebookId", item.getCoursebookId());
        result.put("unitId", item.getUnitId());
        result.put("chapterId", item.getChapterId());
        result.put("sectionId", item.getSectionId());
        result.put("linkExpId", item.getLinkExpId());
        result.put("expType", item.getExpType());
        result.put("expTaskType", item.getExpTaskType());
        result.put("likeNum", item.getLikeNum());
        result.put("notlikeNum", item.getNotlikeNum());
        result.put("collectionNum", item.getCollectionNum());
        result.put("evaluateNum", item.getEvaluateNum());
        result.put("status", item.getStatus());
        result.put("createUserId", item.getCreateUserId());
        result.put("createUserName", resolveUserName(item.getCreateUserId()));
        result.put("createTime", item.getCreateTime());
        result.put("confirmUserId", item.getConfirmUserId());
        result.put("confirmTime", item.getConfirmTime());
        result.put("confirmComments", item.getConfirmComments());
        result.put("updateUserId", item.getUpdateUserId());
        result.put("updateTime", item.getUpdateTime());
        return result;
    }

    private String resolveUserName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "";
        }
        return sysUserRepository.findById(userId)
                .map(user -> StringUtils.hasText(user.getUserName()) ? user.getUserName() : user.getLoginName())
                .orElse(userId);
    }

    private String getCurrentUserId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            throw new IllegalStateException("无法获取当前登录用户");
        }
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        String userId = request.getHeader("X-User-Id");
        if (!StringUtils.hasText(userId)) {
            throw new IllegalStateException("未获取到登录用户信息");
        }
        return userId.trim();
    }

    private List<String> toStringList(Object... values) {
        for (Object value : values) {
            if (value == null) continue;
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                return list.stream().filter(item -> item != null).map(item -> String.valueOf(item).trim()).filter(StringUtils::hasText).collect(Collectors.toList());
            }
            String s = String.valueOf(value).trim();
            if (s.startsWith("[") && s.endsWith("]")) {
                String body = s.substring(1, s.length() - 1).trim();
                if (body.isEmpty()) return new java.util.ArrayList<String>();
                String[] parts = body.split(",");
                List<String> result = new java.util.ArrayList<String>();
                for (String part : parts) {
                    String item = part.trim();
                    if (!item.isEmpty()) result.add(item.replaceAll("^\"|\"$", ""));
                }
                return result;
            }
            if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                List<String> result = new java.util.ArrayList<String>();
                result.add(s);
                return result;
            }
        }
        return new java.util.ArrayList<String>();
    }

    private String asString(Object... values) {
        for (Object value : values) {
            if (value != null) {
                String s = String.valueOf(value).trim();
                if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) return s;
            }
        }
        return null;
    }

    private BigDecimal asBigDecimal(Object... values) {
        for (Object value : values) {
            if (value != null) {
                String s = String.valueOf(value).trim();
                if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                    return new BigDecimal(s);
                }
            }
        }
        return null;
    }

    private Integer asInteger(Object... values) {
        for (Object value : values) {
            if (value != null) {
                String s = String.valueOf(value).trim();
                if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                    return new Integer(s);
                }
            }
        }
        return null;
    }
}
