import request from './request'

/**
 * 获取首页信息流（按年级筛选）
 * @param {object} params - { schoolLevelId, page, size }
 * @returns {Promise<{total: number, records: Array}>}
 */
export function fetchHomeFeed(params = {}) {
  return request.get('/mobile/home/feed', {
    params: {
      gradeKey: params.gradeKey || 'all',
      page: params.page || 1,
      size: params.size || 20
    }
  })
}

/**
 * 搜索实验（与首页 feed 相同数据结构）
 */
export function fetchHomeSearch(params = {}) {
  return request.get('/mobile/home/search', {
    params: {
      keyword: params.keyword,
      page: params.page || 1,
      size: params.size || 20
    }
  })
}

/**
 * 搜索热词（最近发布的实验名称）
 */
export function fetchHotKeywords(limit = 8) {
  return request.get('/mobile/home/hot-keywords', { params: { limit } })
}

/**
 * 浏览统计（已发布实验数、模拟实验数）
 */
export function fetchBrowseStats() {
  return request.get('/mobile/home/browse-stats')
}

/**
 * 获取年级筛选列表（全部、1-2年级、3-4年级、5-6年级）
 * @returns {Promise<Array<{key: string, label: string}>>}
 */
export function fetchGradeFilters() {
  return request.get('/mobile/home/grade-filters')
}

/**
 * 获取最新公告通知
 * @returns {Promise<{id: string, title: string, body: string, date: string, type: string, badge: string, hasUnread: boolean}|null>}
 */
export function fetchLatestNotice() {
  return request.get('/mobile/home/notices/latest')
}

export function fetchNoticeReadIds() {
  return request.get('/mobile/home/notices/read-ids')
}

export function markNoticeRead(noticeId) {
  return request.post(`/mobile/home/notices/${noticeId}/read`)
}

/**
 * 获取家长首页仪表盘
 * @returns {Promise<{children, todayProgress, recentActivities, activitiesByChild, quizTodayByChild}>}
 */
export function fetchParentDashboard() {
  return request.get('/mobile/home/parent-dashboard')
}
