package com.xuanyue.exp.exp.service;

import java.util.List;
import java.util.Map;

public interface ExpVideoService {

    List<?> listByExpId(String expId);

    void saveBatch(String expId, List<Map<String, Object>> videos);

    void saveOne(String expId, Map<String, Object> video);

    void delete(String seqId);
}
