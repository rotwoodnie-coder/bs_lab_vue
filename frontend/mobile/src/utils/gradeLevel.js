/**
 * 从登录用户 / 个人资料解析年级段（低段/中段/高段）与报告展示用年级名称。
 */

export function normalizeGradeSegment(raw) {
  const value = String(raw || '').trim()
  if (value === '低段' || value === '中段' || value === '高段') return value
  return ''
}

/** 从「三年级」「三年级2班」等文本解析 1-6 年级数字 */
export function parseChineseGradeNumber(text = '') {
  const s = String(text || '').trim()
  if (!s) return null
  if (/[1一]年级/.test(s)) return 1
  if (/[2二]年级/.test(s)) return 2
  if (/[3三]年级/.test(s)) return 3
  if (/[4四]年级/.test(s)) return 4
  if (/[5五]年级/.test(s)) return 5
  if (/[6六]年级/.test(s)) return 6
  const gMatch = s.match(/\bg([1-6])\b/i)
  if (gMatch) return Number(gMatch[1])
  return null
}

export function gradeNumberToSegment(num) {
  if (!num || num < 1) return ''
  if (num <= 2) return '低段'
  if (num <= 4) return '中段'
  if (num <= 6) return '高段'
  return ''
}

/** 提取「X年级」用于报告展示 */
export function extractGradeLabel(text = '') {
  const s = String(text || '').trim()
  if (!s) return ''
  const m = s.match(/([一二三四五六1-6]年级)/)
  if (m) {
    const num = parseChineseGradeNumber(m[1])
    const cn = ['', '一年级', '二年级', '三年级', '四年级', '五年级', '六年级']
    return num ? cn[num] : m[1]
  }
  return s
}

/**
 * @param {{ profile?: object, userInfo?: object }} sources
 * @returns {string} 低段 / 中段 / 高段
 */
export function resolveGradeSegment({ profile = {}, userInfo = {} } = {}) {
  const direct = normalizeGradeSegment(
    profile.gradeSegment ||
      userInfo.gradeSegment ||
      userInfo.gradeLevel ||
      profile.gradeLevel
  )
  if (direct) return direct

  const orgText =
    profile.gradeName ||
    profile.userOrgName ||
    userInfo.gradeName ||
    userInfo.userOrgName ||
    profile.className ||
    ''
  const num = parseChineseGradeNumber(orgText)
  if (num) return gradeNumberToSegment(num)
  return ''
}

/**
 * 诊断报告「年级」栏展示文案：优先具体年级（三年级），否则学段（中段）。
 */
export function resolveGradeLabel({ profile = {}, userInfo = {} } = {}) {
  if (profile.gradeName) return String(profile.gradeName).trim()

  const orgText = profile.userOrgName || userInfo.userOrgName || profile.className || ''
  const fromOrg = extractGradeLabel(orgText)
  if (fromOrg && /年级/.test(fromOrg)) return fromOrg

  const segment = resolveGradeSegment({ profile, userInfo })
  if (segment) return segment

  return orgText.trim()
}
