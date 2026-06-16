package com.xuanyue.exp.mobile.service;



import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.xuanyue.exp.exp.entity.ExpQuestion;

import com.xuanyue.exp.exp.entity.ExpQuestionSelect;

import com.xuanyue.exp.exp.repository.ExpQuestionRepository;

import com.xuanyue.exp.exp.repository.ExpQuestionSelectRepository;

import com.xuanyue.exp.mobile.dto.*;

import com.xuanyue.exp.mobile.entity.*;

import com.xuanyue.exp.mobile.repository.*;

import com.xuanyue.exp.mobile.support.MobileGrowthAccessSupport;
import com.xuanyue.exp.mobile.support.MobileGrowthFilterSupport;
import com.xuanyue.exp.mobile.support.MobileGrowthPlanSupport;
import com.xuanyue.exp.mobile.support.MobileIds;

import com.xuanyue.exp.mobile.support.MobileJsonUtils;

import com.xuanyue.exp.mobile.support.MobileParentAccessService;

import com.xuanyue.exp.mobile.support.MobileTextUtils;

import com.xuanyue.exp.mobile.support.MobileUserContext;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;



import java.sql.Date;

import java.text.SimpleDateFormat;

import java.time.LocalDate;

import java.time.ZoneId;

import java.time.format.DateTimeFormatter;

import java.util.*;

import java.util.stream.Collectors;



@Service

public class MobileLearningService {



    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final SimpleDateFormat SUBMIT_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");



    private final MbQuizRecordRepository quizRecordRepository;

    private final MobileQuizQuestionAllocator quizQuestionAllocator;

    private final MobileQuizConfigService quizConfigService;

    private final ExpQuestionRepository questionRepository;

    private final ExpQuestionSelectRepository questionSelectRepository;

    private final MbBadgeDefRepository badgeDefRepository;

    private final MbBadgeProgressRepository badgeProgressRepository;

    private final MbGrowthEventRepository growthEventRepository;

    private final MbGrowthPlanRepository growthPlanRepository;

    private final MbWorkRepository workRepository;

    private final MbTaskSubmissionRepository taskSubmissionRepository;

    private final com.xuanyue.exp.system.repository.SysUserRepository sysUserRepository;

    private final MobilePointsService pointsService;

    private final MobileBadgeGrantService badgeGrantService;

    private final MobileGrowthEventService growthEventService;

    private final MobileParentAccessService parentAccessService;

    private final MobileGrowthStatsService growthStatsService;

    private final MobileGrowthBackfillService growthBackfillService;

    private final MobileGrowthPlanSupport growthPlanSupport;



    private static final ZoneId GROWTH_ZONE = ZoneId.of("Asia/Shanghai");

    public MobileLearningService(MbQuizRecordRepository quizRecordRepository,

                                 MobileQuizQuestionAllocator quizQuestionAllocator,

                                 MobileQuizConfigService quizConfigService,

                                 ExpQuestionRepository questionRepository,

                                 ExpQuestionSelectRepository questionSelectRepository,

                                 MbBadgeDefRepository badgeDefRepository,

                                 MbBadgeProgressRepository badgeProgressRepository,

                                 MbGrowthEventRepository growthEventRepository,

                                 MbGrowthPlanRepository growthPlanRepository,

                                 MbWorkRepository workRepository,

                                 MbTaskSubmissionRepository taskSubmissionRepository,

                                 com.xuanyue.exp.system.repository.SysUserRepository sysUserRepository,

                                 MobilePointsService pointsService,

                                 MobileBadgeGrantService badgeGrantService,

                                 MobileGrowthEventService growthEventService,

                                 MobileParentAccessService parentAccessService,

                                 MobileGrowthStatsService growthStatsService,

                                 MobileGrowthBackfillService growthBackfillService,

                                 MobileGrowthPlanSupport growthPlanSupport) {

        this.quizRecordRepository = quizRecordRepository;

        this.quizQuestionAllocator = quizQuestionAllocator;

        this.quizConfigService = quizConfigService;

        this.questionRepository = questionRepository;

        this.questionSelectRepository = questionSelectRepository;

        this.badgeDefRepository = badgeDefRepository;

        this.badgeProgressRepository = badgeProgressRepository;

        this.growthEventRepository = growthEventRepository;

        this.growthPlanRepository = growthPlanRepository;

        this.workRepository = workRepository;

        this.taskSubmissionRepository = taskSubmissionRepository;

        this.sysUserRepository = sysUserRepository;

        this.pointsService = pointsService;

        this.badgeGrantService = badgeGrantService;

        this.growthEventService = growthEventService;

        this.parentAccessService = parentAccessService;

        this.growthStatsService = growthStatsService;

        this.growthBackfillService = growthBackfillService;

        this.growthPlanSupport = growthPlanSupport;

    }



    public MobileQuizDto getTodayQuiz(String userId) {

        String uid = MobileUserContext.resolveStudentId(userId);

        LocalDate today = LocalDate.now();

        Date todaySql = Date.valueOf(today);



        MbQuizConfig config = quizConfigService.getEffectiveConfig();

        int configuredCount = config.getQuestionsPerDay();

        int basePoints = config.getBasePoints();

        int streakBonus = config.getStreakBonus();

        boolean enabled = quizConfigService.isEnabled();



        MobileQuizDto dto = new MobileQuizDto();

        dto.setHistory(buildHistory(uid));

        dto.setQuestionsPerDay(configuredCount);

        dto.setBasePoints(basePoints);

        dto.setBonusPoints(streakBonus);

        dto.setEnabled(enabled);

        dto.setStreakDays(countStreakBeforeToday(uid, today));



        if (!enabled) {

            dto.setReady(false);

            dto.setMessage("每日答题功能已暂停，请稍后再试");

            dto.setQuestions(Collections.emptyList());

            attachTodayRecord(dto, uid, todaySql);

            return dto;

        }



        List<String> questionIds = resolveTodayQuestionIds(uid, today, todaySql, configuredCount);

        if (questionIds.isEmpty()) {

            dto.setReady(false);

            dto.setMessage("题库暂无可用题目，请联系老师维护题库");

            dto.setQuestions(Collections.emptyList());

            attachTodayRecord(dto, uid, todaySql);

            return dto;

        }



        dto.setQuestionsPerDay(questionIds.size());

        List<MobileQuizDto.Question> questions = new ArrayList<>();

        for (int i = 0; i < questionIds.size(); i++) {

            Optional<MobileQuizDto.Question> questionOpt = buildQuestion(questionIds.get(i), i + 1);

            if (!questionOpt.isPresent()) {

                dto.setReady(false);

                dto.setMessage("今日题目加载失败，请稍后重试");

                dto.setQuestions(Collections.emptyList());

                attachTodayRecord(dto, uid, todaySql);

                return dto;

            }

            questions.add(questionOpt.get());

        }



        dto.setReady(true);

        dto.setQuestions(questions);

        attachTodayRecord(dto, uid, todaySql);

        return dto;

    }



    public Optional<MobileQuizDto.TodayResult> getRecordByDate(String userId, LocalDate quizDate) {

        if (quizDate == null) {

            return Optional.empty();

        }

        String uid = MobileUserContext.resolveStudentId(userId);

        return quizRecordRepository.findByUserIdAndQuizDate(uid, Date.valueOf(quizDate))

                .map(record -> toTodayResult(record, countStreakIncludingToday(uid, Date.valueOf(quizDate))));

    }



    public Optional<List<MobileQuizReviewDto>> getReview(String userId, LocalDate quizDate) {

        if (quizDate == null) {

            return Optional.empty();

        }

        String uid = MobileUserContext.resolveStudentId(userId);

        Optional<MbQuizRecord> recordOpt = quizRecordRepository.findByUserIdAndQuizDate(uid, Date.valueOf(quizDate));

        if (!recordOpt.isPresent()) {

            return Optional.empty();

        }

        MbQuizRecord record = recordOpt.get();

        if ("y".equalsIgnoreCase(record.getPerfect())) {

            return Optional.empty();

        }



        List<String> questionIds = parseQuestionIds(record);

        List<Integer> answers = MobileJsonUtils.parseIntegerList(record.getAnswersJson());

        if (questionIds.isEmpty()) {

            return Optional.empty();

        }



        List<MobileQuizReviewDto> items = new ArrayList<>();

        for (int i = 0; i < questionIds.size(); i++) {

            String questionId = questionIds.get(i);

            int correctIndex = resolveCorrectAnswerIndex(questionId);

            Integer chosen = i < answers.size() ? answers.get(i) : null;

            int userIndex = chosen == null ? -1 : chosen;

            if (userIndex == correctIndex) {

                continue;

            }

            Optional<MobileQuizReviewDto> reviewOpt = buildReviewItem(record, questionId, userIndex, correctIndex, i + 1);

            reviewOpt.ifPresent(items::add);

        }

        return items.isEmpty() ? Optional.empty() : Optional.of(items);

    }



    @Transactional
    public QuizSubmitResultDto submitQuiz(String userId, QuizSubmitRequest request) {

        boolean practice = request != null && request.isPractice();

        List<Integer> answers = request != null && request.getAnswers() != null

                ? request.getAnswers()

                : Collections.emptyList();



        String uid = MobileUserContext.resolveStudentId(userId);

        LocalDate today = LocalDate.now();

        Date todaySql = Date.valueOf(today);



        if (!practice && !quizConfigService.isEnabled()) {

            throw new IllegalArgumentException("每日答题功能已暂停");

        }



        int configuredCount = quizConfigService.getQuestionsPerDay();

        List<String> questionIds = resolveTodayQuestionIds(uid, today, todaySql, configuredCount);

        if (questionIds.isEmpty()) {

            throw new IllegalArgumentException("题库暂无可用题目，暂无法提交");

        }

        int total = questionIds.size();

        if (answers.size() != total) {

            throw new IllegalArgumentException("请完成全部 " + total + " 题后再提交");

        }

        for (int i = 0; i < total; i++) {

            if (answers.get(i) == null) {

                throw new IllegalArgumentException("第 " + (i + 1) + " 题尚未选择答案");

            }

        }



        int score = 0;

        for (int i = 0; i < total; i++) {

            int correctIndex = resolveCorrectAnswerIndex(questionIds.get(i));

            if (Objects.equals(answers.get(i), correctIndex)) {

                score++;

            }

        }

        boolean perfect = score == total;

        int previousStreak = countStreakBeforeToday(uid, today);

        int streakDays = perfect ? previousStreak + 1 : 0;

        int streakBonus = quizConfigService.getStreakBonus();

        int points = practice ? 0 : calcPoints(perfect, previousStreak, quizConfigService.getBasePoints(), streakBonus);



        QuizSubmitResultDto result = new QuizSubmitResultDto();

        result.setScore(score);

        result.setTotal(total);

        result.setPoints(points);

        result.setPerfect(perfect);

        result.setResultType(resolveResultType(score, total, practice));

        result.setStreakDays(streakDays);



        if (practice) {

            return result;

        }



        Optional<MbQuizRecord> existingOpt = quizRecordRepository.findByUserIdAndQuizDate(uid, todaySql);

        if (existingOpt.isPresent()) {

            MbQuizRecord existing = existingOpt.get();

            if (isQuizSubmissionComplete(existing)) {

                return toSubmitResult(existing, uid, todaySql);

            }

            throw new IllegalStateException("今日已提交过答卷");

        }



        MbQuizRecord record = new MbQuizRecord();

        record.setRecordId(MobileIds.newId("qr"));

        record.setUserId(uid);

        record.setQuestionId(questionIds.get(0));

        record.setQuestionIdsJson(MobileJsonUtils.toJson(questionIds));

        record.setQuizDate(todaySql);

        record.setScore(score);

        record.setTotal(total);

        record.setPoints(points);

        record.setPerfect(perfect ? "y" : "n");

        record.setAnswersJson(MobileJsonUtils.toJson(answers));

        record.setCreateTime(new java.util.Date());

        try {

            quizRecordRepository.saveAndFlush(record);

        } catch (DataIntegrityViolationException ex) {

            MbQuizRecord saved = quizRecordRepository.findByUserIdAndQuizDate(uid, todaySql)

                    .orElseThrow(() -> new IllegalStateException("今日已提交过答卷"));

            if (isQuizSubmissionComplete(saved)) {

                return toSubmitResult(saved, uid, todaySql);

            }

            throw new IllegalStateException("今日已提交过答卷");

        }

        if (points > 0) {

            pointsService.credit(uid, points, "quiz", record.getRecordId(), "每日答题");

        }

        growthEventService.onQuizSubmitted(uid, record, points);

        badgeGrantService.onQuizSubmitted(uid, record);



        return result;

    }



    /** 正式答卷已写入（有作答 JSON 或完整计分字段） */

    private boolean isQuizSubmissionComplete(MbQuizRecord record) {

        if (record == null) {

            return false;

        }

        if (StringUtils.hasText(record.getAnswersJson())) {

            List<Integer> parsed = MobileJsonUtils.parseIntegerList(record.getAnswersJson());

            if (!parsed.isEmpty()) {

                return true;

            }

        }

        return record.getTotal() != null && record.getTotal() > 0

                && record.getScore() != null

                && StringUtils.hasText(record.getQuestionIdsJson());

    }



    private QuizSubmitResultDto toSubmitResult(MbQuizRecord record, String uid, Date todaySql) {

        QuizSubmitResultDto dto = new QuizSubmitResultDto();

        int score = record.getScore() != null ? record.getScore() : 0;

        int total = record.getTotal() != null ? record.getTotal() : 0;

        dto.setScore(score);

        dto.setTotal(total);

        dto.setPoints(record.getPoints() != null ? record.getPoints() : 0);

        dto.setPerfect("y".equalsIgnoreCase(record.getPerfect()));

        dto.setResultType(resolveResultType(score, total, false));

        dto.setStreakDays(countStreakIncludingToday(uid, todaySql));

        return dto;

    }



    private List<String> resolveTodayQuestionIds(String uid, LocalDate today, Date todaySql, int configuredCount) {

        Optional<MbQuizRecord> recordOpt = quizRecordRepository.findByUserIdAndQuizDate(uid, todaySql);

        if (recordOpt.isPresent()) {

            List<String> fromRecord = parseQuestionIds(recordOpt.get());

            if (!fromRecord.isEmpty()) {

                return fromRecord;

            }

        }

        return quizQuestionAllocator.resolveQuestionIds(uid, today, configuredCount);

    }



    private List<String> parseQuestionIds(MbQuizRecord record) {

        if (record == null) {

            return Collections.emptyList();

        }

        List<String> ids = MobileJsonUtils.parseStringList(record.getQuestionIdsJson());

        if (!ids.isEmpty()) {

            return ids;

        }

        if (StringUtils.hasText(record.getQuestionId())) {

            return Collections.singletonList(record.getQuestionId().trim());

        }

        return Collections.emptyList();

    }



    private Optional<MobileQuizReviewDto> buildReviewItem(MbQuizRecord record, String questionId,

                                                          int userIndex, int correctIndex, int questionNo) {

        Optional<ExpQuestion> questionOpt = questionRepository.findById(questionId.trim());

        if (!questionOpt.isPresent()) {

            return Optional.empty();

        }

        List<ExpQuestionSelect> selects = questionSelectRepository

                .findByQuestionIdOrderBySortOrderAscSelectIdAsc(questionId);

        if (selects == null || selects.isEmpty()) {

            return Optional.empty();

        }

        List<String> options = selects.stream()

                .map(s -> MobileTextUtils.toPlainOneLine(s.getSelectContent(), 300))

                .filter(StringUtils::hasText)

                .collect(Collectors.toList());

        if (options.isEmpty()) {

            return Optional.empty();

        }



        MobileQuizReviewDto review = new MobileQuizReviewDto();

        review.setDate(record.getQuizDate().toString());

        review.setQuestionNo(questionNo);

        ExpQuestion question = questionOpt.get();

        review.setTitle(MobileTextUtils.toPlainOneLine(question.getQuestionContent(), 500));

        review.setMeta(buildQuestionMeta(question));

        review.setOptions(options);

        review.setCorrectAnswerIndex(correctIndex);

        review.setCorrectAnswerText(optionAt(options, correctIndex));

        review.setUserAnswerIndex(userIndex);

        review.setUserAnswerText(userIndex >= 0 ? optionAt(options, userIndex) : "未作答");

        return Optional.of(review);

    }



    private String optionAt(List<String> options, int index) {

        if (index < 0 || index >= options.size()) {

            return "—";

        }

        return options.get(index);

    }



    private void attachTodayRecord(MobileQuizDto dto, String uid, Date todaySql) {

        Optional<MbQuizRecord> recordOpt = quizRecordRepository.findByUserIdAndQuizDate(uid, todaySql);

        if (!recordOpt.isPresent()) {

            dto.setSubmittedToday(false);

            return;

        }

        MbQuizRecord record = recordOpt.get();

        dto.setSubmittedToday(true);

        dto.setTodayResult(toTodayResult(record, countStreakIncludingToday(uid, todaySql)));

    }



    private MobileQuizDto.TodayResult toTodayResult(MbQuizRecord record, int streakDays) {

        MobileQuizDto.TodayResult result = new MobileQuizDto.TodayResult();

        int score = record.getScore() != null ? record.getScore() : 0;

        int total = record.getTotal() != null ? record.getTotal() : 1;

        result.setScore(score);

        result.setTotal(total);

        result.setPoints(record.getPoints() != null ? record.getPoints() : 0);

        result.setPerfect("y".equalsIgnoreCase(record.getPerfect()));

        result.setResultType(result.isPerfect() ? "perfect" : "low");

        result.setStreakDays(streakDays);

        result.setWrongCount(Math.max(0, total - score));

        if (record.getCreateTime() != null) {

            result.setSubmitTime(SUBMIT_TIME_FMT.format(record.getCreateTime()));

        }

        return result;

    }



    private List<MobileQuizDto.HistoryItem> buildHistory(String uid) {

        List<MbQuizRecord> records = quizRecordRepository.findByUserIdOrderByQuizDateDesc(uid);

        if (records.isEmpty()) {

            return Collections.emptyList();

        }

        return records.stream().map(r -> {

            MobileQuizDto.HistoryItem h = new MobileQuizDto.HistoryItem();

            h.setDate(r.getQuizDate().toString());

            h.setScore(r.getScore() + "/" + r.getTotal());

            h.setPoints("+" + r.getPoints());

            h.setPerfect("y".equalsIgnoreCase(r.getPerfect()));

            return h;

        }).collect(Collectors.toList());

    }



    private Optional<MobileQuizDto.Question> buildQuestion(String questionId, int displayId) {

        if (!StringUtils.hasText(questionId)) {

            return Optional.empty();

        }

        Optional<ExpQuestion> questionOpt = questionRepository.findById(questionId.trim());

        if (!questionOpt.isPresent() || !"y".equalsIgnoreCase(questionOpt.get().getStatus())) {

            return Optional.empty();

        }

        ExpQuestion question = questionOpt.get();

        List<ExpQuestionSelect> selects = questionSelectRepository

                .findByQuestionIdOrderBySortOrderAscSelectIdAsc(question.getQuestionId());

        if (selects == null || selects.isEmpty()) {

            return Optional.empty();

        }



        MobileQuizDto.Question dto = new MobileQuizDto.Question();

        dto.setId(displayId);

        dto.setQuestionId(question.getQuestionId());

        dto.setTitle(MobileTextUtils.toPlainOneLine(question.getQuestionContent(), 500));

        dto.setMeta(buildQuestionMeta(question));

        dto.setOptions(selects.stream()

                .map(s -> MobileTextUtils.toPlainOneLine(s.getSelectContent(), 300))

                .filter(StringUtils::hasText)

                .collect(Collectors.toList()));

        if (dto.getOptions().isEmpty() || !StringUtils.hasText(dto.getTitle())) {

            return Optional.empty();

        }

        return Optional.of(dto);

    }



    private String buildQuestionMeta(ExpQuestion question) {

        if (StringUtils.hasText(question.getKnowledgeContent())) {

            return question.getKnowledgeContent().trim();

        }

        return "每日答题";

    }



    private int resolveCorrectAnswerIndex(String questionId) {

        List<ExpQuestionSelect> selects = questionSelectRepository

                .findByQuestionIdOrderBySortOrderAscSelectIdAsc(questionId);

        for (int i = 0; i < selects.size(); i++) {

            if ("y".equalsIgnoreCase(selects.get(i).getIsRight())) {

                return i;

            }

        }

        return 0;

    }



    private int calcPoints(boolean perfect, int previousStreak, int basePoints, int bonusPoints) {

        if (!perfect) {

            return 0;

        }

        int points = Math.max(basePoints, 0);

        if (previousStreak > 0) {

            points += Math.max(bonusPoints, 0);

        }

        return points;

    }



    private int countStreakBeforeToday(String uid, LocalDate today) {

        int streak = 0;

        LocalDate cursor = today.minusDays(1);

        while (true) {

            Optional<MbQuizRecord> recordOpt = quizRecordRepository.findByUserIdAndQuizDate(uid, Date.valueOf(cursor));

            if (!recordOpt.isPresent() || !"y".equalsIgnoreCase(recordOpt.get().getPerfect())) {

                break;

            }

            streak++;

            cursor = cursor.minusDays(1);

        }

        return streak;

    }



    private int countStreakIncludingToday(String uid, Date todaySql) {

        Optional<MbQuizRecord> todayOpt = quizRecordRepository.findByUserIdAndQuizDate(uid, todaySql);

        if (!todayOpt.isPresent() || !"y".equalsIgnoreCase(todayOpt.get().getPerfect())) {

            return 0;

        }

        LocalDate today = todaySql.toLocalDate();

        int streak = 1;

        LocalDate cursor = today.minusDays(1);

        while (true) {

            Optional<MbQuizRecord> recordOpt = quizRecordRepository.findByUserIdAndQuizDate(uid, Date.valueOf(cursor));

            if (!recordOpt.isPresent() || !"y".equalsIgnoreCase(recordOpt.get().getPerfect())) {

                break;

            }

            streak++;

            cursor = cursor.minusDays(1);

        }

        return streak;

    }



    private String resolveResultType(int score, int total, boolean practice) {

        if (practice) {

            return "practice";

        }

        if (score == total) {

            return "perfect";

        }

        return "low";

    }



    public MobileBadgeWallDto getBadges(String userId, String childUserId) {

        String uid = parentAccessService.resolveStudentScope(userId, childUserId);

        List<MbBadgeDef> defs = badgeDefRepository.findByStatusOrderBySortOrderAsc("y");

        if (defs.isEmpty()) {

            MobileBadgeWallDto empty = new MobileBadgeWallDto();

            empty.setItems(Collections.emptyList());

            empty.setEarned(0);

            empty.setTotal(0);

            empty.setProgressPercent(0);

            empty.setProgressHint("");

            return empty;

        }

        List<MbBadgeProgress> progressList = badgeProgressRepository.findByUserId(uid);

        Map<String, MbBadgeProgress> progressMap = new HashMap<>();

        for (MbBadgeProgress p : progressList) {

            progressMap.put(p.getBadgeId(), p);

        }

        MobileBadgeWallDto dto = new MobileBadgeWallDto();

        List<MobileBadgeWallDto.BadgeItem> items = new ArrayList<>();

        int earned = 0;

        for (MbBadgeDef def : defs) {

            MobileBadgeWallDto.BadgeItem item = new MobileBadgeWallDto.BadgeItem();

            item.setId(def.getBadgeId());

            item.setIcon(def.getIcon());

            item.setTitle(def.getTitle());

            item.setDesc(def.getDescription());

            item.setAction(normalizeBadgeAction(def));

            MbBadgeProgress prog = progressMap.get(def.getBadgeId());

            boolean isEarned = prog != null && "y".equalsIgnoreCase(prog.getEarned());

            item.setEarned(isEarned);

            if (isEarned) {

                earned++;

            } else {

                String progressLabel = badgeGrantService.buildProgressLabel(uid, def, prog, false);

                if (StringUtils.hasText(progressLabel)) {

                    item.setProgress(progressLabel);

                }

            }

            items.add(item);

        }

        dto.setItems(items);

        dto.setEarned(earned);

        dto.setTotal(defs.size());

        dto.setProgressPercent(defs.isEmpty() ? 0 : earned * 100.0 / defs.size());

        dto.setProgressHint(badgeGrantService.buildProgressHint(uid, defs, progressMap));

        return dto;

    }



    private String normalizeBadgeAction(MbBadgeDef def) {

        if (def == null) {

            return null;

        }

        if (StringUtils.hasText(def.getActionRoute())) {

            String route = def.getActionRoute().trim();

            return route.startsWith("/") ? route : "/" + route;

        }

        String type = def.getCriteriaType() != null ? def.getCriteriaType().trim().toLowerCase() : "";

        if (type.startsWith("quiz")) {

            return "/quiz";

        }

        return null;

    }



    public MobileGrowthDto getGrowth(String userId, String childUserId) {

        String uid = parentAccessService.resolveStudentScope(userId, childUserId);

        boolean isParent = parentAccessService.isParentUser(userId);

        MobileGrowthDto.Plan plan = growthPlanSupport.resolvePlan(uid);

        MobileGrowthDto dto = new MobileGrowthDto();

        MobileGrowthDto.Access access = new MobileGrowthDto.Access();

        access.setCanEditPlan(!isParent);

        dto.setPlan(plan);

        if (isParent && MobileGrowthAccessSupport.isSelfOnly(plan.getVisibilityKey())) {

            access.setCanViewDetail(false);

            access.setRestrictedReason("孩子已将成长档案设为仅本人可见");

            dto.setAccess(access);

            dto.setStats(growthStatsService.emptyStats());

            dto.setTimeline(Collections.emptyList());

            return dto;

        }

        access.setCanViewDetail(true);

        dto.setAccess(access);

        growthBackfillService.syncMissingEvents(uid);

        List<MbGrowthEvent> allEvents = growthEventRepository.findByUserIdOrderBySortTimeDesc(uid);

        List<MbGrowthEvent> events = MobileGrowthFilterSupport.filterEvents(
                allEvents, plan.getContentKeys(), plan.getRangeKey());

        dto.setStats(growthStatsService.computeStats(uid, plan.getContentKeys(), plan.getRangeKey()));

        dto.setTimeline(events.stream().map(this::toTimelineItem).collect(Collectors.toList()));

        if (!isParent) {

            dto.setNudge(buildGrowthNudge(uid));

        }

        return dto;

    }



    private MobileGrowthDto.TimelineItem toTimelineItem(MbGrowthEvent e) {

        MobileGrowthDto.TimelineItem t = new MobileGrowthDto.TimelineItem();

        t.setTime(e.getEventTimeLabel());

        t.setGroupLabel(formatGroupLabel(e.getSortTime()));

        t.setEmoji(e.getEmoji());

        t.setTitle(e.getTitle());

        t.setHint(e.getHint());

        t.setBadge(e.getPointsLabel());

        t.setDot(e.getDotClass());

        t.setBadgeClass(e.getBadgeClass());

        applyTimelineLink(t, e);

        if (e.getBadgesJson() != null) {

            try {

                t.setBadges(MAPPER.readValue(e.getBadgesJson(), new TypeReference<List<String>>() {}));

            } catch (Exception ignored) {

            }

        }

        return t;

    }



    private void applyTimelineLink(MobileGrowthDto.TimelineItem item, MbGrowthEvent event) {

        if (event == null || !StringUtils.hasText(event.getSourceType())) {

            return;

        }

        String type = event.getSourceType().trim().toLowerCase();

        String sourceId = event.getSourceId() != null ? event.getSourceId().trim() : "";

        switch (type) {

            case "quiz":

                item.setLinkType("quiz");

                item.setLinkId(sourceId);

                break;

            case "work":

                item.setLinkType("work");

                item.setLinkId(workLinkId(sourceId));

                break;

            case "task":

                item.setLinkType("task");

                if (StringUtils.hasText(sourceId)) {

                    taskSubmissionRepository.findById(sourceId)

                            .ifPresent(sub -> item.setLinkId(sub.getTaskId()));

                }

                break;

            case "badge":

                item.setLinkType("badge");

                break;

            default:

                break;

        }

    }



    private String workLinkId(String sourceId) {

        if (!StringUtils.hasText(sourceId)) {

            return null;

        }

        int idx = sourceId.indexOf(':');

        return idx > 0 ? sourceId.substring(0, idx) : sourceId;

    }



    private String formatGroupLabel(java.util.Date sortTime) {

        if (sortTime == null) {

            return "更早";

        }

        LocalDate day = sortTime.toInstant().atZone(GROWTH_ZONE).toLocalDate();

        return day.format(DateTimeFormatter.ofPattern("yyyy年M月"));

    }



    private MobileGrowthDto.Nudge buildGrowthNudge(String uid) {

        MobileGrowthDto.Nudge nudge = new MobileGrowthDto.Nudge();

        int questionsPerDay = quizConfigService.getQuestionsPerDay();

        nudge.setQuestionsPerDay(questionsPerDay);

        LocalDate today = LocalDate.now();

        Date todaySql = Date.valueOf(today);

        Optional<MbQuizRecord> recordOpt = quizRecordRepository.findByUserIdAndQuizDate(uid, todaySql);

        if (recordOpt.isPresent()) {

            MbQuizRecord record = recordOpt.get();

            nudge.setQuizSubmittedToday(true);

            int streak = countStreakIncludingToday(uid, todaySql);

            nudge.setStreakDays(streak);

            int score = record.getScore() != null ? record.getScore() : 0;

            int total = record.getTotal() != null ? record.getTotal() : questionsPerDay;

            nudge.setText("今日已完成 " + score + "/" + total + " · 连对 " + streak + " 天");

        } else if (!quizConfigService.isEnabled()) {

            nudge.setQuizSubmittedToday(false);

            nudge.setStreakDays(0);

            nudge.setText("每日答题功能已暂停");

        } else {

            nudge.setQuizSubmittedToday(false);

            nudge.setStreakDays(countStreakBeforeToday(uid, today));

            nudge.setText("今日 " + questionsPerDay + " 题待完成");

        }

        return nudge;

    }



    public MobileGrowthDto.Plan saveGrowthPlan(String userId, GrowthPlanSaveRequest request) {

        if (request == null) {

            throw new IllegalArgumentException("请求不能为空");

        }

        String uid = MobileUserContext.resolveStudentId(userId);

        List<String> keys = request.getContentKeys() != null && !request.getContentKeys().isEmpty()

                ? request.getContentKeys()

                : Arrays.asList("exp", "work", "badge", "quiz");

        String visibility = StringUtils.hasText(request.getVisibility()) ? request.getVisibility().trim() : "parent";

        String range = StringUtils.hasText(request.getRange()) ? request.getRange().trim() : "term";



        MbGrowthPlan plan = growthPlanRepository.findById(uid).orElseGet(MbGrowthPlan::new);

        plan.setUserId(uid);

        try {

            plan.setContentKeysJson(MAPPER.writeValueAsString(keys));

        } catch (Exception e) {

            plan.setContentKeysJson("[\"exp\",\"work\",\"badge\",\"quiz\"]");

        }

        plan.setVisibility(visibility);

        plan.setRange(range);

        plan.setUpdateTime(new java.util.Date());

        growthPlanRepository.save(plan);

        return growthPlanSupport.toPlanDto(plan);

    }

}
