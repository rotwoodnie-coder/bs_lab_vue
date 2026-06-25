/** 本地日期 yyyy-MM-dd，默认发布日后 7 天 */
export function defaultDeadlineDate(daysAfter = 7) {
  const d = new Date()
  d.setDate(d.getDate() + daysAfter)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export function deadlineDateToIsoEnd(dateStr) {
  if (!dateStr) return undefined
  return `${dateStr}T23:59:59`
}
