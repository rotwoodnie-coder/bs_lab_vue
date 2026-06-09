package com.xuanyue.exp.exp.service.impl;

import com.xuanyue.exp.data.entity.DataSchoolGrade;
import com.xuanyue.exp.data.repository.DataSchoolGradeRepository;
import com.xuanyue.exp.exp.entity.ExpGrade;
import com.xuanyue.exp.exp.repository.ExpGradeRepository;
import com.xuanyue.exp.exp.service.ExpGradeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ExpGradeServiceImpl implements ExpGradeService {

    private final ExpGradeRepository repository;
    private final DataSchoolGradeRepository dataSchoolGradeRepository;

    public ExpGradeServiceImpl(ExpGradeRepository repository, DataSchoolGradeRepository dataSchoolGradeRepository) {
        this.repository = repository;
        this.dataSchoolGradeRepository = dataSchoolGradeRepository;
    }

    @Override
    @Transactional
    public void saveGrades(String expId, List<String> gradeIds) {
        repository.deleteByExpId(expId);
        if (gradeIds == null || gradeIds.isEmpty()) {
            return;
        }
        List<ExpGrade> entities = new ArrayList<ExpGrade>();
        int index = 1;
        for (String gradeId : gradeIds) {
            if (!StringUtils.hasText(gradeId)) continue;
            ExpGrade grade = new ExpGrade();
            grade.setSeqId(UUID.randomUUID().toString().replace("-", ""));
            grade.setExpId(expId);
            grade.setGradeId(gradeId.trim());
            grade.setSortOrder(index++);
            entities.add(grade);
        }
        repository.saveAll(entities);
    }

    @Override
    public List<String> listGradeIds(String expId) {
        List<ExpGrade> grades = repository.findByExpIdOrderBySortOrderAsc(expId);
        List<String> result = new ArrayList<String>();
        for (ExpGrade grade : grades) {
            result.add(grade.getGradeId());
        }
        return result;
    }

    @Override
    public List<String> listGradeNames(String expId) {
        List<ExpGrade> grades = repository.findByExpIdOrderBySortOrderAsc(expId);
        Set<String> gradeIds = new LinkedHashSet<String>();
        for (ExpGrade grade : grades) {
            if (grade != null && StringUtils.hasText(grade.getGradeId())) {
                gradeIds.add(grade.getGradeId().trim());
            }
        }
        List<DataSchoolGrade> gradeEntities = dataSchoolGradeRepository.findByGradeIdIn(gradeIds);
        List<String> result = new ArrayList<String>();
        for (ExpGrade grade : grades) {
            if (grade == null || !StringUtils.hasText(grade.getGradeId())) continue;
            String gradeId = grade.getGradeId().trim();
            String gradeName = null;
            for (DataSchoolGrade entity : gradeEntities) {
                if (entity != null && gradeId.equals(String.valueOf(entity.getGradeId()))) {
                    gradeName = entity.getGradeName();
                    break;
                }
            }
            if (StringUtils.hasText(gradeName)) {
                result.add(gradeName);
            }
        }
        return result;
    }

    @Override
    public String listGradeNamesString(String expId) {
        String strName = "";
        List<ExpGrade> grades = repository.findByExpIdOrderBySortOrderAsc(expId);
        Set<String> gradeIds = new LinkedHashSet<String>();
        for (ExpGrade grade : grades) {
            if (grade != null && StringUtils.hasText(grade.getGradeId())) {
                gradeIds.add(grade.getGradeId().trim());
            }
        }
        List<DataSchoolGrade> gradeEntities = dataSchoolGradeRepository.findByGradeIdIn(gradeIds);
        List<String> result = new ArrayList<String>();
        for (ExpGrade grade : grades) {
            if (grade == null || !StringUtils.hasText(grade.getGradeId())) continue;
            String gradeId = grade.getGradeId().trim();
            for (DataSchoolGrade entity : gradeEntities) {
                if (entity != null && gradeId.equals(String.valueOf(entity.getGradeId()))) {
                    strName += entity.getGradeName() + "、";
                    break;
                }
            }
        }
        if(strName.length() > 0){
            strName = strName.substring(0, strName.length() - 1);
        }
        return strName;
    }
}
