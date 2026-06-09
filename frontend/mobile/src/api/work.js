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

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/mobile/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
