package com.xuanyue.exp.exp.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.exp.dto.ExpSimulatorListItem;
import com.xuanyue.exp.exp.dto.ExpSimulatorPageQuery;
import com.xuanyue.exp.exp.dto.ExpSimulatorSaveRequest;
import com.xuanyue.exp.exp.dto.ExpSimulatorUpdateRequest;
import com.xuanyue.exp.exp.service.ExpSimulatorService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/exp/simulators")
public class ExpSimulatorController {

    private final ExpSimulatorService service;

    public ExpSimulatorController(ExpSimulatorService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<ExpSimulatorListItem>> page(@RequestParam(value = "keyword", required = false) String keyword,
                                                              @RequestParam(value = "status", required = false) String status,
                                                              @RequestParam(value = "subjectId", required = false) String subjectId,
                                                              @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        ExpSimulatorPageQuery query = new ExpSimulatorPageQuery();
        query.setKeyword(keyword);
        query.setStatus(status);
        query.setSubjectId(subjectId);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return ApiResponse.success(service.page(query));
    }

    @GetMapping("/{simulatorId}")
    public ApiResponse<ExpSimulatorListItem> get(@PathVariable String simulatorId) {
        return ApiResponse.success(service.get(simulatorId));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody ExpSimulatorSaveRequest request, HttpServletRequest httpServletRequest) {
        service.create(request, httpServletRequest.getHeader("X-User-Id"));
        return ApiResponse.success(null);
    }

    @PostMapping("/{simulatorId}")
    public ApiResponse<Void> update(@PathVariable String simulatorId, @RequestBody ExpSimulatorUpdateRequest request, HttpServletRequest httpServletRequest) {
        service.update(simulatorId, request, httpServletRequest.getHeader("X-User-Id"));
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{simulatorId}")
    public ApiResponse<Void> delete(@PathVariable String simulatorId) {
        service.delete(simulatorId);
        return ApiResponse.success(null);
    }
}
