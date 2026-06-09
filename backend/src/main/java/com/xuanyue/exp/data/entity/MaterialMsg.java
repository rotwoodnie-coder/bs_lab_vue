package com.xuanyue.exp.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "material_msg")
public class MaterialMsg {

    @Id
    @Column(name = "material_id")
    private String materialId;

    @Column(name = "material_name")
    private String materialName;

    @Column(name = "material_prop_id")
    private String materialPropId;

    @Column(name = "material_type_id")
    private String materialTypeId;

    @Column(name = "material_num")
    private String materialNum;

    @Column(name = "main_pic_url")
    private String mainPicUrl;

    @Column(name = "exp_purpose")
    private String expPurpose;

    @Column(name = "security_comments")
    private String securityComments;

    @Column(name = "additional_comments")
    private String additionalComments;

    @Column(name = "status")
    private String status;

    @Column(name = "is_public")
    private String isPublic;

    @Column(name = "create_user_id")
    private String createUserId;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_user_id")
    private String updateUserId;

    @Column(name = "update_time")
    private Date updateTime;

    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getMaterialPropId() { return materialPropId; }
    public void setMaterialPropId(String materialPropId) { this.materialPropId = materialPropId; }
    public String getMaterialTypeId() { return materialTypeId; }
    public void setMaterialTypeId(String materialTypeId) { this.materialTypeId = materialTypeId; }
    public String getMaterialNum() { return materialNum; }
    public void setMaterialNum(String materialNum) { this.materialNum = materialNum; }
    public String getMainPicUrl() { return mainPicUrl; }
    public void setMainPicUrl(String mainPicUrl) { this.mainPicUrl = mainPicUrl; }
    public String getExpPurpose() { return expPurpose; }
    public void setExpPurpose(String expPurpose) { this.expPurpose = expPurpose; }
    public String getSecurityComments() { return securityComments; }
    public void setSecurityComments(String securityComments) { this.securityComments = securityComments; }
    public String getAdditionalComments() { return additionalComments; }
    public void setAdditionalComments(String additionalComments) { this.additionalComments = additionalComments; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIsPublic() { return isPublic; }
    public void setIsPublic(String isPublic) { this.isPublic = isPublic; }
    public String getCreateUserId() { return createUserId; }
    public void setCreateUserId(String createUserId) { this.createUserId = createUserId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getUpdateUserId() { return updateUserId; }
    public void setUpdateUserId(String updateUserId) { this.updateUserId = updateUserId; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
