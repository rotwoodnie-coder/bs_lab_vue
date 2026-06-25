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

/** 富文本内嵌媒体 URL：经后端 MinIO 预览代理 */
export function buildRichTextMediaUrl(raw) {
  if (!raw) return ''
  const trimmed = String(raw).trim()
  if (!trimmed || trimmed.startsWith('data:')) return trimmed
  if (trimmed.includes('/mobile/files/preview')) return trimmed
  return `${apiBase()}/mobile/files/preview?url=${encodeURIComponent(trimmed)}`
}

/** 将 HTML 中 src="…" 重写为 MinIO 预览代理（参考资料、步骤说明等富文本） */
export function rewriteRichTextMediaUrls(html) {
  if (!html || !/\bsrc\s*=/.test(html)) return html
  return String(html).replace(
    /(\bsrc\s*=\s*)(["'])([^"']+)\2/gi,
    (_, prefix, quote, url) => `${prefix}${quote}${buildRichTextMediaUrl(url)}${quote}`
  )
}

/** 实验材料主图：经后端 MinIO 预览代理（<img> 无法携带 JWT） */
function buildMaterialPreviewUrl(raw) {
  if (!raw) return ''
  const trimmed = String(raw).trim()
  if (!trimmed) return ''
  if (trimmed.includes('/mobile/files/preview')) return trimmed
  if (/^https?:\/\//i.test(trimmed) && !isLegacyStorageUrl(trimmed)) return trimmed
  return `${apiBase()}/mobile/files/preview?url=${encodeURIComponent(trimmed)}`
}

export function resolveMaterialPicUrl(item) {
  if (!item) return ''
  if (typeof item === 'string') {
    return buildMaterialPreviewUrl(item)
  }
  const preview = item.mainPicPreviewUrl || item.materialPreviewUrl
  if (preview) {
    const trimmed = String(preview).trim()
    if (trimmed.includes('/mobile/files/preview')) return trimmed
    if (/^https?:\/\//i.test(trimmed) && !isLegacyStorageUrl(trimmed)) return trimmed
    return buildMaterialPreviewUrl(trimmed)
  }
  return buildMaterialPreviewUrl(item.mainPicUrl || item.materialUrl || '')
}

/** 作品附件：经后端 MinIO 预览代理（<img>/<video> 无法携带 JWT） */
export function resolveWorkFileUrl(file) {
  if (!file) return ''
  const raw = typeof file === 'string'
    ? file
    : (file.previewUrl || file.preview_url || file.url || file.fileUrl || '')
  if (!raw) return ''
  const trimmed = String(raw).trim()
  if (trimmed.includes('/mobile/files/preview')) return trimmed
  return `${apiBase()}/mobile/files/preview?url=${encodeURIComponent(trimmed)}`
}

const VIDEO_MEDIA_PATTERN = /\.(mp4|webm|ogg|mov|m4v)(\?|#|$)/i

export function isVideoMediaUrl(raw) {
  if (!raw) return false
  return VIDEO_MEDIA_PATTERN.test(String(raw).trim())
}
