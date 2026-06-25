package com.xuanyue.exp.system.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.system.dto.RoleListItem;
import com.xuanyue.exp.system.dto.RoleMenuAuthRequest;
import com.xuanyue.exp.system.dto.RoleMenuAuthResponse;
import com.xuanyue.exp.system.dto.RolePageQuery;
import com.xuanyue.exp.system.dto.RoleSaveRequest;
import com.xuanyue.exp.system.dto.RoleUpdateRequest;
import com.xuanyue.exp.system.service.RoleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sys/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<PageResult<RoleListItem>> page(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "status", required = false) String status,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        RolePageQuery query = new RolePageQuery();
        query.setKeyword(keyword);
        query.setStatus(status);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return ApiResponse.success(roleService.page(query));
    }

    @PostMapping
    public ApiResponse<Void> create(@Validated @RequestBody RoleSaveRequest request) {
        roleService.create(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/{roleId}")
    public ApiResponse<Void> update(@PathVariable String roleId, @Validated @RequestBody RoleUpdateRequest request) {
        roleService.update(roleId, request);
        return ApiResponse.success(null);
    }

    @PostMapping("/{roleId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable String roleId, @RequestParam String status) {
        roleService.updateStatus(roleId, status);
        return ApiResponse.success(null);
    }

    @GetMapping("/{roleId}/menus")
    public ApiResponse<RoleMenuAuthResponse> getRoleMenus(@PathVariable String roleId) {
        return ApiResponse.success(roleService.getRoleMenus(roleId));
    }

    @PostMapping("/{roleId}/menus")
    public ApiResponse<Void> saveRoleMenus(@PathVariable String roleId, @RequestBody RoleMenuAuthRequest request) {
        roleService.saveRoleMenus(roleId, request.getMenuIds());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{roleId}")
    public ApiResponse<Void> delete(@PathVariable String roleId) {
        roleService.delete(roleId);
        return ApiResponse.success(null);
    }
}
