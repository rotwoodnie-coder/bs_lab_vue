import { getApiBaseURL } from '@/api/config'

/**
 * 解析文件/封面 URL（与 PC 端逻辑一致）
 * - 已是 http(s) 则原样返回
 * - 相对路径拼接到文件服务前缀
 */
export function resolveFileUrl(raw) {
  if (!raw) return ''
  const url = String(raw).trim()
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url

  const prefix = (import.meta.env.VITE_FILE_URL_PREFIX || '').replace(/\/$/, '')
  if (prefix) {
    if (url.startsWith('/')) return `${prefix}${url}`
    return `${prefix}/${url}`
  }

  // 未配置前缀时：尝试用 API 同源（仅适用于 /uploads 挂在 API 服务上的情况）
  const apiBase = getApiBaseURL()
  const origin = apiBase.replace(/\/api\/?$/, '')
  if (url.startsWith('/')) return `${origin}${url}`
  return `${origin}/${url}`
}

const VIDEO_MEDIA_PATTERN = /\.(mp4|webm|ogg|mov|m4v)(\?|#|$)/i

/** 判断 URL 是否指向视频资源（与实验详情 mediaSlides 一致） */
export function isVideoMediaUrl(raw) {
  if (!raw) return false
  return VIDEO_MEDIA_PATTERN.test(String(raw).trim())
}
