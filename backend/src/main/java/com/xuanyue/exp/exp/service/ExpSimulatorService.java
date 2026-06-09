package com.xuanyue.exp.exp.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.exp.dto.ExpSimulatorListItem;
import com.xuanyue.exp.exp.dto.ExpSimulatorPageQuery;
import com.xuanyue.exp.exp.dto.ExpSimulatorSaveRequest;
import com.xuanyue.exp.exp.dto.ExpSimulatorUpdateRequest;

public interface ExpSimulatorService {

    PageResult<ExpSimulatorListItem> page(ExpSimulatorPageQuery query);

    ExpSimulatorListItem get(String simulatorId);

    void create(ExpSimulatorSaveRequest request, String userId);

    void update(String simulatorId, ExpSimulatorUpdateRequest request, String userId);

    void delete(String simulatorId);
}
