package com.xuanyue.exp.exp.service;

import java.util.List;
import java.util.Map;

public interface ExpQuestionSelectService {
    List<Map<String, Object>> list(String questionId);
    void saveBatch(String questionId, List<Map<String, Object>> selects);
    void delete(String selectId);
}
