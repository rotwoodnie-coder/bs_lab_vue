package com.xuanyue.exp.mobile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanyue.exp.mobile.dto.MobileAccountSecurityDto;
import com.xuanyue.exp.mobile.dto.MobileUserPreferencesDto;
import com.xuanyue.exp.mobile.entity.MbNoticeRead;
import com.xuanyue.exp.mobile.entity.MbParentChild;
import com.xuanyue.exp.mobile.entity.MbUserSettings;
import com.xuanyue.exp.mobile.repository.MbNoticeReadRepository;
import com.xuanyue.exp.mobile.repository.MbParentChildRepository;
import com.xuanyue.exp.mobile.repository.MbUserSettingsRepository;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MobileSettingsService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SimpleDateFormat LOGIN_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final MbUserSettingsRepository userSettingsRepository;
    private final MbNoticeReadRepository noticeReadRepository;
    private final MbParentChildRepository parentChildRepository;
    private final SysUserRepository sysUserRepository;
    private final MobileDingTalkService dingTalkService;

    public MobileSettingsService(MbUserSettingsRepository userSettingsRepository,
                                 MbNoticeReadRepository noticeReadRepository,
                                 MbParentChildRepository parentChildRepository,
                                 SysUserRepository sysUserRepository,
                                 MobileDingTalkService dingTalkService) {
        this.userSettingsRepository = userSettingsRepository;
        this.noticeReadRepository = noticeReadRepository;
        this.parentChildRepository = parentChildRepository;
        this.sysUserRepository = sysUserRepository;
        this.dingTalkService = dingTalkService;
    }

    public MobileUserPreferencesDto getPreferences(String userId) {
        String uid = resolveUserId(userId);
        return userSettingsRepository.findById(uid)
                .map(this::parsePreferences)
                .orElseGet(MobileUserPreferencesDto::new);
    }

    @Transactional
    public MobileUserPreferencesDto savePreferences(String userId, MobileUserPreferencesDto preferences) {
        String uid = resolveUserId(userId);
        MobileUserPreferencesDto toSave = preferences != null ? preferences : new MobileUserPreferencesDto();
        MbUserSettings entity = userSettingsRepository.findById(uid).orElseGet(MbUserSettings::new);
        entity.setUserId(uid);
        try {
            entity.setSettingsJson(MAPPER.writeValueAsString(toSave));
        } catch (Exception e) {
            throw new IllegalStateException("保存设置失败");
        }
        entity.setUpdateTime(new Date());
        userSettingsRepository.save(entity);
        return toSave;
    }

    public MobileAccountSecurityDto getAccountSecurity(String userId) {
        String uid = resolveUserId(userId);
        SysUser user = sysUserRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        MobileAccountSecurityDto dto = new MobileAccountSecurityDto();
        dto.setPhoneBound(StringUtils.hasText(user.getUserPhone()));
        dto.setMaskedPhone(maskPhone(user.getUserPhone()));
        dto.setWechatBound(false);

        com.xuanyue.exp.mobile.dto.DingTalkBindStatusDto dingStatus = dingTalkService.getBindStatus(uid);
        dto.setDingTalkConfigured(dingStatus.isConfigured());
        dto.setDingTalkBound(dingStatus.isBound());
        dto.setDingTalkLabel(dingStatus.getLabel());

        String role = user.getUserRoleId() != null ? user.getUserRoleId().toLowerCase() : "student";
        if ("parent".equals(role)) {
            long approvedChildren = parentChildRepository.findByParentUserIdAndBindStatusOrderByIsDefaultDesc(uid, "approved").size();
            long pendingChildren = parentChildRepository.findByParentUserIdAndBindStatusOrderByIsDefaultDesc(uid, "pending").size();
            dto.setParentBindCount((int) approvedChildren);
            dto.setParentPendingCount((int) pendingChildren);
            dto.setParentBound(approvedChildren > 0);
            if (approvedChildren > 0) {
                dto.setParentBindLabel("已绑定 " + approvedChildren + " 个孩子");
            } else if (pendingChildren > 0) {
                dto.setParentBindLabel("审核中");
            } else {
                dto.setParentBindLabel("未绑定孩子");
            }
        } else if ("student".equals(role)) {
            long approvedParents = parentChildRepository.countByChildUserIdAndBindStatus(uid, "approved");
            long pendingParents = parentChildRepository.countByChildUserIdAndBindStatus(uid, "pending");
            dto.setParentBindCount((int) approvedParents);
            dto.setParentPendingCount((int) pendingParents);
            dto.setParentBound(approvedParents > 0);
            if (approvedParents > 0) {
                dto.setParentBindLabel("已绑定 " + approvedParents + " 位家长");
            } else if (pendingParents > 0) {
                dto.setParentBindLabel("审核中");
            } else {
                dto.setParentBindLabel("未绑定");
            }
        } else {
            dto.setParentBindCount(0);
            dto.setParentPendingCount(0);
            dto.setParentBound(false);
            dto.setParentBindLabel("");
        }

        dto.setLastLoginTime(user.getLastLoginTime());
        if (user.getLastLoginTime() != null) {
            dto.setLastLoginLabel(LOGIN_FMT.format(user.getLastLoginTime()));
        } else {
            dto.setLastLoginLabel("暂无记录");
        }
        dto.setCurrentDeviceLabel("本机 · 最近登录 " + dto.getLastLoginLabel());
        return dto;
    }

    public List<String> listReadNoticeIds(String userId) {
        String uid = resolveUserId(userId);
        return noticeReadRepository.findByUserIdOrderByReadTimeDesc(uid).stream()
                .map(MbNoticeRead::getNoticeId)
                .collect(Collectors.toList());
    }

    public boolean isNoticeRead(String userId, String noticeId) {
        if (!StringUtils.hasText(noticeId)) {
            return false;
        }
        String uid = resolveUserId(userId);
        return noticeReadRepository.existsByUserIdAndNoticeId(uid, noticeId.trim());
    }

    @Transactional
    public void markNoticeRead(String userId, String noticeId) {
        if (!StringUtils.hasText(noticeId)) {
            throw new IllegalArgumentException("公告 ID 不能为空");
        }
        String uid = resolveUserId(userId);
        String id = noticeId.trim();
        if (noticeReadRepository.existsByUserIdAndNoticeId(uid, id)) {
            return;
        }
        MbNoticeRead read = new MbNoticeRead();
        read.setUserId(uid);
        read.setNoticeId(id);
        read.setReadTime(new Date());
        noticeReadRepository.save(read);
    }

    private MobileUserPreferencesDto parsePreferences(MbUserSettings entity) {
        if (entity == null || !StringUtils.hasText(entity.getSettingsJson())) {
            return new MobileUserPreferencesDto();
        }
        try {
            return MAPPER.readValue(entity.getSettingsJson(), MobileUserPreferencesDto.class);
        } catch (Exception e) {
            return new MobileUserPreferencesDto();
        }
    }

    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return "";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String resolveUserId(String userId) {
        return StringUtils.hasText(userId) ? userId.trim() : userId;
    }
}
