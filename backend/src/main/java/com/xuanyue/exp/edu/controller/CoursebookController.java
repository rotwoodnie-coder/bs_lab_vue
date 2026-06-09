package com.xuanyue.exp.edu.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.edu.service.CoursebookService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/edu/coursebooks")
public class CoursebookController {

    private final CoursebookService coursebookService;

    public CoursebookController(CoursebookService coursebookService) {
        this.coursebookService = coursebookService;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "editionId", required = false) String editionId,
                               @RequestParam(value = "subjectId", required = false) String subjectId,
                               @RequestParam(value = "gradeId", required = false) String gradeId,
                               @RequestParam(value = "paged", defaultValue = "false") boolean paged) {
        if (paged) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("records", coursebookService.page(pageNum, pageSize, keyword, editionId, subjectId, gradeId));
            result.put("total", coursebookService.count(keyword, editionId, subjectId, gradeId));
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return ApiResponse.success(result);
        }
        return ApiResponse.success(coursebookService.list(keyword, editionId, subjectId, gradeId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable String id) {
        return ApiResponse.success(coursebookService.get(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> payload) {
        coursebookService.create(payload);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        coursebookService.update(id, payload);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        coursebookService.delete(id);
        return ApiResponse.success(null);
    }

}
