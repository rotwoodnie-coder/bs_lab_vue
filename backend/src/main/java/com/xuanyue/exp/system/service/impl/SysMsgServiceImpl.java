package com.xuanyue.exp.system.service.impl;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.system.entity.SysMsg;
import com.xuanyue.exp.system.repository.SysMsgRepository;
import com.xuanyue.exp.system.service.SysMsgService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysMsgServiceImpl implements SysMsgService {

    private final SysMsgRepository repository;

    public SysMsgServiceImpl(SysMsgRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<?> page(int pageNum, int pageSize, String msgTypeId, String readTag, String currentUserId) {
        String uid = asString(currentUserId);
        String typeId = asString(msgTypeId);
        String status = asString(readTag);
        List<Map<String, Object>> rows = (StringUtils.hasText(typeId)
                ? repository.findByReceiverUserIdAndMsgTypeIdOrderBySendTimeDesc(uid, typeId)
                : repository.findByReceiverUserIdOrderBySendTimeDesc(uid))
                .stream()
                .filter(item -> !StringUtils.hasText(status) || status.equals(asString(item.getReadTag())))
                .map(this::toView)
                .collect(Collectors.toList());
        int safePageSize = Math.max(pageSize, 1);
        int safePageNum = Math.max(pageNum, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, rows.size());
        int toIndex = Math.min(fromIndex + safePageSize, rows.size());
        return new PageResult<>(rows.size(), rows.subList(fromIndex, toIndex));
    }

    @Override
    public long unreadCount(String currentUserId) {
        String uid = asString(currentUserId);
        if (!StringUtils.hasText(uid)) return 0L;
        return repository.findByReceiverUserIdOrderBySendTimeDesc(uid).stream()
                .filter(item -> !"1".equals(asString(item.getReadTag())))
                .count();
    }

    @Override
    @Transactional
    public void markRead(String msgId, String currentUserId) {
        SysMsg msg = repository.findById(msgId).orElseThrow(() -> new RuntimeException("消息不存在"));
        if (!StringUtils.hasText(currentUserId) || !currentUserId.equals(msg.getReceiverUserId())) {
            throw new RuntimeException("只能操作自己的消息");
        }
        if (!"1".equals(msg.getReadTag())) {
            msg.setReadTag("1");
            msg.setReadTime(new Date());
            repository.save(msg);
        }
    }

    @Override
    @Transactional
    public int markAllRead(String currentUserId) {
        String uid = asString(currentUserId);
        if (!StringUtils.hasText(uid)) return 0;
        List<SysMsg> unread = repository.findByReceiverUserIdOrderBySendTimeDesc(uid).stream()
                .filter(item -> !"1".equals(asString(item.getReadTag())))
                .collect(Collectors.toList());
        if (unread.isEmpty()) return 0;
        Date now = new Date();
        for (SysMsg msg : unread) {
            msg.setReadTag("1");
            msg.setReadTime(now);
        }
        repository.saveAll(unread);
        return unread.size();
    }

    private Map<String, Object> toView(SysMsg item) {
        Map<String, Object> view = new HashMap<>();
        view.put("msgId", item.getMsgId());
        view.put("receiverUserId", item.getReceiverUserId());
        view.put("senderUserId", item.getSenderUserId());
        view.put("msgTypeId", item.getMsgTypeId());
        view.put("msgContent", item.getMsgContent());
        view.put("readTag", item.getReadTag());
        view.put("sendTime", item.getSendTime());
        view.put("readTime", item.getReadTime());
        return view;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}
