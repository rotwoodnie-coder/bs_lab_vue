package com.xuanyue.exp.system.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.system.dto.UserListItem;
import com.xuanyue.exp.system.dto.UserPageQuery;
import com.xuanyue.exp.system.dto.ChangePasswordRequest;
import com.xuanyue.exp.system.dto.UserRoleAuthResponse;
import com.xuanyue.exp.system.dto.UserSaveRequest;
import com.xuanyue.exp.system.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {

    PageResult<UserListItem> page(UserPageQuery query);

    void create(UserSaveRequest request);

    void update(String userId, UserUpdateRequest request);

    void delete(String userId);

    void updateStatus(String userId, String status);

    void changePassword(String userId, ChangePasswordRequest request);

    UserRoleAuthResponse getUserRoles(String userId);

    void saveUserRoles(String userId, List<String> roleIds);
}
