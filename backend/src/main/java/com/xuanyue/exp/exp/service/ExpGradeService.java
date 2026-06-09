package com.xuanyue.exp.exp.service;

import java.util.List;

public interface ExpGradeService {

    void saveGrades(String expId, List<String> gradeIds);

    List<String> listGradeIds(String expId);

    List<String> listGradeNames(String expId);
    String listGradeNamesString(String expId);
}
