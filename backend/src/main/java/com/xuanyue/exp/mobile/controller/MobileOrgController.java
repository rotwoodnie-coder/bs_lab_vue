package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.mobile.dto.OrgOptionDto;
import com.xuanyue.exp.mobile.dto.StudentSearchItemDto;
import com.xuanyue.exp.mobile.service.MobileOrgBindService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/org")
public class MobileOrgController {

    private final MobileOrgBindService orgBindService;

    public MobileOrgController(MobileOrgBindService orgBindService) {
        this.orgBindService = orgBindService;
    }

    @GetMapping("/schools")
    public ApiResponse<List<OrgOptionDto>> schools() {
        return ApiResponse.success(orgBindService.listSchools());
    }

    @GetMapping("/grades")
    public ApiResponse<List<OrgOptionDto>> grades(@RequestParam String schoolOrgId) {
        try {
            return ApiResponse.success(orgBindService.listGrades(schoolOrgId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/classes")
    public ApiResponse<List<OrgOptionDto>> classes(@RequestParam String gradeOrgId) {
        try {
            return ApiResponse.success(orgBindService.listClasses(gradeOrgId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/students/search")
    public ApiResponse<List<StudentSearchItemDto>> searchStudents(
            @RequestParam String classOrgId,
            @RequestParam String name,
            @RequestParam(required = false) String studentNo) {
        try {
            return ApiResponse.success(orgBindService.searchStudents(classOrgId, name, studentNo));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
