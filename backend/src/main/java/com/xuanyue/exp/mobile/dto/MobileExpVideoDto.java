package com.xuanyue.exp.mobile.dto;

public class MobileExpVideoDto {

    private String seqId;
    private String videoUrl;
    private String previewUrl;
    private String expId;
    private Integer sortOrder;
    private String fileId;

    public String getSeqId() { return seqId; }
    public void setSeqId(String seqId) { this.seqId = seqId; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
    public String getExpId() { return expId; }
    public void setExpId(String expId) { this.expId = expId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
}
