package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.data.service.DataDictService;
import com.xuanyue.exp.mobile.support.MobileMediaUrlSupport;
import com.xuanyue.exp.mobile.support.MobileMinioKeySupport;
import com.xuanyue.exp.mobile.dto.MobileProfileDto;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 移动端个人中心 Service
 */
@Service
public class MobileProfileService {

    private static final Logger log = LoggerFactory.getLogger(MobileProfileService.class);
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final DataDictService dataDictService;
    private final MinioStorageService minioStorageService;

    public MobileProfileService(SysUserRepository sysUserRepository,
                                SysOrgRepository sysOrgRepository,
                                DataDictService dataDictService,
                                MinioStorageService minioStorageService) {
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.dataDictService = dataDictService;
        this.minioStorageService = minioStorageService;
    }

    /**
     * 获取当前用户完整信息
     */
    public MobileProfileDto getProfile(String userId) {
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        MobileProfileDto dto = new MobileProfileDto();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setLoginName(user.getLoginName());
        dto.setUserNickName(user.getUserNickName());
        dto.setUserPhone(user.getUserPhone());
        dto.setUserEmail(user.getUserEmail());
        if (StringUtils.hasText(user.getUserLogo())) {
            try {
                dto.setUserLogo(MobileMediaUrlSupport.resolve(minioStorageService, user.getUserLogo()));
            } catch (Exception e) {
                log.warn("解析头像 URL 失败 userId={}", user.getUserId(), e);
                dto.setUserLogo(user.getUserLogo());
            }
        }
        dto.setRootOrgId(user.getRootOrgId());
        dto.setUserRoleId(user.getUserRoleId());
        dto.setPerResume(user.getPerResume());
        dto.setPerScore(user.getPerScore());
        dto.setPrefTitleId(user.getPrefTitleId());
        dto.setCreateTime(user.getCreateTime());
        dto.setLastLoginTime(user.getLastLoginTime());

        // 解析学校全名
        if (StringUtils.hasText(user.getRootOrgId())) {
            resolveOrgName(dto, user.getRootOrgId());
        }

        // 解析班级/年级（userOrgName）
        if (StringUtils.hasText(user.getUserOrgId())) {
            try {
                SysOrg userOrg = sysOrgRepository.findById(user.getUserOrgId()).orElse(null);
                if (userOrg != null && StringUtils.hasText(userOrg.getOrgName())) {
                    dto.setUserOrgName(userOrg.getOrgName());
                }
            } catch (Exception e) {
                log.warn("查询用户组织失败 orgId={}", user.getUserOrgId(), e);
            }
        }

        if (dto.getRootOrgName() == null && StringUtils.hasText(user.getUserOrgId())) {
            resolveOrgName(dto, user.getUserOrgId());
        }

        // 解析职称
        if (StringUtils.hasText(user.getPrefTitleId())) {
            try {
                Object obj = dataDictService.get("data_pref_title", user.getPrefTitleId());
                if (obj instanceof Map) {
                    Object name = ((Map<?, ?>) obj).get("title_name");
                    if (name != null) {
                        dto.setPrefTitleName(name.toString());
                    }
                }
            } catch (Exception e) {
                log.warn("查询职称失败 prefTitleId={}", user.getPrefTitleId(), e);
            }
        }

        return dto;
    }

    private void resolveOrgName(MobileProfileDto dto, String orgId) {
        try {
            SysOrg org = sysOrgRepository.findById(orgId).orElse(null);
            if (org != null && StringUtils.hasText(org.getOrgName())) {
                dto.setRootOrgName(org.getOrgName());
            }
        } catch (Exception e) {
            log.warn("查询组织失败 orgId={}", orgId, e);
        }
    }

    /**
     * 更新个人资料
     */
    @Transactional
    public void updateProfile(String userId, Map<String, Object> payload) {
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (payload.containsKey("userName")) {
            user.setUserName((String) payload.get("userName"));
        }
        if (payload.containsKey("userNickName")) {
            user.setUserNickName((String) payload.get("userNickName"));
        }
        if (payload.containsKey("userPhone")) {
            user.setUserPhone((String) payload.get("userPhone"));
        }
        if (payload.containsKey("userEmail")) {
            user.setUserEmail((String) payload.get("userEmail"));
        }
        if (payload.containsKey("perResume")) {
            user.setPerResume((String) payload.get("perResume"));
        }
        if (payload.containsKey("userLogo")) {
            String logo = payload.get("userLogo") != null ? payload.get("userLogo").toString().trim() : null;
            if (!StringUtils.hasText(logo)) {
                user.setUserLogo(null);
            } else {
                user.setUserLogo(MobileMinioKeySupport.requireStorageKey(minioStorageService, logo));
            }
        }

        sysUserRepository.save(user);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(String userId, String oldPassword, String newPassword) {
        if (!StringUtils.hasText(oldPassword)) {
            throw new RuntimeException("原密码不能为空");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new RuntimeException("新密码不能为空");
        }

        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getLoginPwd())) {
            throw new RuntimeException("原密码不正确");
        }

        user.setLoginPwd(passwordEncoder.encode(newPassword));
        sysUserRepository.save(user);
    }
}
