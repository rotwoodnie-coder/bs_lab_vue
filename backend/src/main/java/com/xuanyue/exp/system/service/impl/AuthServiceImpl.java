package com.xuanyue.exp.system.service.impl;

import com.xuanyue.exp.system.dto.LoginRequest;
import com.xuanyue.exp.system.dto.LoginResponse;
import com.xuanyue.exp.system.entity.SysLog;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import com.xuanyue.exp.system.service.AuthService;
import com.xuanyue.exp.system.service.SysLogService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService {

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final SysLogService sysLogService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(SysUserRepository sysUserRepository, SysOrgRepository sysOrgRepository, SysLogService sysLogService) {
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.sysLogService = sysLogService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserRepository.findByLoginNameAndStatus(request.getUsername(), "y")
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!"y".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("账号已停用");
        }

        if (user.getExpireDate() != null && user.getExpireDate().before(new Date())) {
            throw new RuntimeException("账号已过期，请联系管理员");
        }

        if (!matchesLoginPassword(user, request.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        user.setLastLoginTime(new Date());
        sysUserRepository.save(user);

        SysLog loginLog = new SysLog();
        loginLog.setUserId(user.getUserId());
        loginLog.setLogType("登录");
        loginLog.setLogDataType("Login");
        loginLog.setLogDataContent("用户登录成功");
        sysLogService.save(loginLog);

        String rootOrgId = user.getRootOrgId();
        String rootOrgName = null;
        if (StringUtils.hasText(rootOrgId)) {
            SysOrg rootOrg = sysOrgRepository.findById(rootOrgId).orElse(null);
            rootOrgName = rootOrg == null ? null : rootOrg.getOrgName();
        }

        String token = "mock-token-" + user.getUserId();
        LoginResponse response = new LoginResponse(token, user.getUserId(), user.getUserName(), user.getLoginName());
        response.setRootOrgId(rootOrgId);
        response.setRootOrgName(rootOrgName);
        response.setUserRoleId(user.getUserRoleId());
        return response;
    }

    private boolean matchesLoginPassword(SysUser user, String rawPassword) {
        if (user == null || !StringUtils.hasText(rawPassword) || !StringUtils.hasText(user.getLoginPwd())) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, user.getLoginPwd());
    }
}
