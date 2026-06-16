package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.exp.dto.ExpSimulatorListItem;
import com.xuanyue.exp.exp.dto.ExpSimulatorPageQuery;
import com.xuanyue.exp.mobile.service.MobileSimulatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/simulators")
public class MobileSimulatorController {

    private final MobileSimulatorService simulatorService;

    public MobileSimulatorController(MobileSimulatorService simulatorService) {
        this.simulatorService = simulatorService;
    }

    @GetMapping
    public ApiResponse<PageResult<ExpSimulatorListItem>> page(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "gradeKey", defaultValue = "all") String gradeKey,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        ExpSimulatorPageQuery query = new ExpSimulatorPageQuery();
        query.setKeyword(keyword);
        query.setStatus(status);
        query.setGradeKey(gradeKey);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return ApiResponse.success(simulatorService.page(query));
    }

    @GetMapping("/{simulatorId}")
    public ApiResponse<ExpSimulatorListItem> get(@PathVariable String simulatorId) {
        return ApiResponse.success(simulatorService.get(simulatorId));
    }
}
