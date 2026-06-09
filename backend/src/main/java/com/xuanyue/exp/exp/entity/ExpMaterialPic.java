package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "exp_material_pic")
public class ExpMaterialPic {

    @Id
    @Column(name = "seq_id")
    private String seqId;

    @Column(name = "exp_material_id")
    private String expMaterialId;

    @Column(name = "material_id")
    private String materialId;

    @Column(name = "material_url")
    private String materialUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Transient
    private String materialPreviewUrl;

    public String getMaterialPreviewUrl() { return materialPreviewUrl; }
    public void setMaterialPreviewUrl(String materialPreviewUrl) { this.materialPreviewUrl = materialPreviewUrl; }
    public String getSeqId() { return seqId; }
    public void setSeqId(String seqId) { this.seqId = seqId; }
    public String getExpMaterialId() { return expMaterialId; }
    public void setExpMaterialId(String expMaterialId) { this.expMaterialId = expMaterialId; }
    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }
    public String getMaterialUrl() { return materialUrl; }
    public void setMaterialUrl(String materialUrl) { this.materialUrl = materialUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
