package com.xuanyue.exp.exp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "exp_video")
public class ExpVideo {

    @Id
    @Column(name = "seq_id")
    private String seqId;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "exp_id")
    private String expId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "file_id")
    private String fileId;

    @Transient
    private String previewUrl;

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
    public String getSeqId() { return seqId; }
    public void setSeqId(String seqId) { this.seqId = seqId; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getExpId() { return expId; }
    public void setExpId(String expId) { this.expId = expId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
}
