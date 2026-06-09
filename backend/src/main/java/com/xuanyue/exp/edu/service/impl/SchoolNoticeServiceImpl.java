package com.xuanyue.exp.edu.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.edu.entity.SchoolNotice;
import com.xuanyue.exp.edu.repository.SchoolNoticeRepository;
import com.xuanyue.exp.edu.service.SchoolNoticeService;
import com.xuanyue.exp.system.entity.SysMsg;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysMsgRepository;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SchoolNoticeServiceImpl implements SchoolNoticeService {

    private final SchoolNoticeRepository schoolNoticeRepository;
    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final SysMsgRepository sysMsgRepository;

    public SchoolNoticeServiceImpl(SchoolNoticeRepository schoolNoticeRepository,
                                   SysUserRepository sysUserRepository,
                                   SysOrgRepository sysOrgRepository,
                                   SysMsgRepository sysMsgRepository) {
        this.schoolNoticeRepository = schoolNoticeRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.sysMsgRepository = sysMsgRepository;
    }

    @Override
    public PageResult<?> list(String keyword, String status, String noticeOrgId, int pageNum, int pageSize) {
        String kw = asString(keyword);
        String st = asString(status);
        String orgId = asString(noticeOrgId);
        List<Map<String, Object>> records = schoolNoticeRepository.findAll(Sort.by(Sort.Direction.DESC, "updateTime")).stream()
                .filter(item -> !StringUtils.hasText(kw)
                        || containsIgnoreCase(item.getNoticeName(), kw)
                        || containsIgnoreCase(item.getNoticeContent(), kw))
                .filter(item -> !StringUtils.hasText(st) || st.equals(item.getStatus()))
                .filter(item -> !StringUtils.hasText(orgId) || orgId.equals(item.getNoticeOrgId()))
                .map(this::toView)
                .collect(Collectors.toList());
        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, records.size());
        int toIndex = Math.min(fromIndex + safePageSize, records.size());
        return new PageResult<>(records.size(), records.subList(fromIndex, toIndex));
    }

    @Override
    public Object get(String id) {
        return schoolNoticeRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
    }

    @Override
    public void create(Map<String, Object> payload, String currentUserId) {
        SchoolNotice item = new SchoolNotice();
        item.setNoticeId(UUID.randomUUID().toString().replace("-", ""));
        applyPayload(item, payload, currentUserId, true);
        schoolNoticeRepository.save(item);
    }

    @Override
    public void update(String id, Map<String, Object> payload, String currentUserId) {
        SchoolNotice item = schoolNoticeRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        applyPayload(item, payload, currentUserId, false);
        schoolNoticeRepository.save(item);
    }

    @Override
    public void confirm(String id, Map<String, Object> payload, String currentUserId) {
        updateStatus(id, payload, currentUserId, "y");
    }

    @Override
    public void publish(String id, Map<String, Object> payload, String currentUserId) {
        updateStatusPublish(id, payload, currentUserId, "r");
    }

    @Override
    public void voidNotice(String id, Map<String, Object> payload, String currentUserId) {
        updateStatus(id, payload, currentUserId, "n");
    }

    @Override
    public void delete(String id) {
        schoolNoticeRepository.deleteById(id);
    }

    private void applyPayload(SchoolNotice item, Map<String, Object> payload, String currentUserId, boolean isCreate) {
        item.setNoticeName(asString(payload.get("noticeName")));
        item.setNoticeContent(asString(payload.get("noticeContent")));
        item.setNoticeOrgId(asString(payload.get("noticeOrgId")));
        item.setStatus(defaultStatus(payload.get("status")));
        Date now = new Date();
        if (isCreate) {
            item.setCreateUserId(StringUtils.hasText(currentUserId) ? currentUserId : item.getCreateUserId());
            item.setCreateTime(now);
        }
        item.setUpdateUserId(StringUtils.hasText(currentUserId) ? currentUserId : item.getUpdateUserId());
        item.setUpdateTime(now);
        if ("r".equals(item.getStatus()) && item.getReleaseTime() == null) {
            item.setReleaseUserId(StringUtils.hasText(currentUserId) ? currentUserId : item.getReleaseUserId());
            item.setReleaseTime(now);
        }
    }

    private Map<String, Object> toView(SchoolNotice item) {
        Map<String, Object> view = new HashMap<String, Object>();
        view.put("noticeId", item.getNoticeId());
        view.put("noticeName", item.getNoticeName());
        view.put("noticeContent", item.getNoticeContent());
        view.put("noticeOrgId", item.getNoticeOrgId());
        view.put("noticeOrgName", resolveOrgName(item.getNoticeOrgId()));
        view.put("status", item.getStatus());
        view.put("createUserId", item.getCreateUserId());
        view.put("createUserName", resolveUserName(item.getCreateUserId()));
        view.put("createTime", item.getCreateTime());
        view.put("updateUserId", item.getUpdateUserId());
        view.put("updateUserName", resolveUserName(item.getUpdateUserId()));
        view.put("updateTime", item.getUpdateTime());
        view.put("releaseUserId", item.getReleaseUserId());
        view.put("releaseUserName", resolveUserName(item.getReleaseUserId()));
        view.put("releaseTime", item.getReleaseTime());
        return view;
    }

    private void updateStatus(String id, Map<String, Object> payload, String currentUserId, String status) {
        SchoolNotice item = schoolNoticeRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        item.setNoticeName(asString(payload.get("noticeName")));
        item.setNoticeContent(asString(payload.get("noticeContent")));
        item.setNoticeOrgId(asString(payload.get("noticeOrgId")));
        item.setStatus(status);
        item.setUpdateUserId(StringUtils.hasText(currentUserId) ? currentUserId : item.getUpdateUserId());
        item.setUpdateTime(new Date());
        if ("r".equals(status)) {
            item.setReleaseUserId(StringUtils.hasText(currentUserId) ? currentUserId : item.getReleaseUserId());
            item.setReleaseTime(new Date());
        }
        schoolNoticeRepository.save(item);
        if ("r".equals(status)) {
            insertNoticeMessages(item, currentUserId);
        }
    }

    private void insertNoticeMessages(SchoolNotice notice, String currentUserId) {
        String rootOrgId = notice.getNoticeOrgId();
        if (!StringUtils.hasText(rootOrgId)) {
            return;
        }
        Date now = new Date();
        List<SysUser> users = sysUserRepository.findByStatus("y").stream()
                .filter(user -> rootOrgId.equals(user.getRootOrgId()))
                .collect(Collectors.toList());
        if (users.isEmpty()) {
            return;
        }
        List<SysMsg> messages = users.stream().map(user -> {
            SysMsg msg = new SysMsg();
            msg.setMsgId(UUID.randomUUID().toString().replace("-", ""));
            msg.setReceiverUserId(user.getUserId());
            msg.setSenderUserId(currentUserId);
            msg.setMsgTypeId("Msg_Notice");
            msg.setMsgContent(notice.getNoticeContent());
            msg.setReadTag("0");
            msg.setSendTime(now);
            msg.setLinkId(notice.getNoticeId());
            return msg;
        }).collect(Collectors.toList());
        sysMsgRepository.saveAll(messages);
    }

    private void updateStatusPublish(String id, Map<String, Object> payload, String currentUserId, String status) {
        SchoolNotice item = schoolNoticeRepository.findById(id).orElseThrow(() -> new RuntimeException("记录不存在"));
        if(!"y".equals(item.getStatus())) {
            throw new RuntimeException("通知状态不是待发布");
        }
        item.setStatus(status);
        item.setReleaseUserId(StringUtils.hasText(currentUserId) ? currentUserId : item.getReleaseUserId());
        item.setReleaseTime(new Date());
        schoolNoticeRepository.save(item);
        insertNoticeMessages(item, currentUserId);
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String resolveUserName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        return sysUserRepository.findById(userId)
                .map(this::displayUserName)
                .orElse(userId);
    }

    private String resolveOrgName(String orgId) {
        if (!StringUtils.hasText(orgId)) {
            return null;
        }
        return sysOrgRepository.findById(orgId)
                .map(SysOrg::getOrgName)
                .filter(StringUtils::hasText)
                .orElse(orgId);
    }

    private String displayUserName(SysUser user) {
        String userName = user.getUserName();
        if (StringUtils.hasText(userName)) {
            return userName;
        }
        String nickname = user.getUserNickName();
        if (StringUtils.hasText(nickname)) {
            return nickname;
        }
        String loginName = user.getLoginName();
        if (StringUtils.hasText(loginName)) {
            return loginName;
        }
        return user.getUserId();
    }

    private String defaultStatus(Object value) {
        String status = asString(value);
        return StringUtils.hasText(status) ? status : "c";
    }
}
