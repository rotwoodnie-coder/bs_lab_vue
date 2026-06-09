package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.mobile.dto.MobileTaskDto;
import com.xuanyue.exp.mobile.dto.MobileWorkDetailDto;
import com.xuanyue.exp.mobile.dto.MobileWorkItemDto;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.mobile.entity.MbTaskSubmission;
import com.xuanyue.exp.mobile.entity.MbWork;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class MobileEntityMapper {

    private static final SimpleDateFormat DEADLINE_FMT = new SimpleDateFormat("今天 HH:mm");

    private MobileEntityMapper() {
    }

    public static MobileTaskDto toTaskDto(MbTask task, MbTaskSubmission submission) {
        MobileTaskDto dto = new MobileTaskDto();
        dto.setId(task.getTaskId());
        dto.setType(task.getTaskType());
        dto.setTitle(task.getTitle());
        dto.setDesc(task.getDescription());
        if (submission != null) {
            dto.setState(submission.getState());
            dto.setStateLabel(submission.getStateLabel());
            dto.setBadgeClass(submission.getBadgeClass());
        } else {
            dto.setState("pending");
            dto.setStateLabel("待完成");
            dto.setBadgeClass("badge-warning");
        }
        if (task.getDeadline() != null) {
            dto.setDeadline(formatDeadline(task.getDeadline()));
        }
        dto.setTeacherHint(task.getTeacherHint());
        dto.setTeacherHintClass(task.getTeacherHintClass());
        dto.setVideoTitle(task.getVideoTitle());
        dto.setVideoDuration(task.getVideoDuration());
        dto.setVideoMeta(task.getVideoMeta());
        dto.setVideoId(task.getVideoId());
        dto.setTaskTypeLabel(task.getTaskTypeLabel());
        dto.setRequirements(MobileJsonUtils.parseStringList(task.getRequirementsJson()));
        dto.setSteps(MobileJsonUtils.parseStringList(task.getStepsJson()));
        dto.setUploadQuery(task.getUploadQuery() != null ? task.getUploadQuery() : "");
        return dto;
    }

    public static MobileWorkItemDto toWorkItem(MbWork work, String author, String authorInitial) {
        MobileWorkItemDto item = new MobileWorkItemDto();
        item.setId(work.getWorkId());
        item.setType(work.getWorkType());
        item.setTitle(work.getTitle());
        item.setAuthor(author != null ? author : "同学");
        item.setAuthorInitial(authorInitial != null ? authorInitial : "同");
        item.setClassName(work.getClassName());
        item.setSchool(work.getSchoolName());
        item.setTint(work.getTint());
        return item;
    }

    public static MobileWorkDetailDto toWorkDetail(MbWork work, String author) {
        MobileWorkDetailDto dto = new MobileWorkDetailDto();
        dto.setId(work.getWorkId());
        dto.setTitle(work.getTitle());
        dto.setGrade(work.getGrade());
        dto.setAuthor(author != null ? author : "同学");
        dto.setClassName(work.getClassName());
        if (work.getCreateTime() != null) {
            dto.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(work.getCreateTime()));
        }
        dto.setDesc(work.getDescription());
        dto.setLikes(work.getLikeCount() != null ? work.getLikeCount() : 0);
        dto.setComments(work.getCommentCount() != null ? work.getCommentCount() : 0);
        if (work.getTeacherReviewText() != null) {
            MobileWorkDetailDto.TeacherReview review = new MobileWorkDetailDto.TeacherReview();
            review.setName(work.getTeacherReviewName());
            review.setText(work.getTeacherReviewText());
            review.setStars(work.getTeacherReviewStars() != null ? work.getTeacherReviewStars() : 5);
            dto.setTeacherReview(review);
        }
        return dto;
    }

    private static String formatDeadline(Date deadline) {
        Calendar cal = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        cal.setTime(deadline);
        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "今天 " + new SimpleDateFormat("HH:mm").format(deadline);
        }
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        if (cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR)
                && cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)) {
            return "明天 " + new SimpleDateFormat("HH:mm").format(deadline);
        }
        return new SimpleDateFormat("MM-dd HH:mm").format(deadline);
    }
}
