package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.mobile.entity.MbPointsLedger;
import com.xuanyue.exp.mobile.repository.MbPointsLedgerRepository;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class MobilePointsService {

    public static final int WORK_SUBMIT_POINTS = 5;
    public static final int TASK_COMPLETE_POINTS = 10;
    public static final int WORK_FEATURED_POINTS = 20;

    private final SysUserRepository sysUserRepository;
    private final MbPointsLedgerRepository ledgerRepository;

    public MobilePointsService(SysUserRepository sysUserRepository,
                               MbPointsLedgerRepository ledgerRepository) {
        this.sysUserRepository = sysUserRepository;
        this.ledgerRepository = ledgerRepository;
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
