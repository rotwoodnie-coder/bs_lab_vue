const SUBJECT_GRADIENT = {
  科学: 'card-media-grad-forest',
  物理: 'card-media-grad-cool',
  化学: 'card-media-grad-amber-rose',
  生物: 'card-media-grad-forest'
}

export const HOME_SEARCH_RECENT_KEY = 'bslab-search-recent'

export function getGradientClass(subject) {
  return SUBJECT_GRADIENT[subject] || 'card-media-grad-warm'
}

export function formatPlayCount(count) {
  if (!count) return '0次播放'
  if (count >= 10000) return (count / 10000).toFixed(1) + '万次播放'
  return count + '次播放'
}

export function detailRoute(item) {
  if (item.type === 'work') return `/works/${item.id}`
  if (item.authorRole === 'student') return `/works/${item.id}`
  if (item.type === 'video') return `/video/${item.id}`
  if (item.type === 'simulation') {
    const simId = item.simulatorId || item.id
    return `/sim/${simId}`
  }
  return `/exp/${item.id}`
}

export function sceneIcon(item) {
  const map = { 科学: 'sprout', 物理: 'zap', 化学: 'flask-conical', 生物: 'sprout' }
  return map[item.subject] || 'beaker'
}

export function tagIcon(item) {
  const map = {
    video: 'play-circle',
    exp: 'flask-conical',
    sim: 'flask-conical',
    'work-homework': 'clipboard-list',
    'work-remix': 'clapperboard',
    'work-creative': 'lightbulb'
  }
  return map[item.tagType] || 'play-circle'
}

export function metaLineParts(item) {
  const parts = []
  if (item.type === 'work') {
    if (item.grade) parts.push(item.grade)
    if (item.tagLabel) parts.push(item.tagLabel)
    parts.push(item.playCount ? `${item.playCount}赞` : '0赞')
    return parts
  }
  if (item.subject) parts.push(item.subject)
  if (item.grade) parts.push(item.grade)
  parts.push(formatPlayCount(item.playCount))
  return parts
}

export function authorLineParts(item) {
  // 学生 / 作品：姓名 + 班级 + 学校名称
  if (item.type === 'work' || item.authorRole === 'student') {
    const parts = []
    if (item.author) parts.push(item.author)
    if (item.classLabel) parts.push(item.classLabel)
    else if (item.grade) parts.push(item.grade)
    if (item.authorSchool) parts.push(item.authorSchool)
    return parts
  }
  // 教师（及未知角色）：姓名 + 老师 + 学校名称
  const parts = []
  parts.push(item.author || '未知')
  parts.push('老师')
  if (item.authorSchool) parts.push(item.authorSchool)
  return parts
}

export function loadHomeSearchRecent() {
  try {
    return JSON.parse(localStorage.getItem(HOME_SEARCH_RECENT_KEY) || '[]')
  } catch {
    return []
  }
}

export function saveHomeSearchRecent(keyword) {
  const trimmed = (keyword || '').trim()
  if (!trimmed) return
  const list = loadHomeSearchRecent().filter((item) => item !== trimmed)
  list.unshift(trimmed)
  localStorage.setItem(HOME_SEARCH_RECENT_KEY, JSON.stringify(list.slice(0, 8)))
}

export function removeHomeSearchRecent(keyword) {
  const list = loadHomeSearchRecent().filter((item) => item !== keyword)
  localStorage.setItem(HOME_SEARCH_RECENT_KEY, JSON.stringify(list))
}

export function formatDateTime(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
