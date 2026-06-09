const STORAGE_KEY = 'mobile_bind_success_snapshot'

export function buildBindSuccessQuery(source = {}, from = 'bind') {
  return {
    name: source.childName || source.name || '',
    from,
    phone: source.phone || '',
    schoolName: source.schoolName || '',
    gradeName: source.gradeName || '',
    className: source.className || '',
    relation: source.relation || '',
    message: source.message || '',
    bindId: source.bindId || ''
  }
}

export function persistBindSuccessSnapshot(source) {
  if (typeof sessionStorage === 'undefined') return
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(source))
  } catch {
    // ignore quota / private mode
  }
}

export function readBindSuccessSnapshot() {
  if (typeof sessionStorage === 'undefined') return null
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function formatClassLabel(item = {}) {
  const parts = [item.schoolName, item.gradeName, item.className].filter(Boolean)
  return parts.length ? parts.join(' · ') : ''
}

export function normalizeBindItem(raw = {}) {
  return {
    bindId: raw.bindId || '',
    childName: raw.childName || raw.name || '',
    schoolName: raw.schoolName || '',
    gradeName: raw.gradeName || '',
    className: raw.className || '',
    relation: raw.relation || '',
    bindStatus: raw.bindStatus || 'pending',
    message: raw.message || '',
    rejectReason: raw.rejectReason || '',
    submitTime: raw.submitTime || ''
  }
}
