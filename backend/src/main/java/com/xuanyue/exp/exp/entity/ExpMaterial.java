package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "exp_material")
public class ExpMaterial {

    @Id
    @Column(name = "exp_material_id")
    private String expMaterialId;

    @Column(name = "exp_id")
    private String expId;

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

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Transient
    private String mainPicPreviewUrl;

    public String getMainPicPreviewUrl() { return mainPicPreviewUrl; }
    public void setMainPicPreviewUrl(String mainPicPreviewUrl) { this.mainPicPreviewUrl = mainPicPreviewUrl; }
    public String getExpMaterialId() { return expMaterialId; }
    public void setExpMaterialId(String expMaterialId) { this.expMaterialId = expMaterialId; }
    public String getExpId() { return expId; }
    public void setExpId(String expId) { this.expId = expId; }
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
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
