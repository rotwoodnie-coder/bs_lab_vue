package com.xuanyue.exp.mobile.controller;

import com.xuanyue.exp.common.ApiResponse;
import com.xuanyue.exp.common.PageResult;
import com.xuanyue.exp.mobile.dto.HomeBootstrapDto;
import com.xuanyue.exp.mobile.dto.HomeFeedItem;
import com.xuanyue.exp.mobile.dto.NoticeDto;
import com.xuanyue.exp.mobile.dto.ParentDashboardDto;
import com.xuanyue.exp.mobile.dto.SubjectItem;
import com.xuanyue.exp.mobile.service.MobileHomeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 移动端首页 Controller
 * 路径: /api/mobile/home/*
 */
@RestController
@RequestMapping("/api/mobile/home")
public class MobileHomeController {

    private final MobileHomeService homeService;

    public MobileHomeController(MobileHomeService homeService) {
        this.homeService = homeService;
    }

    /**
     * 首页首屏聚合（feed + 公告 + 未读数）
     * GET /api/mobile/home/bootstrap?gradeKey=all&size=12
     */
    @GetMapping("/bootstrap")
    public ApiResponse<HomeBootstrapDto> bootstrap(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(defaultValue = "all") String gradeKey,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.success(homeService.getBootstrap(userId, gradeKey, size));
    }

    /**
     * 获取首页信息流（分页）
     * GET /api/mobile/home/feed?gradeKey=all&page=1&size=20
     */
    @GetMapping("/feed")
    public ApiResponse<PageResult<HomeFeedItem>> getFeed(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "childUserId", required = false) String childUserId,
            @RequestParam(defaultValue = "all") String gradeKey,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<HomeFeedItem> result = homeService.getFeed(userId, childUserId, gradeKey, page, size);
        return ApiResponse.success(result);
    }

    /**
     * 搜索实验（与 feed 相同卡片数据结构）
     * GET /api/mobile/home/search?keyword=xxx&page=1&size=20
     */
    @GetMapping("/search")
    public ApiResponse<PageResult<HomeFeedItem>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(homeService.searchFeed(keyword, page, size));
    }

    /**
     * 搜索热词（最近发布的实验名称）
     * GET /api/mobile/home/hot-keywords?limit=8
     */
    @GetMapping("/hot-keywords")
    public ApiResponse<List<String>> hotKeywords(
            @RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.success(homeService.getHotKeywords(limit));
    }

    /**
     * 浏览统计（已发布实验数、模拟实验数）
     * GET /api/mobile/home/browse-stats
     */
    @GetMapping("/browse-stats")
    public ApiResponse<Map<String, Long>> browseStats() {
        return ApiResponse.success(homeService.getBrowseStats());
    }

    /**
     * 获取年级筛选列表（全部、1-2年级、3-4年级、5-6年级）
     * GET /api/mobile/home/grade-filters
     */
    @GetMapping("/grade-filters")
    public ApiResponse<List<SubjectItem>> getGradeFilters() {
        List<SubjectItem> filters = homeService.getGradeFilters();
        return ApiResponse.success(filters);
    }

    /**
     * 获取最新公告通知
     * GET /api/mobile/home/notices/latest
     */
    @GetMapping("/notices/latest")
    public ApiResponse<NoticeDto> getLatestNotice(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        NoticeDto notice = homeService.getLatestNotice(userId);
        return ApiResponse.success(notice);
    }

    @GetMapping("/notices/read-ids")
    public ApiResponse<List<String>> getNoticeReadIds(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success(homeService.listReadNoticeIds(userId));
    }

    @PostMapping("/notices/{noticeId}/read")
    public ApiResponse<Void> markNoticeRead(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String noticeId) {
        homeService.markNoticeRead(userId, noticeId);
        return ApiResponse.success(null);
    }

    /**
     * 获取家长首页仪表盘
     * GET /api/mobile/home/parent-dashboard
     */
    @GetMapping("/parent-dashboard")
    public ApiResponse<ParentDashboardDto> getParentDashboard(
            @RequestHeader("X-User-Id") String userId) {
        ParentDashboardDto dashboard = homeService.getParentDashboard(userId);
        return ApiResponse.success(dashboard);
    }
}
