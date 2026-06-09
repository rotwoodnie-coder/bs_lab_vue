package com.xuanyue.exp.system.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.system.dto.ChangePasswordRequest;
import com.xuanyue.exp.system.dto.UserListItem;
import com.xuanyue.exp.system.dto.UserPageQuery;
import com.xuanyue.exp.system.dto.UserRoleAuthRequest;
import com.xuanyue.exp.system.dto.UserRoleAuthResponse;
import com.xuanyue.exp.system.dto.UserSaveRequest;
import com.xuanyue.exp.system.dto.UserUpdateRequest;
import com.xuanyue.exp.system.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sys/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResult<UserListItem>> page(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "roleId", required = false) String roleId,
                                                      @RequestParam(value = "status", required = false) String status,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        UserPageQuery query = new UserPageQuery();
        query.setKeyword(keyword);
        query.setRoleId(roleId);
        query.setStatus(status);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return ApiResponse.success(userService.page(query));
    }

    @PostMapping
    public ApiResponse<Void> create(@Validated @RequestBody UserSaveRequest request) {
        userService.create(request);
        return ApiResponse.success(null);
    }

    @PutMapping("/{userId}")
    public ApiResponse<Void> update(@PathVariable String userId, @Validated @RequestBody UserUpdateRequest request) {
        userService.update(userId, request);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{userId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable String userId, @RequestParam String status) {
        userService.updateStatus(userId, status);
        return ApiResponse.success(null);
    }

    @PostMapping("/{userId}/password")
    public ApiResponse<Void> changePassword(@PathVariable String userId, @Validated @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/{userId}/roles")
    public ApiResponse<UserRoleAuthResponse> getUserRoles(@PathVariable String userId) {
        return ApiResponse.success(userService.getUserRoles(userId));
    }

    @PostMapping("/{userId}/roles")
    public ApiResponse<Void> saveUserRoles(@PathVariable String userId, @RequestBody UserRoleAuthRequest request) {
        userService.saveUserRoles(userId, request.getRoleIds());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> delete(@PathVariable String userId) {
        userService.delete(userId);
        return ApiResponse.success(null);
    }
}
