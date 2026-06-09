import { fetchNoticeReadIds, markNoticeRead as markNoticeReadApi } from '@/api/home'

const readIds = new Set()
let loaded = false
let loadingPromise = null

export async function ensureNoticeReadState() {
  if (loaded) return readIds
  if (!loadingPromise) {
    loadingPromise = fetchNoticeReadIds()
      .then((res) => {
        const ids = res?.data || []
        readIds.clear()
        ids.forEach((id) => readIds.add(id))
        loaded = true
      })
      .catch(() => {
        loaded = true
      })
      .finally(() => {
        loadingPromise = null
      })
  }
  await loadingPromise
  return readIds
}

export function isNoticeRead(noticeId) {
  if (!noticeId) return false
  return readIds.has(noticeId)
}

export async function markNoticeRead(noticeId) {
  if (!noticeId || readIds.has(noticeId)) return
  try {
    await markNoticeReadApi(noticeId)
  } catch {
    /* ignore network error; still hide locally */
  }
  readIds.add(noticeId)
}

export function resetNoticeReadState() {
  readIds.clear()
  loaded = false
  loadingPromise = null
}
