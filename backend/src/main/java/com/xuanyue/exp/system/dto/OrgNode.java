package com.xuanyue.exp.system.dto;

import java.util.ArrayList;
import java.util.List;

public class OrgNode {

    private String orgId;
    private String orgName;
    private String orgTypeId;
    private String parentOrgId;
    private String status;
    private Integer sortOrder;
    private List<OrgNode> children = new ArrayList<>();

    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getOrgTypeId() { return orgTypeId; }
    public void setOrgTypeId(String orgTypeId) { this.orgTypeId = orgTypeId; }
    public String getParentOrgId() { return parentOrgId; }
    public void setParentOrgId(String parentOrgId) { this.parentOrgId = parentOrgId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public List<OrgNode> getChildren() { return children; }
    public void setChildren(List<OrgNode> children) { this.children = children; }
}
