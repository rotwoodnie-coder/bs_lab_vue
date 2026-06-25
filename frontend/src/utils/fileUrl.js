/** MinIO / 文件存储 URL 前缀（与 backend app.minio.url-prefix、Nginx /media/ 一致） */
export function getFileUrlPrefix() {
  return (import.meta.env.VITE_File_URL_PREFIX || import.meta.env.VITE_MINIO_URL_PREFIX || '').replace(/\/$/, '')
}

/** 将后端返回的 fileUrl / previewUrl 转为浏览器可访问地址 */
export function resolveFileUrl(raw) {
  if (!raw) return ''
  const value = String(raw).trim()
  if (/^https?:\/\//i.test(value)) return value
  const prefix = getFileUrlPrefix()
  if (!prefix) return value.startsWith('/') ? value : `/${value}`
  if (value.startsWith(`${prefix}/`) || value === prefix) return value
  if (value.startsWith('/')) return `${prefix}${value}`
  return `${prefix}/${value}`
}

/**
 * 展示用：MinIO 存储键优先走同源相对路径（避免 HTTPS 页面加载 HTTP previewUrl 被拦截）；
 * 外链或仅有 previewUrl 时再使用 previewUrl。
 */
export function resolveDisplayUrl(previewUrl, fileUrl) {
  const stored = String(fileUrl || '').trim()
  const preview = String(previewUrl || '').trim()
  if (stored && !/^https?:\/\//i.test(stored)) {
    return resolveFileUrl(stored)
  }
  if (preview) return resolveFileUrl(preview)
  return resolveFileUrl(stored)
}

export function isImageUrl(url = '', fileName = '') {
  return /\.(png|jpe?g|gif|webp|bmp|svg)(\?.*)?$/i.test(String(fileName || url))
}

export function isVideoUrl(url = '', fileName = '') {
  return /\.(mp4|mov|avi|mkv|webm)(\?.*)?$/i.test(String(fileName || url))
}

export function isAudioUrl(url = '', fileName = '') {
  return /\.(mp3|wav|flac|aac|ogg|m4a)(\?.*)?$/i.test(String(fileName || url))
}

/** 打开 MinIO 上的 HTML 模拟器等：走后端预览接口，避免 /media/ 未配置时 404 */
export function resolveMinioPreviewApiUrl(fileUrl) {
  const stored = String(fileUrl || '').trim()
  if (!stored || /^https?:\/\//i.test(stored)) return stored
  const apiBase = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')
  return `${apiBase}/minio/files/preview?url=${encodeURIComponent(stored)}`
}
