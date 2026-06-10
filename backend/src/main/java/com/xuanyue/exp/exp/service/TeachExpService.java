package com.xuanyue.exp.exp.service;

import java.util.Map;

public interface TeachExpService {
    String createFromStandard(Map<String, Object> payload);
    Map<String, Object> findMyTeachBySectionId(String sectionId);
    void updateSimulatorId(String expId, String simulatorId);
}