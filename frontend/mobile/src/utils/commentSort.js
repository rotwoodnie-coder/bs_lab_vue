/** @param {'hot'|'latest'} mode */
export function sortComments(list, mode = 'hot') {
  const items = [...(list || [])]
  if (mode === 'latest') return items
  return items.sort((a, b) => {
    const likeDiff = (b.likes || 0) - (a.likes || 0)
    if (likeDiff !== 0) return likeDiff
    return String(b.time || '').localeCompare(String(a.time || ''))
  })
}
