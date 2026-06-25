package com.xuanyue.exp.mobile.admin.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.admin.dto.ParentRegistrationAuditRequest;
import com.xuanyue.exp.mobile.admin.dto.ParentRegistrationListItem;
import com.xuanyue.exp.mobile.admin.support.MobileAdminAuthSupport;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MobileParentRegistrationAdminService {

    private static final String PARENT_ROLE = "Parent";
    private static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final SysUserRepository sysUserRepository;
    private final SysOrgRepository sysOrgRepository;
    private final MobileAdminAuthSupport authSupport;

    public MobileParentRegistrationAdminService(SysUserRepository sysUserRepository,
                                                SysOrgRepository sysOrgRepository,
                                                MobileAdminAuthSupport authSupport) {
        this.sysUserRepository = sysUserRepository;
        this.sysOrgRepository = sysOrgRepository;
        this.authSupport = authSupport;
    }

    /** 待审核家长注册数量（status=t），校管理员限本校 */
    public int countPending(String operatorUserId) {
        if (!StringUtils.hasText(operatorUserId)) {
            return 0;
        }
        SysUser operator = sysUserRepository.findById(operatorUserId.trim()).orElse(null);
        if (operator == null) {
            return 0;
        }
        String role = operator.getUserRoleId() != null ? operator.getUserRoleId().trim() : "";
        if (!"Sys_Admin".equals(role) && !"School_Admin".equals(role)) {
            return 0;
        }
        int count = 0;
        for (SysUser user : sysUserRepository.findAll()) {
            if (!PARENT_ROLE.equals(user.getUserRoleId())) {
                continue;
            }
            if (!"t".equals(user.getStatus())) {
                continue;
            }
            if (authSupport.canSeeRootOrg(operator, user.getRootOrgId())) {
                count++;
            }
        }
        return count;
    }

    public PageResult<ParentRegistrationListItem> page(String keyword, String status,
                                                       int pageNum, int pageSize) {
        SysUser operator = authSupport.requireAdminUser();
        Map<String, String> orgNameMap = buildOrgNameMap();

        List<SysUser> matched = new ArrayList<SysUser>();
        for (SysUser user : sysUserRepository.findAll()) {
            if (!PARENT_ROLE.equals(user.getUserRoleId())) {
                continue;
            }
            if (!authSupport.canSeeRootOrg(operator, user.getRootOrgId())) {
                continue;
            }
            if (StringUtils.hasText(status) && !status.equals(user.getStatus())) {
                continue;
            }
            if (StringUtils.hasText(keyword) && !matchesKeyword(user, keyword.trim())) {
                continue;
            }
            matched.add(user);
        }

        matched.sort(Comparator.comparing(SysUser::getCreateTime, Comparator.nullsLast(Date::compareTo)).reversed());

        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(matched.size(), from + pageSize);
        List<ParentRegistrationListItem> records = new ArrayList<ParentRegistrationListItem>();
        if (from < matched.size()) {
            for (SysUser user : matched.subList(from, to)) {
                records.add(toListItem(user, orgNameMap));
            }
        }
        return new PageResult<ParentRegistrationListItem>(matched.size(), records);
    }

    @Transactional
    public void audit(String userId, ParentRegistrationAuditRequest request) {
        SysUser operator = authSupport.requireAdminUser();
        if (request == null || !StringUtils.hasText(request.getAction())) {
            throw new RuntimeException("请指定审核操作");
        }

        SysUser target = sysUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!PARENT_ROLE.equals(target.getUserRoleId())) {
            throw new RuntimeException("该用户不是家长角色");
        }
        authSupport.ensureTargetRootOrg(operator, target.getRootOrgId());

        String action = request.getAction().trim().toLowerCase();
        String nextStatus;
        if ("approve".equals(action)) {
            nextStatus = "y";
        } else if ("reject".equals(action)) {
            nextStatus = "n";
        } else {
            throw new RuntimeException("无效的审核操作");
        }

        if (!"t".equals(target.getStatus())) {
            throw new RuntimeException("仅待审核状态的家长可审核");
        }

        target.setStatus(nextStatus);
        target.setUpdateUserId(operator.getUserId());
        target.setUpdateTime(new Date());
        sysUserRepository.save(target);
    }

    private boolean matchesKeyword(SysUser user, String keyword) {
        return contains(user.getLoginName(), keyword)
                || contains(user.getUserName(), keyword)
                || contains(user.getUserNickName(), keyword)
                || contains(user.getUserPhone(), keyword);
    }

    private boolean contains(String value, String keyword) {
        return StringUtils.hasText(value) && value.contains(keyword);
    }

    private Map<String, String> buildOrgNameMap() {
        Map<String, String> map = new HashMap<String, String>();
        for (SysOrg org : sysOrgRepository.findAll()) {
            if (StringUtils.hasText(org.getOrgId())) {
                map.put(org.getOrgId(), org.getOrgName());
            }
        }
        return map;
    }

    private ParentRegistrationListItem toListItem(SysUser user, Map<String, String> orgNameMap) {
        ParentRegistrationListItem item = new ParentRegistrationListItem();
        item.setUserId(user.getUserId());
        item.setLoginName(user.getLoginName());
        item.setUserName(user.getUserName());
        item.setUserNickName(user.getUserNickName());
        item.setUserPhone(user.getUserPhone());
        item.setStatus(user.getStatus());
        item.setRootOrgId(user.getRootOrgId());
        if (StringUtils.hasText(user.getRootOrgId())) {
            item.setRootOrgName(orgNameMap.get(user.getRootOrgId()));
        }
        item.setCreateTime(formatDate(user.getCreateTime()));
        return item;
    }

    private String formatDate(Date value) {
        return value == null ? null : DATE_TIME.format(value);
    }
}
