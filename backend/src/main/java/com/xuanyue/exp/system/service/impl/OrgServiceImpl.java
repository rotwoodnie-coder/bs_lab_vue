package com.xuanyue.exp.system.service.impl;

import com.xuanyue.exp.system.dto.OrgNode;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import com.xuanyue.exp.system.repository.SysUserRepository;
import com.xuanyue.exp.system.service.OrgService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class OrgServiceImpl implements OrgService {

    private static final String SYS_ADMIN_ROLE_ID = "Sys_Admin";

    private final SysOrgRepository sysOrgRepository;
    private final SysUserRepository sysUserRepository;

    public OrgServiceImpl(SysOrgRepository sysOrgRepository, SysUserRepository sysUserRepository) {
        this.sysOrgRepository = sysOrgRepository;
        this.sysUserRepository = sysUserRepository;
    }

    @Override
    public List<OrgNode> tree() {
        SysUser currentUser = getCurrentUser();
        boolean isAdmin = isSysAdmin(currentUser);
        String rootOrgId = currentUser.getRootOrgId();

        List<SysOrg> all = sysOrgRepository.findAll();
        List<OrgNode> roots = new ArrayList<>();
        for (SysOrg org : all) {
            if (!StringUtils.hasText(org.getParentOrgId())) {
                roots.add(toNode(org));
            }
        }
        roots.sort(Comparator.comparing(OrgNode::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(OrgNode::getOrgName, Comparator.nullsLast(String::compareTo)));

        List<OrgNode> result = new ArrayList<>();
        for (OrgNode root : roots) {
            if (isAdmin || root.getOrgId().equals(rootOrgId)) {
                attachChildren(root, all, isAdmin, rootOrgId);
                result.add(root);
            }
        }
        return result;
    }

    @Override
    public SysOrg get(String id) {
        return sysOrgRepository.findById(id).orElseThrow(() -> new RuntimeException("组织不存在"));
    }

    @Override
    public void create(SysOrg org) {
        if (!StringUtils.hasText(org.getOrgId())) {
            org.setOrgId(UUID.randomUUID().toString().replace("-", ""));
        }
        sysOrgRepository.save(org);
    }

    @Override
    public void update(String id, SysOrg org) {
        SysOrg current = get(id);
        current.setOrgName(org.getOrgName());
        current.setOrgTypeId(org.getOrgTypeId());
        current.setParentOrgId(org.getParentOrgId());
        current.setOrgPath(org.getOrgPath());
        current.setStatus(org.getStatus());
        current.setSortOrder(org.getSortOrder());
        sysOrgRepository.save(current);
    }

    @Override
    public void delete(String id) {
        sysOrgRepository.deleteById(id);
    }

    private OrgNode toNode(SysOrg org) {
        OrgNode node = new OrgNode();
        node.setOrgId(org.getOrgId());
        node.setOrgName(org.getOrgName());
        node.setOrgTypeId(org.getOrgTypeId());
        node.setParentOrgId(org.getParentOrgId());
        node.setStatus(org.getStatus());
        node.setSortOrder(org.getSortOrder());
        return node;
    }

    private void attachChildren(OrgNode parent, List<SysOrg> all, boolean isAdmin, String rootOrgId) {
        List<OrgNode> children = new ArrayList<>();
        for (SysOrg org : all) {
            if (parent.getOrgId().equals(org.getParentOrgId())) {
                OrgNode child = toNode(org);
                attachChildren(child, all, isAdmin, rootOrgId);
                children.add(child);
            }
        }
        children.sort(Comparator.comparing(OrgNode::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(OrgNode::getOrgName, Comparator.nullsLast(String::compareTo)));
        parent.setChildren(children);
    }

    private boolean isSysAdmin(SysUser user) {
        return user != null && SYS_ADMIN_ROLE_ID.equals(user.getUserRoleId());
    }

    private SysUser getCurrentUser() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            throw new RuntimeException("无法获取当前登录用户");
        }
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        String userId = request.getHeader("X-User-Id");
        if (!StringUtils.hasText(userId)) {
            throw new RuntimeException("未获取到登录用户信息");
        }
        return sysUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("登录用户不存在"));
    }
}
