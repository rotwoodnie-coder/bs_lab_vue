package com.xuanyue.exp.mobile.service;

import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.data.entity.DataSchoolSubject;
import com.xuanyue.exp.data.repository.DataSchoolSubjectRepository;
import com.xuanyue.exp.edu.entity.SubjectGroup;
import com.xuanyue.exp.edu.entity.SubjectGroupResearcher;
import com.xuanyue.exp.edu.repository.SubjectGroupRepository;
import com.xuanyue.exp.edu.repository.SubjectGroupResearcherRepository;
import com.xuanyue.exp.exp.entity.ExpMsg;
import com.xuanyue.exp.exp.repository.ExpMsgRepository;
import com.xuanyue.exp.exp.service.ExpStandardService;
import com.xuanyue.exp.mobile.dto.ResearcherExpAuditItemDto;
import com.xuanyue.exp.mobile.dto.ResearcherExpAuditRequest;
import com.xuanyue.exp.system.entity.SysUser;
import com.xuanyue.exp.system.repository.SysUserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MobileResearcherExpAuditService {

    private static final Set<String> AUDIT_ROLES = new HashSet<>(Arrays.asList(
            "Researcher", "Sys_Admin", "School_Admin"));
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final ExpMsgRepository expMsgRepository;
    private final ExpStandardService expStandardService;
    private final SysUserRepository sysUserRepository;
    private final SubjectGroupResearcherRepository groupResearcherRepository;
    private final SubjectGroupRepository subjectGroupRepository;
    private final DataSchoolSubjectRepository subjectRepository;
    private final MobileNotificationService notificationService;

    public MobileResearcherExpAuditService(ExpMsgRepository expMsgRepository,
                                           ExpStandardService expStandardService,
                                           SysUserRepository sysUserRepository,
                                           SubjectGroupResearcherRepository groupResearcherRepository,
                                           SubjectGroupRepository subjectGroupRepository,
                                           DataSchoolSubjectRepository subjectRepository,
                                           MobileNotificationService notificationService) {
        this.expMsgRepository = expMsgRepository;
        this.expStandardService = expStandardService;
        this.sysUserRepository = sysUserRepository;
        this.groupResearcherRepository = groupResearcherRepository;
        this.subjectGroupRepository = subjectGroupRepository;
        this.subjectRepository = subjectRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public PageResult<ResearcherExpAuditItemDto> listPending(String userId, String expType, int page, int size) {
        SysUser user = requireAuditUser(userId);
        Specification<ExpMsg> spec = buildScopedSpec(user, "t", expType);
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "updateTime"));
        List<ExpMsg> rows = expMsgRepository.findAll(spec, pageable).getContent();
        long total = expMsgRepository.count(spec);
        Map<String, String> subjectNames = loadSubjectNames(rows);
        List<ResearcherExpAuditItemDto> items = rows.stream()
                .map(row -> toItem(row, subjectNames))
                .collect(Collectors.toList());
        return new PageResult<>(total, items);
    }

    @Transactional(readOnly = true)
    public int countPending(String userId) {
        SysUser user = requireAuditUser(userId);
        Specification<ExpMsg> spec = buildScopedSpec(user, "t", null);
        return (int) expMsgRepository.count(spec);
    }

    @Transactional(readOnly = true)
    public List<ResearcherExpAuditItemDto> listProcessed(String userId, int limit) {
        SysUser user = requireAuditUser(userId);
        // 已处理列表本就限定 confirmUserId = 本人，叠加范围校验确保口径一致
        Specification<ExpMsg> scope = buildScopedSpec(user, null, null);
        Specification<ExpMsg> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("status").in(Arrays.asList("y", "n")));
            predicates.add(cb.equal(root.get("confirmUserId"), user.getUserId()));
            Predicate scopePredicate = scope.toPredicate(root, query, cb);
            if (scopePredicate != null) {
                predicates.add(scopePredicate);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1),
                Sort.by(Sort.Direction.DESC, "confirmTime"));
        List<ExpMsg> rows = expMsgRepository.findAll(spec, pageable).getContent();
        Map<String, String> subjectNames = loadSubjectNames(rows);
        return rows.stream().map(row -> toItem(row, subjectNames)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDetail(String userId, String expId) {
        requireAuditUser(userId);
        ExpMsg entity = expMsgRepository.findById(expId.trim())
                .orElseThrow(() -> new IllegalArgumentException("实验不存在"));
        SysUser user = requireAuditUser(userId);
        ensureCanAccess(user, entity);
        return expStandardService.getDetailView(expId.trim());
    }

    @Transactional
    public void audit(String userId, String expId, ResearcherExpAuditRequest request) {
        SysUser user = requireAuditUser(userId);
        if (request == null || !StringUtils.hasText(request.getStatus())) {
            throw new IllegalArgumentException("请选择审核结果");
        }
        String status = request.getStatus().trim().toLowerCase();
        if (!"y".equals(status) && !"n".equals(status)) {
            throw new IllegalArgumentException("审核结果无效");
        }
        ExpMsg entity = expMsgRepository.findById(expId.trim())
                .orElseThrow(() -> new IllegalArgumentException("实验不存在"));
        ensureCanAccess(user, entity);
        if (!"t".equalsIgnoreCase(safe(entity.getStatus()))) {
            throw new IllegalArgumentException("该实验不在待审核状态");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("confirmComments", request.getConfirmComments());
        expStandardService.updateAudit(expId.trim(), payload);

        if ("n".equals(status) && StringUtils.hasText(entity.getCreateUserId())) {
            notificationService.sendExpAuditResult(userId, entity.getCreateUserId(),
                    entity.getExpName(), status, request.getConfirmComments());
        }
    }

    private SysUser requireAuditUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("请先登录");
        }
        SysUser user = sysUserRepository.findById(userId.trim())
                .orElseThrow(() -> new IllegalArgumentException("登录用户不存在"));
        if (!AUDIT_ROLES.contains(safe(user.getUserRoleId()))) {
            throw new IllegalArgumentException("无权限访问实验审核");
        }
        return user;
    }

    /** 系统管理员：全局范围，可审核所有学校的实验 */
    private boolean isGlobalAuditor(SysUser user) {
        return "Sys_Admin".equals(safe(user.getUserRoleId()));
    }

    /** 校管理员：限本校范围（按提交者所属 rootOrg） */
    private boolean isSchoolAuditor(SysUser user) {
        return "School_Admin".equals(safe(user.getUserRoleId()));
    }

    /** 本校全部成员的 userId 集合（用于按提交者限定校管理员审核范围） */
    private Set<String> resolveSchoolUserIds(SysUser user) {
        String rootOrgId = safe(user.getRootOrgId());
        if (rootOrgId.isEmpty()) {
            return Collections.emptySet();
        }
        return sysUserRepository.findByRootOrgId(rootOrgId).stream()
                .map(SysUser::getUserId)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    /**
     * 按角色解析审核范围 Specification：
     * 系统管理员=全局；校管理员=本校提交者；教研员=所辖学科。
     */
    private Specification<ExpMsg> buildScopedSpec(SysUser user, String status, String expType) {
        if (isGlobalAuditor(user)) {
            return buildSpec(status, expType, null, null, true);
        }
        if (isSchoolAuditor(user)) {
            return buildSpec(status, expType, null, resolveSchoolUserIds(user), false);
        }
        return buildSpec(status, expType, resolveSubjectScope(user), null, false);
    }

    private Set<String> resolveSubjectScope(SysUser user) {
        if (isGlobalAuditor(user)) {
            return Collections.emptySet();
        }
        List<SubjectGroupResearcher> assignments = groupResearcherRepository.findByResearcherUserId(user.getUserId());
        Set<String> groupIds = assignments.stream()
                .map(SubjectGroupResearcher::getGroupId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (groupIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> subjectIds = new LinkedHashSet<>();
        for (SubjectGroup group : subjectGroupRepository.findAll()) {
            if (group != null && groupIds.contains(group.getGroupId()) && StringUtils.hasText(group.getSubjectId())) {
                subjectIds.add(group.getSubjectId().trim());
            }
        }
        return subjectIds;
    }

    private void ensureCanAccess(SysUser user, ExpMsg entity) {
        if (isGlobalAuditor(user)) {
            return;
        }
        if (isSchoolAuditor(user)) {
            Set<String> schoolUserIds = resolveSchoolUserIds(user);
            if (schoolUserIds.isEmpty() || !schoolUserIds.contains(safe(entity.getCreateUserId()))) {
                throw new IllegalArgumentException("无权审核该实验（非本校提交）");
            }
            return;
        }
        Set<String> subjectIds = resolveSubjectScope(user);
        if (subjectIds.isEmpty()) {
            throw new IllegalArgumentException("未分配教研组，无法审核实验");
        }
        if (!subjectIds.contains(safe(entity.getSubjectId()))) {
            throw new IllegalArgumentException("无权审核该学科的实验");
        }
    }

    /**
     * @param status       实验状态，为空则不限定（用于"已处理"叠加范围时）
     * @param subjectIds   学科范围（教研员），与 createUserIds 二选一
     * @param createUserIds 提交者范围（校管理员），与 subjectIds 二选一
     * @param global       系统管理员全局，忽略上述范围
     */
    private Specification<ExpMsg> buildSpec(String status, String expType,
                                            Set<String> subjectIds, Set<String> createUserIds, boolean global) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(expType)) {
                predicates.add(cb.equal(root.get("expType"), expType.trim()));
            }
            if (!global) {
                if (createUserIds != null) {
                    // 校管理员：按本校提交者限定
                    if (createUserIds.isEmpty()) {
                        predicates.add(cb.disjunction());
                    } else {
                        predicates.add(root.get("createUserId").in(createUserIds));
                    }
                } else {
                    // 教研员：按所辖学科限定
                    if (subjectIds == null || subjectIds.isEmpty()) {
                        predicates.add(cb.disjunction());
                    } else {
                        predicates.add(root.get("subjectId").in(subjectIds));
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ResearcherExpAuditItemDto toItem(ExpMsg row, Map<String, String> subjectNames) {
        ResearcherExpAuditItemDto dto = new ResearcherExpAuditItemDto();
        dto.setExpId(row.getExpId());
        dto.setExpName(row.getExpName());
        dto.setExpType(row.getExpType());
        dto.setExpTypeLabel(expTypeLabel(row.getExpType()));
        dto.setSubjectId(row.getSubjectId());
        dto.setSubjectName(subjectNames.getOrDefault(safe(row.getSubjectId()), row.getSubjectId()));
        dto.setSubmitterName(resolveUserName(row.getCreateUserId()));
        Date time = row.getConfirmTime() != null ? row.getConfirmTime() : row.getUpdateTime();
        dto.setSubmitTime(time != null ? TIME_FMT.format(time) : "");
        dto.setStatus(row.getStatus());
        return dto;
    }

    private Map<String, String> loadSubjectNames(List<ExpMsg> rows) {
        Set<String> ids = rows.stream()
                .map(ExpMsg::getSubjectId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, String> map = new HashMap<>();
        for (DataSchoolSubject subject : subjectRepository.findAll()) {
            if (subject != null && ids.contains(subject.getSubjectId())) {
                map.put(subject.getSubjectId(), subject.getSubjectName());
            }
        }
        return map;
    }

    private String expTypeLabel(String expType) {
        String type = safe(expType).toLowerCase();
        if ("teach".equals(type) || "teaching".equals(type)) {
            return "教学实验";
        }
        if ("standard".equals(type)) {
            return "标准实验";
        }
        if ("student".equals(type)) {
            return "学生实验";
        }
        return StringUtils.hasText(expType) ? expType : "实验";
    }

    private String resolveUserName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "未知";
        }
        return sysUserRepository.findById(userId.trim())
                .map(u -> StringUtils.hasText(u.getUserNickName()) ? u.getUserNickName() : u.getUserName())
                .orElse("未知");
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
