package com.xuanyue.exp.mobile.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "mb_notice_read")
@IdClass(MbNoticeRead.NoticeReadId.class)
public class MbNoticeRead {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "notice_id")
    private String noticeId;

    @Column(name = "read_time")
    private Date readTime;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getNoticeId() { return noticeId; }
    public void setNoticeId(String noticeId) { this.noticeId = noticeId; }
    public Date getReadTime() { return readTime; }
    public void setReadTime(Date readTime) { this.readTime = readTime; }

    public static class NoticeReadId implements Serializable {
        private String userId;
        private String noticeId;

        public NoticeReadId() {
        }

        public NoticeReadId(String userId, String noticeId) {
            this.userId = userId;
            this.noticeId = noticeId;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getNoticeId() { return noticeId; }
        public void setNoticeId(String noticeId) { this.noticeId = noticeId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NoticeReadId)) return false;
            NoticeReadId that = (NoticeReadId) o;
            return java.util.Objects.equals(userId, that.userId)
                    && java.util.Objects.equals(noticeId, that.noticeId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(userId, noticeId);
        }
    }
}
