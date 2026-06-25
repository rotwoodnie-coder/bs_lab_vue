import request from './request'

export function bindChild(payload) {
  return request.post('/mobile/parent/bind', payload)
}

export function fetchParentChildren() {
  return request.get('/mobile/parent/children')
}

export function fetchPendingBinds() {
  return request.get('/mobile/parent/bind-pending')
}

export function fetchBindApplications() {
  return request.get('/mobile/parent/bind-applications')
}

export function setDefaultChild(childUserId) {
  return request.post('/mobile/parent/default-child/save', { childUserId })
}
