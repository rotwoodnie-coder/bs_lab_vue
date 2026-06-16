package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.dto.MobileAccountSecurityDto;
import com.xuanyue.exp.mobile.dto.MobileUserPreferencesDto;
import com.xuanyue.exp.mobile.entity.MbParentChild;
import com.xuanyue.exp.mobile.repository.MbParentChildRepository;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.system.entity.SysMsg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysMsgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 移动端设置：公告已读走 sys_msg.read_tag + link_id（与 school_notice 发布逻辑一致）；
 * 用户偏好暂无独立表，进程内缓存（实库未建 mb_user_settings）。
 */
@Service
public class MobileSettingsService {

    /** 与 SchoolNoticeServiceImpl 发布公告时写入的 msg_type_id 一致 */
    private static final String MSG_TYPE_NOTICE = "Msg_Notice";

    private static final SimpleDateFormat LOGIN_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /** 无 mb_user_settings 表时的运行时偏好缓存 */
    private final Map<String, MobileUserPreferencesDto> preferenceCache = new ConcurrentHashMap<>();

    private final MbParentChildRepository parentChildRepository;
    private final SysUserRepository sysUserRepository;
    private final SysMsgRepository sysMsgRepository;
    private final MobileDingTalkService dingTalkService;

    public MobileSettingsService(MbParentChildRepository parentChildRepository,
                                 SysUserRepository sysUserRepository,
                                 SysMsgRepository sysMsgRepository,
                                 MobileDingTalkService dingTalkService) {
        this.parentChildRepository = parentChildRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysMsgRepository = sysMsgRepository;
        this.dingTalkService = dingTalkService;
    }

    public MobileUserPreferencesDto getPreferences(String userId) {
        String uid = resolveUserId(userId);
        if (!StringUtils.hasText(uid)) {
            return new MobileUserPreferencesDto();
        }
        return preferenceCache.getOrDefault(uid, new MobileUserPreferencesDto());
    }

    public MobileUserPreferencesDto savePreferences(String userId, MobileUserPreferencesDto preferences) {
        String uid = resolveUserId(userId);
        MobileUserPreferencesDto toSave = preferences != null ? preferences : new MobileUserPreferencesDto();
        if (StringUtils.hasText(uid)) {
            preferenceCache.put(uid, toSave);
        }
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
        if (!StringUtils.hasText(uid)) {
            return Collections.emptyList();
        }
        return sysMsgRepository.findByReceiverUserIdAndMsgTypeIdOrderBySendTimeDesc(uid, MSG_TYPE_NOTICE).stream()
                .filter(msg -> isRead(msg.getReadTag()))
                .map(SysMsg::getLinkId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean isNoticeRead(String userId, String noticeId) {
        if (!StringUtils.hasText(noticeId)) {
            return false;
        }
        String uid = resolveUserId(userId);
        if (!StringUtils.hasText(uid)) {
            return false;
        }
        String id = noticeId.trim();
        List<SysMsg> messages = sysMsgRepository.findByReceiverUserIdAndLinkIdOrderBySendTimeDesc(uid, id);
        if (messages.isEmpty()) {
            return false;
        }
        return messages.stream().anyMatch(msg -> isRead(msg.getReadTag()));
    }

    @Transactional
    public void markNoticeRead(String userId, String noticeId) {
        if (!StringUtils.hasText(noticeId)) {
            throw new IllegalArgumentException("公告 ID 不能为空");
        }
        String uid = resolveUserId(userId);
        if (!StringUtils.hasText(uid)) {
            throw new IllegalArgumentException("请先登录");
        }
        String id = noticeId.trim();
        Date now = new Date();
        List<SysMsg> messages = sysMsgRepository.findByReceiverUserIdAndLinkIdOrderBySendTimeDesc(uid, id);
        if (!messages.isEmpty()) {
            for (SysMsg msg : messages) {
                if (!isRead(msg.getReadTag())) {
                    msg.setReadTag("1");
                    msg.setReadTime(now);
                    sysMsgRepository.save(msg);
                }
            }
            return;
        }
        // 无广播消息时（如仅展示 school_notice 最新一条），写入已读回执到 sys_msg
        SysMsg receipt = new SysMsg();
        receipt.setMsgId(MobileIds.newId());
        receipt.setReceiverUserId(uid);
        receipt.setMsgTypeId(MSG_TYPE_NOTICE);
        receipt.setMsgContent("{\"noticeRead\":true}");
        receipt.setReadTag("1");
        receipt.setLinkId(id);
        receipt.setSendTime(now);
        receipt.setReadTime(now);
        sysMsgRepository.save(receipt);
    }

    private boolean isRead(String readTag) {
        return "1".equals(safe(readTag));
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

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
