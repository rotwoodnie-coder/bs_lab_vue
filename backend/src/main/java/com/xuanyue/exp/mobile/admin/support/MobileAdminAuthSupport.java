package com.xuanyue.exp.mobile.admin.support;

import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class MobileAdminAuthSupport {

    private static final String SYS_ADMIN_ROLE = "Sys_Admin";
    private static final Set<String> ADMIN_ROLES = new HashSet<String>(Arrays.asList(
            SYS_ADMIN_ROLE, "School_Admin"));
    private static final Set<String> BADGE_MANAGER_ROLES = new HashSet<String>(Arrays.asList(
            SYS_ADMIN_ROLE, "School_Admin", "Researcher", "Teacher"));

    private final SysUserRepository sysUserRepository;

    public MobileAdminAuthSupport(SysUserRepository sysUserRepository) {
        this.sysUserRepository = sysUserRepository;
    }

    public SysUser requireAdminUser() {
        SysUser user = getCurrentUser();
        if (!ADMIN_ROLES.contains(user.getUserRoleId())) {
            throw new RuntimeException("无权限访问移动端管理功能");
        }
        return user;
    }

    public SysUser requireSysAdmin() {
        SysUser user = requireAdminUser();
        if (!SYS_ADMIN_ROLE.equals(user.getUserRoleId())) {
            throw new RuntimeException("仅系统管理员可执行此操作");
        }
        return user;
    }

    public SysUser requireBadgeManager() {
        SysUser user = getCurrentUser();
        if (!BADGE_MANAGER_ROLES.contains(user.getUserRoleId())) {
            throw new RuntimeException("无权限管理勋章规则");
        }
        return user;
    }

    public boolean isSysAdmin(SysUser user) {
        return user != null && SYS_ADMIN_ROLE.equals(user.getUserRoleId());
    }

    public void ensureTargetRootOrg(SysUser operator, String targetRootOrgId) {
        if (isSysAdmin(operator)) {
            return;
        }
        if (!StringUtils.hasText(operator.getRootOrgId())
                || !operator.getRootOrgId().equals(targetRootOrgId)) {
            throw new RuntimeException("无权限操作该学校下的数据");
        }
    }

    public boolean canSeeRootOrg(SysUser operator, String targetRootOrgId) {
        if (isSysAdmin(operator)) {
            return true;
        }
        return StringUtils.hasText(operator.getRootOrgId())
                && operator.getRootOrgId().equals(targetRootOrgId);
    }

    private SysUser getCurrentUser() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            throw new RuntimeException("无法获取当前登录用户");
        }
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        String userId = request.getHeader("X-User-Id");
        if (!StringUtils.hasText(userId)) {
            throw new RuntimeException("未获取到登录用户信息");
        }
        return sysUserRepository.findById(userId.trim())
                .orElseThrow(() -> new RuntimeException("登录用户不存在"));
    }
}
