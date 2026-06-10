package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.exp.service.TeachExpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.UUID;
import java.util.Date;
import java.util.Map;
import java.util.List;
import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.entity.ExpLog;
import com.xuanyue.exp.exp.entity.ExpReference;
import com.xuanyue.exp.exp.entity.ExpResult;
import com.xuanyue.exp.exp.entity.ExpScientist;
import com.xuanyue.exp.exp.entity.ExpStep;
import com.xuanyue.exp.exp.entity.ExpVideo;
import com.xuanyue.exp.exp.entity.ExpGrade;
import com.xuanyue.exp.exp.entity.ExpMaterial;
import com.xuanyue.exp.exp.entity.ExpMaterialPic;
import com.xuanyue.exp.exp.repository.ExpLogRepository;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.exp.repository.ExpGradeRepository;
import com.xuanyue.exp.exp.repository.ExpMaterialRepository;
import com.xuanyue.exp.exp.repository.ExpMaterialPicRepository;
import com.xuanyue.exp.exp.repository.ExpReferenceRepository;
import com.xuanyue.exp.exp.repository.ExpResultRepository;
import com.xuanyue.exp.exp.repository.ExpScientistRepository;
import com.xuanyue.exp.exp.repository.ExpStepRepository;
import com.xuanyue.exp.exp.repository.ExpVideoRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import com.xuanyue.exp.system.entity.SysUser;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;

@Service
public class TeachExpServiceImpl implements TeachExpService {
    private final ExpMsgRepository repository;
    private final ExpLogRepository logRepository;
    private final SysUserRepository sysUserRepository;
    private final ExpGradeRepository expGradeRepository;
    private final ExpMaterialRepository expMaterialRepository;
    private final ExpMaterialPicRepository expMaterialPicRepository;
    private final ExpReferenceRepository expReferenceRepository;
    private final ExpResultRepository expResultRepository;
    private final ExpScientistRepository expScientistRepository;
    private final ExpStepRepository expStepRepository;
    private final ExpVideoRepository expVideoRepository;

    public TeachExpServiceImpl(ExpMsgRepository repository,ExpLogRepository logRepository, SysUserRepository sysUserRepository
      ,ExpGradeRepository expGradeRepository, ExpMaterialRepository expMaterialRepository, ExpMaterialPicRepository expMaterialPicRepository,
      ExpReferenceRepository expReferenceRepository, ExpResultRepository expResultRepository, ExpScientistRepository expScientistRepository,
       ExpStepRepository expStepRepository, ExpVideoRepository expVideoRepository) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.sysUserRepository = sysUserRepository;
        this.expGradeRepository = expGradeRepository;
        this.expMaterialRepository = expMaterialRepository;
        this.expMaterialPicRepository = expMaterialPicRepository;
        this.expReferenceRepository = expReferenceRepository;
        this.expResultRepository = expResultRepository;
        this.expScientistRepository = expScientistRepository;
        this.expStepRepository = expStepRepository;
        this.expVideoRepository = expVideoRepository;
    }

    @Override
    public Map<String, Object> findMyTeachBySectionId(String sectionId) {
        if (!StringUtils.hasText(sectionId)) {
            throw new IllegalArgumentException("目录ID不能为空");
        }
        String currentUserId = getCurrentUserId();
        List<ExpMsg> records = repository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("expType"), "teach"),
                cb.equal(root.get("createUserId"), currentUserId),
                cb.equal(root.get("sectionId"), sectionId)
        ));
        if (records == null || records.isEmpty()) {
            return null;
        }
        ExpMsg latest = records.stream().sorted((a, b) -> {
            Date at = a.getCreateTime();
            Date bt = b.getCreateTime();
            if (at == null && bt == null) return 0;
            if (at == null) return 1;
            if (bt == null) return -1;
            return bt.compareTo(at);
        }).findFirst().orElse(records.get(0));
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("expId", latest.getExpId());
        result.put("expName", latest.getExpName());
        result.put("chooseType", latest.getChooseType());
        result.put("status", latest.getStatus());
        result.put("sectionId", latest.getSectionId());
        result.put("expType", latest.getExpType());
        result.put("createUserId", latest.getCreateUserId());
        result.put("createTime", latest.getCreateTime());
        return result;
    }

    @Override
    @Transactional
    public void updateSimulatorId(String expId, String simulatorId) {
        if (!StringUtils.hasText(expId)) {
            throw new IllegalArgumentException("实验ID不能为空");
        }
        ExpMsg entity = repository.findById(expId).orElseThrow(() -> new IllegalArgumentException("实验不存在"));
        entity.setSimulatorId(StringUtils.hasText(simulatorId) ? simulatorId.trim() : null);
        entity.setUpdateUserId(getCurrentUserId());
        entity.setUpdateTime(new Date());
        repository.save(entity);
    }

    @Override
    @Transactional
    public String createFromStandard(Map<String, Object> payload) {
        String toExpId = (String) payload.get("toExpId");  
        ExpMsg expTo = null;
        if (StringUtils.isEmpty(toExpId)) {
            toExpId = UUID.randomUUID().toString().replace("-", "");
            expTo = new ExpMsg();
        }else{
            expTo = repository.findById(toExpId).orElse(null);
            if (expTo == null) {
                throw new IllegalArgumentException("实验不存在");
            }else{
                expTo = new ExpMsg();
            }
        }
        String standardId = (String) payload.get("fromExpId");
        ExpMsg expFrom = repository.findById(standardId).orElse(null);
        if (expFrom == null) {
            throw new IllegalArgumentException("实验不存在");
        }
        
        String currentUserId = getCurrentUserId();
        BeanUtils.copyProperties(expFrom, expTo);
        expTo.setExpId(toExpId);
        expTo.setExpType("teach");
        expTo.setStatus("c");
        expTo.setCreateUserId(currentUserId);
        expTo.setUpdateUserId(currentUserId);
        expTo.setCreateTime(new Date());
        expTo.setUpdateTime(new Date());
        expTo.setConfirmUserId(null);
        expTo.setConfirmTime(null);
        expTo.setConfirmComments(null);
        expTo.setLinkExpId(standardId);
        repository.save(expTo);

        //grades
        List<ExpGrade> grades = expGradeRepository.findByExpIdOrderBySortOrderAsc(expFrom.getExpId());
        for (ExpGrade grade : grades) { 
            ExpGrade expGrade = new ExpGrade();
            BeanUtils.copyProperties(grade, expGrade);
            expGrade.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            expGrade.setExpId(expTo.getExpId());
            expGradeRepository.save(expGrade);
        }

        //materials
        List<ExpMaterial> materials = expMaterialRepository.findByExpIdOrderBySortOrderAsc(expFrom.getExpId());
        for (ExpMaterial material : materials) { 
            ExpMaterial expMaterial = new ExpMaterial();
            BeanUtils.copyProperties(material, expMaterial);
            String tempId = expMaterial.getExpMaterialId();
            expMaterial.setExpMaterialId(UUID.randomUUID().toString().replace("-", ""));
            expMaterial.setExpId(expTo.getExpId());
            expMaterialRepository.save(expMaterial);

            //material pics
            List<ExpMaterialPic> materialPics = expMaterialPicRepository.findByExpMaterialIdOrderBySortOrderAsc(tempId);
            for (ExpMaterialPic materialPic : materialPics) { 
                ExpMaterialPic expMaterialPic = new ExpMaterialPic();
                BeanUtils.copyProperties(materialPic, expMaterialPic);
                expMaterialPic.setSeqId(UUID.randomUUID().toString().replace("-", ""));
                expMaterialPic.setExpMaterialId(expMaterialPic.getExpMaterialId());
                expMaterialPicRepository.save(expMaterialPic);
            }
        }

        //references
        List<ExpReference> references = expReferenceRepository.findByExpIdOrderBySortOrderAsc(expFrom.getExpId());
        for (ExpReference reference : references) { 
            ExpReference expReference = new ExpReference();
                BeanUtils.copyProperties(reference, expReference);
            expReference.setReferenceId(UUID.randomUUID().toString().replace("-", ""));
            expReference.setExpId(expTo.getExpId());
            expReferenceRepository.save(expReference);
        }

        //results
        List<ExpResult> results = expResultRepository.findByExpIdOrderBySortOrderAsc(expFrom.getExpId());
        for (ExpResult result : results) { 
            ExpResult expResult = new ExpResult();
                BeanUtils.copyProperties(result, expResult);
            expResult.setResultId(UUID.randomUUID().toString().replace("-", ""));
            expResult.setExpId(expTo.getExpId());
            expResultRepository.save(expResult);
        }
        //scientists
        List<ExpScientist> scientists = expScientistRepository.findByExpIdOrderBySortOrderAsc(expFrom.getExpId());
        for (ExpScientist scientist : scientists) { 
            ExpScientist expScientist = new ExpScientist();
                BeanUtils.copyProperties(scientist, expScientist);
            expScientist.setScientistId(UUID.randomUUID().toString().replace("-", ""));
            expScientist.setExpId(expTo.getExpId());
            expScientistRepository.save(expScientist);
        }
        //steps
        List<ExpStep> steps = expStepRepository.findByExpIdOrderBySortOrderAsc(expFrom.getExpId());
        for (ExpStep step : steps) { 
            ExpStep expStep = new ExpStep();
                BeanUtils.copyProperties(step, expStep);
            expStep.setStepId(UUID.randomUUID().toString().replace("-", ""));
            expStep.setExpId(expTo.getExpId());
            expStepRepository.save(expStep);
        }   
        //videos
        List<ExpVideo> videos = expVideoRepository.findByExpIdOrderBySortOrderAsc(expFrom.getExpId());
        for (ExpVideo video : videos) { 
            ExpVideo expVideo = new ExpVideo();
                BeanUtils.copyProperties(video, expVideo);
            expVideo.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            expVideo.setExpId(expTo.getExpId());
            expVideoRepository.save(expVideo);
        }

        
        //log
        ExpLog expLog = new ExpLog();
        expLog.setLogId(UUID.randomUUID().toString().replace("-", ""));
        expLog.setExpId(expTo.getExpId());
        expLog.setLogType("create");
        expLog.setLogTypeName("创建");
        expLog.setLogTime(new Date());
        expLog.setLogUserId(currentUserId);
        expLog.setLogUserName(resolveUserName(currentUserId));
        logRepository.save(expLog);

        return toExpId;
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

    private String resolveUserName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "";
        }
        return sysUserRepository.findById(userId)
                .map(user -> StringUtils.hasText(user.getUserName()) ? user.getUserName() : user.getLoginName())
                .orElse(userId);
    }
}