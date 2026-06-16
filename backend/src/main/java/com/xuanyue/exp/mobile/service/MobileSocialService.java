package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.storage.minio.MinioStorageService;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.mobile.dto.CreateCommentRequest;
import com.xuanyue.exp.mobile.dto.MobileCommentDto;
import com.xuanyue.exp.mobile.dto.MobileSocialSummaryDto;
import com.xuanyue.exp.mobile.dto.ToggleReactionRequest;
import com.xuanyue.exp.mobile.entity.MbComment;
import com.xuanyue.exp.mobile.entity.MbUserReaction;
import com.xuanyue.exp.mobile.repository.MbCommentRepository;
import com.xuanyue.exp.mobile.repository.MbUserReactionRepository;
import com.xuanyue.exp.mobile.repository.MbWorkRepository;
import com.xuanyue.exp.mobile.support.MobileAvatarSupport;
import com.xuanyue.exp.mobile.support.MobileIds;
import com.xuanyue.exp.mobile.support.MobileUserContext;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MobileSocialService {

    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final Set<String> TARGET_TYPES = new HashSet<>(Arrays.asList("exp_msg", "work", "exp_simulator", "exp_video"));
    private static final Set<String> REACTION_TYPES = new HashSet<>(Arrays.asList("like", "collect"));

    private final MbCommentRepository commentRepository;
    private final MbUserReactionRepository reactionRepository;
    private final MbWorkRepository workRepository;
    private final ExpMsgRepository expMsgRepository;
    private final SysUserRepository sysUserRepository;
    private final MinioStorageService minioStorageService;

    public MobileSocialService(MbCommentRepository commentRepository,
                               MbUserReactionRepository reactionRepository,
                               MbWorkRepository workRepository,
                               ExpMsgRepository expMsgRepository,
                               SysUserRepository sysUserRepository,
                               MinioStorageService minioStorageService) {
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
        this.workRepository = workRepository;
        this.expMsgRepository = expMsgRepository;
        this.sysUserRepository = sysUserRepository;
        this.minioStorageService = minioStorageService;
    }

    public List<MobileCommentDto> listComments(String userId, String targetType, String targetId) {
        validateTarget(targetType, targetId);
        String uid = MobileUserContext.resolveStudentId(userId);
        return commentRepository.findByTargetTypeAndTargetIdAndStatusOrderByCreateTimeDesc(
                        targetType.trim(), targetId.trim(), "y")
                .stream()
                .map(c -> toCommentDto(c, uid))
                .collect(Collectors.toList());
    }

    public MobileSocialSummaryDto getSummary(String userId, String targetType, String targetId) {
        validateTarget(targetType, targetId);
        String uid = MobileUserContext.resolveStudentId(userId);
        String type = targetType.trim();
        String id = targetId.trim();

        MobileSocialSummaryDto dto = new MobileSocialSummaryDto();
        dto.setCommentCount((int) commentRepository.countByTargetTypeAndTargetIdAndStatus(type, id, "y"));
        dto.setLiked(reactionRepository.existsByUserIdAndTargetIdAndTargetTypeAndReactionType(uid, id, type, "like"));
        dto.setCollected(reactionRepository.existsByUserIdAndTargetIdAndTargetTypeAndReactionType(uid, id, type, "collect"));

        if ("work".equals(type)) {
            workRepository.findById(id).ifPresent(work -> {
                dto.setLikeCount(work.getLikeCount() != null ? work.getLikeCount() : 0);
                dto.setCommentCount(work.getCommentCount() != null ? work.getCommentCount() : dto.getCommentCount());
            });
            if (dto.getLikeCount() == 0) {
                dto.setLikeCount((int) reactionRepository.countByTargetIdAndTargetTypeAndReactionType(id, type, "like"));
            }
            dto.setCollectCount((int) reactionRepository.countByTargetIdAndTargetTypeAndReactionType(id, type, "collect"));
        } else if ("exp_msg".equals(type)) {
            expMsgRepository.findById(id).ifPresent(exp -> {
                dto.setLikeCount(exp.getLikeNum() != null ? exp.getLikeNum() : 0);
                dto.setCollectCount(exp.getCollectionNum() != null ? exp.getCollectionNum() : 0);
                if (exp.getEvaluateNum() != null && exp.getEvaluateNum() > 0) {
                    dto.setCommentCount(exp.getEvaluateNum());
                }
            });
        } else {
            dto.setLikeCount((int) reactionRepository.countByTargetIdAndTargetTypeAndReactionType(id, type, "like"));
            dto.setCollectCount((int) reactionRepository.countByTargetIdAndTargetTypeAndReactionType(id, type, "collect"));
        }
        return dto;
    }

    @Transactional
    public MobileCommentDto addComment(String userId, CreateCommentRequest request) {
        if (request == null || !StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        validateTarget(request.getTargetType(), request.getTargetId());
        String uid = requireUserId(userId);
        SysUser user = sysUserRepository.findById(uid).orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        MbComment comment = new MbComment();
        comment.setCommentId(MobileIds.newId("cmt"));
        comment.setTargetType(request.getTargetType().trim());
        comment.setTargetId(request.getTargetId().trim());
        comment.setUserId(uid);
        comment.setUserName(resolveDisplayName(user));
        comment.setUserRoleTag(normalizeRole(user.getUserRoleId()));
        comment.setContent(request.getContent().trim());
        comment.setLikeCount(0);
        comment.setStatus("y");
        comment.setCreateTime(new Date());
        commentRepository.save(comment);
        incrementCommentCount(comment.getTargetType(), comment.getTargetId());
        return toCommentDto(comment, uid);
    }

    @Transactional
    public MobileSocialSummaryDto toggleReaction(String userId, ToggleReactionRequest request) {
        if (request == null || !StringUtils.hasText(request.getReactionType())) {
            throw new IllegalArgumentException("互动类型不能为空");
        }
        String reactionType = request.getReactionType().trim().toLowerCase(Locale.ROOT);
        if (!REACTION_TYPES.contains(reactionType)) {
            throw new IllegalArgumentException("不支持的互动类型");
        }
        validateTarget(request.getTargetType(), request.getTargetId());
        String uid = requireUserId(userId);
        String type = request.getTargetType().trim();
        String id = request.getTargetId().trim();

        long removed = reactionRepository.deleteByUserIdAndTargetIdAndTargetTypeAndReactionType(
                uid, id, type, reactionType);
        if (removed > 0) {
            reactionRepository.flush();
            adjustReactionCount(type, id, reactionType, -1);
        } else {
            MbUserReaction reaction = new MbUserReaction();
            reaction.setUserId(uid);
            reaction.setTargetId(id);
            reaction.setTargetType(type);
            reaction.setReactionType(reactionType);
            reaction.setCreatedAt(new Date());
            reactionRepository.save(reaction);
            reactionRepository.flush();
            adjustReactionCount(type, id, reactionType, 1);
        }
        return getSummary(uid, type, id);
    }

    @Transactional
    public MobileCommentDto toggleCommentLike(String userId, String commentId) {
        if (!StringUtils.hasText(commentId)) {
            throw new IllegalArgumentException("评论 ID 不能为空");
        }
        String uid = requireUserId(userId);
        MbComment comment = commentRepository.findById(commentId.trim())
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));

        Optional<MbUserReaction> existing = reactionRepository.findByUserIdAndTargetIdAndTargetTypeAndReactionType(
                uid, comment.getCommentId(), "comment", "like");
        int count = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
        if (existing.isPresent()) {
            reactionRepository.deleteByUserIdAndTargetIdAndTargetTypeAndReactionType(
                    uid, comment.getCommentId(), "comment", "like");
            reactionRepository.flush();
            comment.setLikeCount(Math.max(0, count - 1));
        } else {
            MbUserReaction reaction = new MbUserReaction();
            reaction.setUserId(uid);
            reaction.setTargetId(comment.getCommentId());
            reaction.setTargetType("comment");
            reaction.setReactionType("like");
            reaction.setCreatedAt(new Date());
            reactionRepository.save(reaction);
            comment.setLikeCount(count + 1);
        }
        commentRepository.save(comment);
        return toCommentDto(comment, uid);
    }

    private String requireUserId(String userId) {
        String uid = MobileUserContext.resolveStudentId(userId);
        if (!StringUtils.hasText(uid)) {
            throw new IllegalArgumentException("请先登录");
        }
        return uid.trim();
    }

    private void validateTarget(String targetType, String targetId) {
        if (!StringUtils.hasText(targetType) || !StringUtils.hasText(targetId)) {
            throw new IllegalArgumentException("目标不能为空");
        }
        if (!TARGET_TYPES.contains(targetType.trim())) {
            throw new IllegalArgumentException("不支持的目标类型");
        }
    }

    private MobileCommentDto toCommentDto(MbComment comment, String currentUserId) {
        MobileCommentDto dto = new MobileCommentDto();
        dto.setId(comment.getCommentId());
        dto.setUserName(comment.getUserName());
        dto.setUserInitial(initialOf(comment.getUserName()));
        dto.setUserAvatarUrl(resolveCommentAvatarUrl(comment.getUserId()));
        dto.setUserRoleTag(comment.getUserRoleTag());
        dto.setContent(comment.getContent());
        dto.setLikeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0);
        if (StringUtils.hasText(currentUserId)) {
            dto.setLiked(reactionRepository.existsByUserIdAndTargetIdAndTargetTypeAndReactionType(
                    currentUserId, comment.getCommentId(), "comment", "like"));
        } else {
            dto.setLiked(false);
        }
        dto.setMine(currentUserId != null && currentUserId.equals(comment.getUserId()));
        if (comment.getCreateTime() != null) {
            dto.setTimeLabel(TIME_FMT.format(comment.getCreateTime()));
        }
        return dto;
    }

    private void incrementCommentCount(String targetType, String targetId) {
        if ("work".equals(targetType)) {
            workRepository.findById(targetId).ifPresent(work -> {
                int count = work.getCommentCount() != null ? work.getCommentCount() : 0;
                work.setCommentCount(count + 1);
                workRepository.save(work);
            });
        } else if ("exp_msg".equals(targetType)) {
            expMsgRepository.findById(targetId).ifPresent(exp -> {
                int count = exp.getEvaluateNum() != null ? exp.getEvaluateNum() : 0;
                exp.setEvaluateNum(count + 1);
                expMsgRepository.save(exp);
            });
        }
    }

    private void adjustReactionCount(String targetType, String targetId, String reactionType, int delta) {
        if ("work".equals(targetType) && "like".equals(reactionType)) {
            workRepository.findById(targetId).ifPresent(work -> {
                int count = Math.max(0, (work.getLikeCount() != null ? work.getLikeCount() : 0) + delta);
                work.setLikeCount(count);
                workRepository.save(work);
            });
            return;
        }
        if ("exp_msg".equals(targetType)) {
            expMsgRepository.findById(targetId).ifPresent(exp -> {
                if ("like".equals(reactionType)) {
                    int count = Math.max(0, (exp.getLikeNum() != null ? exp.getLikeNum() : 0) + delta);
                    exp.setLikeNum(count);
                } else if ("collect".equals(reactionType)) {
                    int count = Math.max(0, (exp.getCollectionNum() != null ? exp.getCollectionNum() : 0) + delta);
                    exp.setCollectionNum(count);
                }
                expMsgRepository.save(exp);
            });
        }
    }

    private String resolveDisplayName(SysUser user) {
        if (StringUtils.hasText(user.getUserNickName())) {
            return user.getUserNickName();
        }
        return user.getUserName();
    }

    private String normalizeRole(String roleId) {
        if (!StringUtils.hasText(roleId)) {
            return "student";
        }
        return roleId.toLowerCase(Locale.ROOT);
    }

    private String initialOf(String name) {
        return MobileAvatarSupport.initialOf(name, "用");
    }

    private String resolveCommentAvatarUrl(String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        Optional<SysUser> user = sysUserRepository.findById(userId);
        return user.map(u -> MobileAvatarSupport.resolveUserAvatarUrl(minioStorageService, u)).orElse(null);
    }
}
