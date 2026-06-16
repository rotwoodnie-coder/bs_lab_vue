export const MAX_UPLOAD_IMAGES = 9

export const MAX_UPLOAD_VIDEOS = 3



const LEGACY_STORAGE_PATTERN = /127\.0\.0\.1|localhost|\/uploads\//i



export function countUploadFilesByType(files = []) {

  let image = 0

  let video = 0

  for (const file of files) {

    if (file?.type === 'video') video += 1

    else image += 1

  }

  return { image, video }

}



export function canAddUploadFile(existingFiles, incomingFile) {

  const counts = countUploadFilesByType(existingFiles)

  const isVideo = incomingFile?.type?.startsWith('video/')

  if (isVideo) {

    if (counts.video >= MAX_UPLOAD_VIDEOS) {

      return { ok: false, message: `最多上传 ${MAX_UPLOAD_VIDEOS} 段视频` }

    }

  } else if (counts.image >= MAX_UPLOAD_IMAGES) {

    return { ok: false, message: `最多上传 ${MAX_UPLOAD_IMAGES} 张照片` }

  }

  return { ok: true }

}



export function canAddAnyUploadFile(files = []) {

  const { image, video } = countUploadFilesByType(files)

  return image < MAX_UPLOAD_IMAGES || video < MAX_UPLOAD_VIDEOS

}



/** 旧版本地/Tomcat 存储地址（移动端已废弃） */

export function isLegacyStorageUrl(url) {

  return Boolean(url) && LEGACY_STORAGE_PATTERN.test(String(url))

}



/** 已通过 MinIO 上传：object key 或 MinIO/预签名 http(s) URL */

export function isMinioStorageUrl(url) {

  if (!url || String(url).startsWith('blob:')) return false

  if (isLegacyStorageUrl(url)) return false

  const value = String(url).trim()

  return /^https?:\/\//i.test(value) || value.startsWith('/')

}



/** @deprecated 请使用 isMinioStorageUrl */

export function isServerUploadedUrl(url) {

  return isMinioStorageUrl(url)

}

