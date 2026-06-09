export function ensureSocialOk(res, defaultMsg = '操作失败，请稍后重试') {
  if (res?.code === 200) return true
  alert(res?.message || defaultMsg)
  return false
}

function pickBool(obj, ...keys) {
  if (!obj) return false
  for (const key of keys) {
    if (obj[key] !== undefined && obj[key] !== null) return !!obj[key]
  }
  return false
}

/** 解析社交 summary（兼容 liked / isLiked 等字段名） */
export function parseSocialSummary(data) {
  if (!data) {
    return {
      liked: false,
      collected: false,
      likeCount: 0,
      collectCount: 0,
      commentCount: 0
    }
  }
  return {
    liked: pickBool(data, 'liked', 'isLiked'),
    collected: pickBool(data, 'collected', 'isCollected'),
    likeCount: Number(data.likeCount ?? 0),
    collectCount: Number(data.collectCount ?? 0),
    commentCount: Number(data.commentCount ?? 0)
  }
}

/** 解析评论点赞结果 */
export function parseCommentReaction(data) {
  if (!data) return { liked: false, likeCount: 0 }
  return {
    liked: pickBool(data, 'liked', 'isLiked'),
    likeCount: Number(data.likeCount ?? 0)
  }
}
