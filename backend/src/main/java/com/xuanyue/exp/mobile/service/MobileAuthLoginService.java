package com.xuanyue.exp.mobile.service;



import com.xuanyue.exp.mobile.auth.MobileAuthTokenService;

import com.xuanyue.exp.mobile.dto.MobileLoginRequest;

import com.xuanyue.exp.mobile.dto.MobileSessionDto;

import com.xuanyue.exp.mobile.dto.MobileRefreshTokenRequest;

import com.xuanyue.exp.mobile.support.MobileParentAccessService;

import com.xuanyue.exp.system.dto.LoginResponse;

import com.xuanyue.exp.system.entity.SysLog;

import com.xuanyue.exp.system.entity.SysOrg;

import com.xuanyue.exp.system.entity.SysUser;

import com.xuanyue.exp.system.repository.SysOrgRepository;

import com.xuanyue.exp.system.repository.SysUserRepository;

import com.xuanyue.exp.system.service.SysLogService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;



import java.util.Date;

import java.util.Optional;



/**

 * 移动端登录：支持 login_name 或 user_phone。

 * 家长 {@code status=t} 或无已通过绑定时允许登录，但仅能查看审核状态页。

 */

@Service

public class MobileAuthLoginService {



    private static final String PARENT_ROLE = "Parent";

    private static final String GENERIC_AUTH_ERROR = "账号或密码错误";



    private final SysUserRepository sysUserRepository;

    private final SysOrgRepository sysOrgRepository;

    private final SysLogService sysLogService;

    private final MobileAuthTokenService tokenService;

    private final MobileParentAccessService parentAccessService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();



    public MobileAuthLoginService(SysUserRepository sysUserRepository,

                                  SysOrgRepository sysOrgRepository,

                                  SysLogService sysLogService,

                                  MobileAuthTokenService tokenService,

                                  MobileParentAccessService parentAccessService) {

        this.sysUserRepository = sysUserRepository;

        this.sysOrgRepository = sysOrgRepository;

        this.sysLogService = sysLogService;

        this.tokenService = tokenService;

        this.parentAccessService = parentAccessService;

    }



    @Transactional

    public LoginResponse login(MobileLoginRequest request) {

        if (request == null || !StringUtils.hasText(request.getUsername())) {

            throw new RuntimeException("请输入账号");

        }

        if (!StringUtils.hasText(request.getPassword())) {

            throw new RuntimeException("请输入密码");

        }



        String account = request.getUsername().trim();

        SysUser user = resolveUser(account)

                .orElseThrow(() -> new RuntimeException(GENERIC_AUTH_ERROR));



        assertCanLogin(user);



        if (user.getExpireDate() != null && user.getExpireDate().before(new Date())) {

            throw new RuntimeException("账号已过期，请联系管理员");

        }



        if (!passwordEncoder.matches(request.getPassword(), user.getLoginPwd())) {

            throw new RuntimeException(GENERIC_AUTH_ERROR);

        }



        user.setLastLoginTime(new Date());

        sysUserRepository.save(user);



        SysLog loginLog = new SysLog();

        loginLog.setUserId(user.getUserId());

        loginLog.setLogType("登录");

        loginLog.setLogDataType("Login");

        loginLog.setLogDataContent("移动端用户登录成功");

        sysLogService.save(loginLog);



        return buildLoginResponse(user, request.getDeviceId());

    }



    @Transactional

    public LoginResponse refresh(MobileRefreshTokenRequest request) {

        if (request == null || !StringUtils.hasText(request.getRefreshToken())) {

            throw new RuntimeException("缺少 refresh token");

        }

        Optional<String> userIdOpt = tokenService.validateRefreshToken(

                request.getRefreshToken(), request.getDeviceId());

        SysUser user = userIdOpt

                .flatMap(sysUserRepository::findById)

                .orElseThrow(() -> new RuntimeException("登录已过期，请重新登录"));



        assertCanLogin(user);

        if (user.getExpireDate() != null && user.getExpireDate().before(new Date())) {

            throw new RuntimeException("账号已过期，请联系管理员");

        }



        return buildLoginResponse(user, request.getDeviceId());

    }



    public MobileSessionDto getSession(String userId) {

        SysUser user = sysUserRepository.findById(userId)

                .orElseThrow(() -> new RuntimeException("用户不存在"));

        MobileSessionDto dto = new MobileSessionDto();

        dto.setUserId(user.getUserId());

        dto.setUsername(user.getUserName());

        dto.setLoginName(user.getLoginName());

        dto.setUserRoleId(user.getUserRoleId());

        dto.setStatus(user.getStatus());

        dto.setRootOrgId(user.getRootOrgId());

        if (StringUtils.hasText(user.getRootOrgId())) {

            SysOrg rootOrg = sysOrgRepository.findById(user.getRootOrgId()).orElse(null);

            dto.setRootOrgName(rootOrg == null ? null : rootOrg.getOrgName());

        }

        dto.setParentRestricted(parentAccessService.isParentRestricted(user.getUserId()));

        return dto;

    }



    public void logout(String refreshToken) {

        tokenService.revokeRefreshToken(refreshToken);

    }



    private LoginResponse buildLoginResponse(SysUser user, String deviceId) {

        String accessToken = tokenService.createAccessToken(user);

        String refreshToken = tokenService.issueRefreshToken(user, deviceId);



        String rootOrgId = user.getRootOrgId();

        String rootOrgName = null;

        if (StringUtils.hasText(rootOrgId)) {

            SysOrg rootOrg = sysOrgRepository.findById(rootOrgId).orElse(null);

            rootOrgName = rootOrg == null ? null : rootOrg.getOrgName();

        }



        LoginResponse response = new LoginResponse(

                accessToken,

                user.getUserId(),

                user.getUserName(),

                user.getLoginName());

        response.setRefreshToken(refreshToken);

        response.setExpiresIn(tokenService.getAccessTokenSeconds());

        response.setTokenType("Bearer");

        response.setRootOrgId(rootOrgId);

        response.setRootOrgName(rootOrgName);

        response.setUserRoleId(user.getUserRoleId());

        response.setStatus(user.getStatus());

        response.setParentRestricted(parentAccessService.isParentRestricted(user.getUserId()));

        return response;

    }



    private void assertCanLogin(SysUser user) {

        String status = user.getStatus();

        if ("y".equalsIgnoreCase(status)) {

            return;

        }

        if ("t".equalsIgnoreCase(status) && PARENT_ROLE.equals(user.getUserRoleId())) {

            return;

        }

        if ("t".equalsIgnoreCase(status)) {

            throw new RuntimeException("账号审核中，请耐心等待");

        }

        throw new RuntimeException("账号已停用或未通过审核");

    }



    private Optional<SysUser> resolveUser(String account) {

        Optional<SysUser> byLogin = sysUserRepository.findByLoginName(account);

        if (byLogin.isPresent()) {

            return byLogin;

        }

        return sysUserRepository.findByUserPhone(account);

    }

}

