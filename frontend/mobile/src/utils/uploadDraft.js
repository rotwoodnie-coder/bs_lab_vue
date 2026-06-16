const DRAFT_TTL_MS = 7 * 24 * 60 * 60 * 1000

export function buildUploadDraftKey({ userId = 'anon', workType = 'homework', taskId = '', childUserId = '' }) {
  return `mb_upload_draft_${userId}_${workType}_${taskId || 'none'}_${childUserId || 'self'}`
}

export function loadUploadDraft(key) {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return null
    const data = JSON.parse(raw)
    if (!data?.savedAt || Date.now() - data.savedAt > DRAFT_TTL_MS) {
      localStorage.removeItem(key)
      return null
    }
    return data
  } catch {
    return null
  }
}

export function saveUploadDraft(key, payload) {
  try {
    localStorage.setItem(key, JSON.stringify({ ...payload, savedAt: Date.now() }))
  } catch (e) {
    console.warn('保存上传草稿失败', e)
  }
}

export function clearUploadDraft(key) {
  try {
    localStorage.removeItem(key)
  } catch {
    /* ignore */
  }
}
