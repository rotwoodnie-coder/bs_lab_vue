/**
 * 消息通知跳转：仅在有明确目标时返回路由，避免误链到 /exp/:id。
 * @returns {string|null}
 */
export function resolveNotificationLink(item) {
  if (!item) return null
  if (item.linkRoute) return item.linkRoute

  const type = (item.type || '').toLowerCase()
  const linkId = item.linkId

  if (type === 'bind') return '/parent-binds'
  if (type === 'task' && linkId) return `/tasks/${linkId}`
  if (type === 'grade' && linkId) return `/works/${linkId}`
  if (type === 'achievement') return '/badges'
  if (type === 'social' && linkId) return `/works/${linkId}`
  if (linkId && ['exp', 'content', 'video'].includes(type)) return `/exp/${linkId}`

  return null
}

export function notificationLinkLabel(item) {
  if (!item) return '查看详情'
  if (item.linkRoute === '/parent-binds' || (item.type || '').toLowerCase() === 'bind') {
    return '前往绑定审核'
  }
  const type = (item.type || '').toLowerCase()
  if (type === 'grade') return '查看作品详情'
  if (type === 'task') return '查看任务详情'
  if (type === 'achievement') return '查看勋章'
  if (type === 'social') return '查看互动内容'
  return '查看关联内容'
}

/** 列表页：无明确外链时进入消息详情 */
export function resolveNotificationListLink(item) {
  if (item?.kind === 'notice') return `/notifications/notice/${item.id}`
  const action = resolveNotificationLink(item)
  if (action) return action
  return `/notifications/${item.id}`
}
