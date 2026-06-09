/** 模拟实验列表/搜索展示辅助 */

export const SUBJECT_FILTERS = [
  { key: 'all', label: '全部' },
  { key: '科学', label: '⚡ 科学' },
  { key: '物理', label: '🔬 物理' },
  { key: '化学', label: '🧪 化学' },
  { key: '生物', label: '🌿 生物' },
  { key: '地理', label: '🌍 地理' }
]

export const EXP_SEARCH_RECENT_KEY = 'bslab-exp-search-recent'

const MEDIA_BY_SUBJECT = {
  科学: ['card-media-grad-warm', 'card-media-grad-forest'],
  物理: ['card-media-grad-ocean', 'card-media-grad-cool', 'card-media-grad-sunset', 'card-media-grad-amber-rose'],
  化学: ['card-media-grad-warm', 'card-media-grad-cool', 'card-media-grad-violet-indigo'],
  生物: ['card-media-grad-forest'],
  地理: ['card-media-grad-ocean']
}

const AVATAR_BY_SUBJECT = {
  科学: ['avatar-grad-warm', 'avatar-grad-sunset'],
  物理: ['avatar-grad-cool', 'avatar-grad-ocean', 'avatar-grad-sunset', 'avatar-grad-amber-rose'],
  化学: ['avatar-grad-warm', 'avatar-grad-ocean', 'avatar-grad-violet-indigo'],
  生物: ['avatar-grad-forest'],
  地理: ['avatar-grad-ocean']
}

const DEFAULT_MEDIA = ['card-media-grad-warm', 'card-media-grad-ocean', 'card-media-grad-forest']
const DEFAULT_AVATAR = ['avatar-grad-cool', 'avatar-grad-warm', 'avatar-grad-ocean']

function pickFrom(list, index) {
  return list[index % list.length]
}

export function getAvatarChar(title) {
  const text = (title || '').trim()
  return text ? text.charAt(0) : '模'
}

export function getMediaGradient(subjectName, index = 0) {
  const list = MEDIA_BY_SUBJECT[subjectName] || DEFAULT_MEDIA
  return pickFrom(list, index)
}

export function getAvatarGradient(subjectName, index = 0) {
  const list = AVATAR_BY_SUBJECT[subjectName] || DEFAULT_AVATAR
  return pickFrom(list, index)
}

export function buildSubjectMap(dictRows = []) {
  const map = {}
  dictRows.forEach((row) => {
    const id = row.subjectId || row.subject_id
    const name = row.subjectName || row.subject_name
    if (id && name) map[id] = name
  })
  return map
}

export function mapSimulatorItem(raw, subjectMap, index = 0) {
  const subjectName = subjectMap[raw.subjectId] || ''
  const parts = []
  if (subjectName) parts.push(subjectName)
  if (raw.comments) parts.push(raw.comments)

  return {
    ...raw,
    subjectName,
    mediaGrad: getMediaGradient(subjectName, index),
    avatarChar: getAvatarChar(raw.simulatorName),
    avatarGrad: getAvatarGradient(subjectName, index),
    metaText: parts.join(' · ')
  }
}

export function loadExpSearchRecent() {
  try {
    return JSON.parse(localStorage.getItem(EXP_SEARCH_RECENT_KEY) || '[]')
  } catch {
    return []
  }
}

export function saveExpSearchRecent(keyword) {
  const trimmed = (keyword || '').trim()
  if (!trimmed) return
  const list = loadExpSearchRecent().filter((item) => item !== trimmed)
  list.unshift(trimmed)
  localStorage.setItem(EXP_SEARCH_RECENT_KEY, JSON.stringify(list.slice(0, 8)))
}

export function removeExpSearchRecent(keyword) {
  const list = loadExpSearchRecent().filter((item) => item !== keyword)
  localStorage.setItem(EXP_SEARCH_RECENT_KEY, JSON.stringify(list))
}
