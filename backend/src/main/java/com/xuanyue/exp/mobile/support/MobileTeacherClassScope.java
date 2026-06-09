package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.mobile.entity.MbParentChild;
import com.xuanyue.exp.mobile.entity.MbTask;
import com.xuanyue.exp.system.entity.SysOrg;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysOrgRepository;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 解析教师可管理的班级组织范围（与布置作业选项一致：rootOrgId 下全部班级 + 任务班级 + 教师直属班级）。
 */
public final class MobileTeacherClassScope {

    private static final String CLASS_ORG_TYPE = "Org_School_Class";

    private MobileTeacherClassScope() {
    }

    public static Set<String> resolveClassOrgIds(SysUser teacher, List<MbTask> teacherTasks,
                                                 SysOrgRepository sysOrgRepository) {
        Set<String> ids = new LinkedHashSet<>();
        if (teacherTasks != null) {
            for (MbTask task : teacherTasks) {
                if (StringUtils.hasText(task.getClassOrgId())) {
                    ids.add(task.getClassOrgId().trim());
                }
            }
        }
        if (teacher == null) {
            return ids;
        }
        if (StringUtils.hasText(teacher.getUserOrgId())) {
            sysOrgRepository.findById(teacher.getUserOrgId()).ifPresent(org -> {
                if (CLASS_ORG_TYPE.equals(org.getOrgTypeId())) {
                    ids.add(org.getOrgId());
                } else {
                    // 教师可能挂在年级/学校节点，向下展开全部班级
                    collectClassOrgIdsUnderRoot(org.getOrgId(), sysOrgRepository, ids);
                }
            });
        }
        if (StringUtils.hasText(teacher.getRootOrgId())) {
            collectClassOrgIdsUnderRoot(teacher.getRootOrgId(), sysOrgRepository, ids);
        }
        return ids;
    }

    public static boolean isChildInScope(String childUserId, Set<String> classOrgIds,
                                         SysUserRepositoryAccessor userRepo) {
        if (!StringUtils.hasText(childUserId) || classOrgIds == null || classOrgIds.isEmpty()) {
            return false;
        }
        return userRepo.findById(childUserId.trim())
                .map(u -> classOrgIds.contains(safe(u.getUserOrgId())))
                .orElse(false);
    }

    public static boolean isBindInScope(MbParentChild bind, Set<String> classOrgIds,
                                        SysUserRepositoryAccessor userRepo) {
        if (bind == null || classOrgIds == null || classOrgIds.isEmpty()) {
            return false;
        }
        if (StringUtils.hasText(bind.getClassOrgId()) && classOrgIds.contains(bind.getClassOrgId().trim())) {
            return true;
        }
        return isChildInScope(bind.getChildUserId(), classOrgIds, userRepo);
    }

    private static void collectClassOrgIdsUnderRoot(String rootOrgId, SysOrgRepository sysOrgRepository,
                                                    Set<String> ids) {
        if (!StringUtils.hasText(rootOrgId)) {
            return;
        }
        Map<String, List<SysOrg>> byParent = new HashMap<>();
        for (SysOrg org : sysOrgRepository.findAll()) {
            if (!"y".equalsIgnoreCase(safe(org.getStatus()))) {
                continue;
            }
            String parent = org.getParentOrgId() != null ? org.getParentOrgId() : "";
            byParent.computeIfAbsent(parent, k -> new ArrayList<>()).add(org);
        }
        walkOrgTree(rootOrgId, byParent, ids, 0);
    }

    private static void walkOrgTree(String orgId, Map<String, List<SysOrg>> byParent, Set<String> ids, int depth) {
        if (depth > 8 || !StringUtils.hasText(orgId)) {
            return;
        }
        for (SysOrg child : byParent.getOrDefault(orgId, Collections.emptyList())) {
            if (CLASS_ORG_TYPE.equals(child.getOrgTypeId())) {
                ids.add(child.getOrgId());
            }
            walkOrgTree(child.getOrgId(), byParent, ids, depth + 1);
        }
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }

    /** 便于在非 Spring 工具类中注入用户查询 */
    public interface SysUserRepositoryAccessor {
        Optional<SysUser> findById(String userId);
    }
}
