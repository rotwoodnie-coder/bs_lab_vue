package com.xuanyue.exp.system.dto;

public class RoleDto {

    private String roleId;
    private String roleName;
    private String roleCode;
    private String status;
    private String remark;

    public RoleDto() {
    }

    public RoleDto(String roleId, String roleName, String roleCode, String status, String remark) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.status = status;
        this.remark = remark;
    }

    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
