package com.xuanyue.exp.system.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "sys_user_role")
@IdClass(SysUserRole.SysUserRoleId.class)
public class SysUserRole {

    @Id
    @Column(name = "seq_id")
    private String seqId;

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "role_id")
    private String roleId;

    public String getSeqId() {
        return seqId;
    }

    public void setSeqId(String seqId) {
        this.seqId = seqId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public static class SysUserRoleId implements Serializable {
        private String seqId;
        private String userId;

        public SysUserRoleId() {
        }

        public String getSeqId() {
            return seqId;
        }

        public void setSeqId(String seqId) {
            this.seqId = seqId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SysUserRoleId that = (SysUserRoleId) o;
            return Objects.equals(seqId, that.seqId) && Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(seqId, userId);
        }
    }
}
