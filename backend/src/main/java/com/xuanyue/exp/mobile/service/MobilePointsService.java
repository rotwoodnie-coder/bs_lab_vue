package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.MobilePointsLedgerItemDto;
import com.xuanyue.exp.mobile.entity.MbPointsLedger;
import com.xuanyue.exp.mobile.repository.MbPointsLedgerRepository;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MobilePointsService {

    public static final int WORK_SUBMIT_POINTS = 5;
    public static final int TASK_COMPLETE_POINTS = 10;
    public static final int WORK_FEATURED_POINTS = 20;

    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final SysUserRepository sysUserRepository;
    private final MbPointsLedgerRepository ledgerRepository;

    public MobilePointsService(SysUserRepository sysUserRepository,
                               MbPointsLedgerRepository ledgerRepository) {
        this.sysUserRepository = sysUserRepository;
        this.ledgerRepository = ledgerRepository;
    }

    /** 积分明细（流水）分页列表 */
    @Transactional(readOnly = true)
    public PageResult<MobilePointsLedgerItemDto> listLedger(String userId, int page, int size) {
        if (!StringUtils.hasText(userId)) {
            return new PageResult<>(0, new ArrayList<>());
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize);
        Page<MbPointsLedger> result = ledgerRepository
                .findByUserIdOrderByCreateTimeDesc(userId.trim(), pageable);
        List<MobilePointsLedgerItemDto> items = result.getContent().stream()
                .map(this::toLedgerItem)
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotalElements(), items);
    }

    private MobilePointsLedgerItemDto toLedgerItem(MbPointsLedger ledger) {
        MobilePointsLedgerItemDto dto = new MobilePointsLedgerItemDto();
        dto.setId(ledger.getLedgerId());
        dto.setDelta(ledger.getDelta() != null ? ledger.getDelta() : 0);
        dto.setBalanceAfter(ledger.getBalanceAfter());
        dto.setSourceType(ledger.getSourceType());
        dto.setSourceTypeLabel(resolveSourceLabel(ledger.getSourceType()));
        dto.setRemark(ledger.getRemark());
        if (ledger.getCreateTime() != null) {
            dto.setTime(TIME_FMT.format(ledger.getCreateTime()));
        }
        return dto;
    }

    private String resolveSourceLabel(String sourceType) {
        String t = StringUtils.hasText(sourceType) ? sourceType.trim() : "";
        switch (t) {
            case "student_work": return "提交作品";
            case "reviewed": return "作品审核通过";
            case "quiz": return "每日答题";
            case "badge": return "获得勋章";
            case "task": return "完成任务";
            case "manual": return "系统调整";
            default: return "积分变动";
        }
    }

    @Transactional
    public boolean credit(String userId, int delta, String sourceType, String sourceId, String remark) {
        if (!StringUtils.hasText(userId) || delta == 0) {
            return false;
        }
        String uid = userId.trim();
        String type = StringUtils.hasText(sourceType) ? sourceType.trim() : "manual";
        String sid = StringUtils.hasText(sourceId) ? sourceId.trim() : "";

        if (StringUtils.hasText(sid)
                && ledgerRepository.existsByUserIdAndSourceTypeAndSourceId(uid, type, sid)) {
            return false;
        }

        SysUser user = sysUserRepository.findById(uid).orElse(null);
        if (user == null) {
            return false;
        }

        int current = user.getPerScore() != null ? user.getPerScore() : 0;
        int next = Math.max(0, current + delta);
        user.setPerScore(next);
        sysUserRepository.save(user);

        MbPointsLedger ledger = new MbPointsLedger();
        ledger.setLedgerId(MobileIds.newId("pl"));
        ledger.setUserId(uid);
        ledger.setDelta(delta);
        ledger.setBalanceAfter(next);
        ledger.setSourceType(type);
        ledger.setSourceId(StringUtils.hasText(sid) ? sid : null);
        ledger.setRemark(remark);
        ledger.setCreateTime(new Date());
        ledgerRepository.save(ledger);
        return true;
    }
}
