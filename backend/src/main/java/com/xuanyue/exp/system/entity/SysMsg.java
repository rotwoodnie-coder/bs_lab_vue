package com.xuanyue.exp.system.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "sys_msg")
public class SysMsg {

    @Id
    @Column(name = "msg_id")
    private String msgId;

    @Column(name = "receiver_user_id")
    private String receiverUserId;

    @Column(name = "sender_user_id")
    private String senderUserId;

    @Column(name = "msg_type_id")
    private String msgTypeId;

    @Column(name = "msg_content")
    private String msgContent;

    @Column(name = "read_tag")
    private String readTag;

    @Column(name = "send_time")
    private Date sendTime;

    @Column(name = "read_time")
    private Date readTime;

    @Column(name = "link_id")
    private String linkId;

    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }
    public String getReceiverUserId() { return receiverUserId; }
    public void setReceiverUserId(String receiverUserId) { this.receiverUserId = receiverUserId; }
    public String getSenderUserId() { return senderUserId; }
    public void setSenderUserId(String senderUserId) { this.senderUserId = senderUserId; }
    public String getMsgTypeId() { return msgTypeId; }
    public void setMsgTypeId(String msgTypeId) { this.msgTypeId = msgTypeId; }
    public String getMsgContent() { return msgContent; }
    public void setMsgContent(String msgContent) { this.msgContent = msgContent; }
    public String getReadTag() { return readTag; }
    public void setReadTag(String readTag) { this.readTag = readTag; }
    public Date getSendTime() { return sendTime; }
    public void setSendTime(Date sendTime) { this.sendTime = sendTime; }
    public Date getReadTime() { return readTime; }
    public void setReadTime(Date readTime) { this.readTime = readTime; }
    public String getLinkId() { return linkId; }
    public void setLinkId(String linkId) { this.linkId = linkId; }
}
