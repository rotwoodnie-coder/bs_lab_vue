package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.mobile.entity.MbParentChild;
import com.xuanyue.exp.mobile.repository.MbParentChildRepository;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Component
public class MobileParentAccessService {

    private final MbParentChildRepository parentChildRepository;
    private final SysUserRepository sysUserRepository;

    public MobileParentAccessService(MbParentChildRepository parentChildRepository,
                                     SysUserRepository sysUserRepository) {
        this.parentChildRepository = parentChildRepository;
        this.sysUserRepository = sysUserRepository;
    }

    public boolean isParentUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        return sysUserRepository.findById(userId.trim())
                .map(u -> "Parent".equalsIgnoreCase(safe(u.getUserRoleId())))
                .orElse(false);
    }

    public void requireApprovedBind(String parentUserId, String childUserId) {
        if (!StringUtils.hasText(parentUserId) || !StringUtils.hasText(childUserId)) {
            throw new IllegalArgumentException("参数无效");
        }
        MbParentChild bind = parentChildRepository.findByParentUserIdAndChildUserId(parentUserId.trim(), childUserId.trim())
                .orElseThrow(() -> new IllegalArgumentException("未绑定该孩子"));
        if (!"approved".equalsIgnoreCase(safe(bind.getBindStatus()))) {
            throw new IllegalArgumentException("绑定审核中，暂不可查看");
        }
    }

    /** 家长代操作时解析目标学生 id；非家长则返回本人 id */
    public String resolveStudentScope(String userId, String childUserId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("请先登录");
        }
        if (isParentUser(userId)) {
            if (StringUtils.hasText(childUserId)) {
                requireApprovedBind(userId, childUserId);
                return childUserId.trim();
            }
            List<MbParentChild> binds = parentChildRepository
                    .findByParentUserIdAndBindStatusOrderByIsDefaultDesc(userId.trim(), "approved");
            if (binds.isEmpty()) {
                throw new IllegalArgumentException("请先绑定孩子");
            }
            return binds.get(0).getChildUserId();
        }
        return userId.trim();
    }

    public Optional<MbParentChild> findApprovedBind(String parentUserId, String childUserId) {
        if (!StringUtils.hasText(parentUserId) || !StringUtils.hasText(childUserId)) {
            return Optional.empty();
        }
        return parentChildRepository.findByParentUserIdAndChildUserId(parentUserId.trim(), childUserId.trim())
                .filter(b -> "approved".equalsIgnoreCase(safe(b.getBindStatus())));
    }

    /**
     * 尚无已通过的孩子绑定时，仅允许查看审核状态页。
     * 绑定审核通过后即可使用家长端（账号 status=t 注册待激活仍允许，仅 status=n 等停用态拦截）。
     */
    public boolean isParentRestricted(String userId) {
        if (!isParentUser(userId)) {
            return false;
        }
        SysUser user = sysUserRepository.findById(userId.trim()).orElse(null);
        if (user == null) {
            return true;
        }
        String status = safe(user.getStatus());
        if ("n".equalsIgnoreCase(status)) {
            return true;
        }
        List<MbParentChild> approved = parentChildRepository
                .findByParentUserIdAndBindStatusOrderByIsDefaultDesc(userId.trim(), "approved");
        return approved.isEmpty();
    }

    public void requireParentFeatureAccess(String userId) {
        if (isParentRestricted(userId)) {
            throw new IllegalArgumentException("绑定审核中，请等待学校通过后再使用家长端功能");
        }
    }

    private String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
