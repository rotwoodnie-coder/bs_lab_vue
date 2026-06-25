import request from './request'

export function fetchGrowth(params = {}) {
  return request.get('/mobile/growth', {
    params: { childUserId: params.childUserId || undefined }
  })
}

export function saveGrowthPlan(payload) {
  return request.post('/mobile/growth/plan/save', payload)
}
