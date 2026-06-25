import request from './request'

// ===== 勋章规则管理 =====
export function fetchBadgeDefinitions() {
  return request.get('/mobile/admin/badges')
}

export function saveBadgeDefinition(payload = {}) {
  return request.post('/mobile/admin/badges', payload)
}

export function deleteBadgeDefinition(badgeId) {
  return request.delete(`/mobile/admin/badges/${badgeId}`)
}

// ===== 每日答题配置 =====
export function fetchQuizConfig() {
  return request.get('/mobile/admin/quiz-config')
}

export function saveQuizConfig(payload = {}) {
  return request.post('/mobile/admin/quiz-config/save', payload)
}

// ===== 家长注册审核 =====
export function fetchParentRegistrations(params = {}) {
  return request.get('/mobile/admin/parent-registrations', {
    params: {
      keyword: params.keyword || undefined,
      status: params.status || undefined,
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10
    }
  })
}

export function auditParentRegistration(userId, action) {
  return request.patch(`/mobile/admin/parent-registrations/${userId}`, { action })
}

// ===== 学生作品审核（管理员，本校/全局） =====
export function fetchAdminWorkReviews(params = {}) {
  return request.get('/mobile/admin/work-reviews', {
    params: {
      page: params.page || 1,
      size: params.size || 20
    }
  })
}

export function submitAdminWorkReview(workId, payload = {}) {
  return request.post(`/mobile/admin/work-reviews/${workId}`, {
    result: payload.result,
    comment: payload.comment || ''
  })
}
