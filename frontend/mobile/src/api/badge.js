import request from './request'

export function fetchBadges(params = {}) {
  return request.get('/mobile/badges', {
    params: { childUserId: params.childUserId || undefined }
  })
}
