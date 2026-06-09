import request from './request'

export function fetchSocialSummary(targetType, targetId) {
  return request.get('/mobile/social/summary', {
    params: { targetType, targetId }
  })
}

export function fetchComments(targetType, targetId) {
  return request.get('/mobile/social/comments', {
    params: { targetType, targetId }
  })
}

export function createComment(payload) {
  return request.post('/mobile/social/comments', payload)
}

export function toggleReaction(payload) {
  return request.post('/mobile/social/reactions/toggle', payload)
}

export function toggleCommentLike(commentId) {
  return request.post(`/mobile/social/comments/${encodeURIComponent(commentId)}/like/toggle`)
}
