import request from './request'

export function fetchWorks(params = {}) {
  return request.get('/mobile/works', {
    params: {
      scope: params.scope || undefined,
      type: params.type || undefined,
      reviewStatus: params.reviewStatus || undefined,
      page: params.page || 1,
      size: params.size || 50
    }
  })
}

export function fetchWorkDetail(workId) {
  return request.get(`/mobile/works/${workId}`)
}

export function createWork(payload) {
  return request.post('/mobile/works', payload)
}

/** 解析上传接口返回（axios 已 unwrap 为 { code, data }） */
export function parseUploadResponse(res) {
  const payload = res?.data != null && typeof res.data === 'object' ? res.data : (res || {})
  const fileUrl = payload.fileUrl || payload.url || ''
  const previewUrl = payload.previewUrl || fileUrl
  return { fileUrl, previewUrl, fileName: payload.fileName || '', storage: payload.storage || 'minio' }
}

/** 移动端文件上传（MinIO） */
export function uploadFile(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/mobile/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: onProgress
  })
}

/** @deprecated 请使用 uploadFile */
export const uploadMinioFile = uploadFile

/** 删除 MinIO 文件 */
export function deleteFileByUrl(url) {
  return request.delete('/mobile/files/delete', { params: { url } })
}

/** 解析 MinIO 存储 key 为可访问 URL（JSON） */
export function resolveStorageUrl(url) {
  return request.get('/mobile/files/resolve', { params: { url } })
}

/** @deprecated 请使用 deleteFileByUrl */
export const deleteMinioFileByUrl = deleteFileByUrl
