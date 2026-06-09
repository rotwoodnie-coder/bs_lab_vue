package com.xuanyue.exp.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "material_pic")
public class MaterialPic {

    @Id
    @Column(name = "seq_id")
    private String seqId;

    @Column(name = "material_id")
    private String materialId;

    @Column(name = "material_url")
    private String materialUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "create_time")
    private Date createTime;

    public String getSeqId() { return seqId; }
    public void setSeqId(String seqId) { this.seqId = seqId; }
    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }
    public String getMaterialUrl() { return materialUrl; }
    public void setMaterialUrl(String materialUrl) { this.materialUrl = materialUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
