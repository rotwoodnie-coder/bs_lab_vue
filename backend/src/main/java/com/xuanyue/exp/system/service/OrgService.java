package com.xuanyue.exp.system.service;

import com.xuanyue.exp.system.dto.OrgNode;
import com.xuanyue.exp.system.entity.SysOrg;

import java.util.List;

public interface OrgService {
    List<OrgNode> tree();
    SysOrg get(String id);
    void create(SysOrg org);
    void update(String id, SysOrg org);
    void delete(String id);
}
