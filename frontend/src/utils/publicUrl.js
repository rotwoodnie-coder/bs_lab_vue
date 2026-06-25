/**
 * public 目录静态资源 URL（随 Vite base 前缀，如 /admin/）
 * @param {string} path 如 icons/home.svg 或 /icons/home.svg
 */
export function publicUrl(path) {
  const base = import.meta.env.BASE_URL || '/'
  const normalized = String(path || '').replace(/^\//, '')
  return `${base}${normalized}`
}

/** 素材类型图标（DataFileType / PublicDataFile 等共用） */
export const FILE_TYPE_ICON_MAP = Object.freeze({
  image: publicUrl('icons/image.svg'),
  video: publicUrl('icons/video.svg'),
  audio: publicUrl('icons/audio.svg'),
  pdf: publicUrl('icons/pdf.svg'),
  word: publicUrl('icons/word.svg'),
  ppt: publicUrl('icons/ppt.svg'),
  excel: publicUrl('icons/excel.svg')
})
