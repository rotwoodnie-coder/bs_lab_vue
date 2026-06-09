export function normalizeBadgeWallData(raw) {
  const data = raw && typeof raw === 'object' ? { ...raw } : {}
  const items = Array.isArray(data.items) ? data.items.map(normalizeBadgeItem) : []
  const earned = Number(data.earned ?? items.filter((b) => b.earned).length)
  const total = Number(data.total ?? items.length)
  const progressPercent = Math.min(100, Math.max(0, Number(data.progressPercent ?? (total ? (earned * 100) / total : 0))))
  return {
    earned,
    total,
    progressPercent,
    progressHint: data.progressHint || (total ? `已解锁 ${earned} 枚 · 共 ${total} 枚勋章` : ''),
    items
  }
}

function normalizeBadgeItem(item) {
  const earned = Boolean(item?.earned)
  let action = item?.action
  if (action && !String(action).startsWith('/')) {
    action = `/${action}`
  }
  return {
    id: item?.id || item?.badgeId || '',
    icon: item?.icon || '🏅',
    title: item?.title || '勋章',
    desc: item?.desc || item?.description || '',
    earned,
    progress: earned ? '已获得' : (item?.progress || '未开始'),
    action: earned ? null : (action || null)
  }
}

/** API 成功且含 data 时规范化；否则返回空结构（不注入假数据） */
export function mergeBadgeWallResponse(res) {
  if (res?.code === 200 && res.data) {
    return normalizeBadgeWallData(res.data)
  }
  return normalizeBadgeWallData({ earned: 0, total: 0, progressPercent: 0, items: [] })
}
