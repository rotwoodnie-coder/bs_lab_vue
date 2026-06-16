/**
 * 移动端 MinIO 媒体 URL 解析（与管理端 previewUrl + fileUrl 语义一致，代码独立维护）
 */

const LEGACY_STORAGE_PATTERN = /127\.0\.0\.1|localhost|\/uploads\//i

function minioPrefix() {
  return (import.meta.env.VITE_MINIO_URL_PREFIX || '').replace(/\/$/, '')
}

function apiBase() {
  return (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')
}

/** 旧版本地/Tomcat 地址 */
export function isLegacyStorageUrl(raw) {
  return Boolean(raw) && LEGACY_STORAGE_PATTERN.test(String(raw))
}

/** 经后端代理预览（手机无法直连 MinIO 时使用） */
export function buildMobilePreviewUrl(storageKey) {
  if (!storageKey) return ''
  const trimmed = String(storageKey).trim()
  // 已是 http(s) 且非 legacy，直接返回
  if (/^https?:\/\//i.test(trimmed) && !isLegacyStorageUrl(trimmed)) return trimmed
  // 相对 object key 或 legacy URL → 后端代理
  return `${apiBase()}/mobile/files/preview?url=${encodeURIComponent(trimmed)}`
}

/** 相对 object key 拼前缀；已是 http(s) 则原样返回；legacy 走后端预览代理 */
export function resolveFileUrl(raw) {
  if (!raw) return ''
  const url = String(raw).trim()
  if (!url) return ''
  // legacy 地址（127.0.0.1 / /uploads/）不走直连，走后端代理
  if (isLegacyStorageUrl(url)) return buildMobilePreviewUrl(url)
  // 正常 http(s) 直通（含预签名 URL）
  if (/^https?:\/\//i.test(url)) return url

  const prefix = minioPrefix()
  if (!prefix) return buildMobilePreviewUrl(url)

  return url.startsWith('/') ? `${prefix}${url}` : `${prefix}/${url}`
}

/**
 * 展示 URL：优先 previewUrl，其次 storage key 拼前缀
 * @param {string} previewUrl 预签名或可访问 URL
 * @param {string} fileUrl 存库 object key
 */
export function resolveDisplayUrl(previewUrl, fileUrl) {
  return resolveFileUrl(previewUrl || fileUrl || '')
}

const PREVIEW_KEYS = [
  'previewUrl', 'preview_url',
  'mainPicPreviewUrl', 'materialPreviewUrl',
  'coverImagePreviewUrl', 'simulatorPreviewUrl'
]

const FILE_KEYS = [
  'url', 'fileUrl', 'videoUrl', 'mainPicUrl',
  'coverUrl', 'coverImageUrl', 'simulatorUrl', 'src'
]

/** 从对象或字符串解析展示 URL */
export function resolveMediaUrl(item, ...fallbackKeys) {
  if (!item) return ''
  if (typeof item === 'string') return resolveFileUrl(item)

  for (const key of PREVIEW_KEYS) {
    const preview = item[key]
    if (preview) return resolveFileUrl(preview)
  }

  for (const key of fallbackKeys.length ? fallbackKeys : FILE_KEYS) {
    const value = item[key]
    if (value) return resolveFileUrl(value)
  }

  return ''
}

/** 作品附件：previewUrl 优先，其次 url/fileUrl */
export function resolveWorkFileUrl(file) {
  if (!file) return ''
  if (typeof file === 'string') return resolveFileUrl(file)
  return resolveDisplayUrl(file.previewUrl || file.preview_url, file.url || file.fileUrl || '')
}

const VIDEO_MEDIA_PATTERN = /\.(mp4|webm|ogg|mov|m4v)(\?|#|$)/i

export function isVideoMediaUrl(raw) {
  if (!raw) return false
  return VIDEO_MEDIA_PATTERN.test(String(raw).trim())
}
