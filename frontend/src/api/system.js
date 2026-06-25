import request, { UPLOAD_TIMEOUT_MS } from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function fetchUsers(params) {
  return request.get('/sys/users', { params })
}

export function createUser(data) {
  return request.post('/sys/users', data)
}

export function updateUser(id, data) {
  return request.post(`/sys/users/${id}`, data)
}

export function updateUserStatus(id, status) {
  return request.post(`/sys/users/${id}/status`, null, { params: { status } })
}

export function changeUserPassword(userId, data) {
  return request.post(`/sys/users/${userId}/password`, data)
}

export function fetchUserRoles(userId) {
  return request.get(`/sys/users/${userId}/roles`)
}

export function saveUserRoles(userId, roleIds) {
  return request.post(`/sys/users/${userId}/roles`, { roleIds })
}

export function deleteUser(id) {
  return request.delete(`/sys/users/${id}`)
}

export function fetchOrgTree() {
  return request.get('/sys/orgs/tree')
}

export function createOrg(data) {
  return request.post('/sys/orgs', data)
}

export function updateOrg(id, data) {
  return request.post(`/sys/orgs/${id}`, data)
}

export function deleteOrg(id) {
  return request.delete(`/sys/orgs/${id}`)
}

export function fetchVisibleMenus() {
  return request.get('/sys/menus/visible')
}

export function fetchMenus(params) {
  return request.get('/sys/menus', { params })
}

export function createMenu(data) {
  return request.post('/sys/menus', data)
}

export function updateMenu(id, data) {
  return request.post(`/sys/menus/${id}`, data)
}

export function deleteMenu(id) {
  return request.delete(`/sys/menus/${id}`)
}

export function updateMenuStatus(id, status) {
  return request.post(`/sys/menus/${id}/status`, null, { params: { status } })
}

export function fetchRoles(params) {
  return request.get('/sys/roles', { params })
}

export function createRole(data) {
  return request.post('/sys/roles', data)
}

export function updateRole(id, data) {
  return request.post(`/sys/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/sys/roles/${id}`)
}

export function updateRoleStatus(id, status) {
  return request.post(`/sys/roles/${id}/status`, null, { params: { status } })
}

export function fetchRoleMenus(roleId) {
  return request.get(`/sys/roles/${roleId}/menus`)
}

export function saveRoleMenus(roleId, menuIds) {
  return request.post(`/sys/roles/${roleId}/menus`, { menuIds })
}

export function fetchSystemMessages(params) {
  return request.get('/system/messages', { params })
}

export function fetchUnreadSystemMessageCount() {
  return request.get('/system/messages/unread-count')
}

export function markSystemMessageRead(msgId) {
  return request.post(`/system/messages/${msgId}/read`)
}

export function fetchSystemLogs(params) {
  return request.get('/sys/logs', { params })
}

export function fetchSystemLog(logId) {
  return request.get(`/sys/logs/${logId}`)
}

export function deleteSystemLog(logId) {
  return request.delete(`/sys/logs/${logId}`)
}

//system data dictionaries
export function fetchDictionaryItems(type) {
  return request.get(`/sys/data-dictionaries/${type}`)
}

export function createDictionaryItem(type, data) {
  return request.post(`/sys/data-dictionaries/${type}`, data)
}

export function updateDictionaryItem(type, id, data) {
  return request.post(`/sys/data-dictionaries/${type}/${id}`, data)
}

export function deleteDictionaryItem(type, id) {
  return request.delete(`/sys/data-dictionaries/${type}/${id}`)
}

//upload files
export function uploadFile(file, onUploadProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/files/upload', formData, {
    onUploadProgress,
    timeout: UPLOAD_TIMEOUT_MS
  })
}

export function deleteFileByUrl(url) {
  return request.delete('/files/delete', { params: { url } })
}

//minio files
/** 绕过部分 Nginx/WAF 对 .html/.htm 文件名的拦截（上传时改用 .sim，由后端按 originalFilename 还原） */
const MINIO_HTML_UPLOAD_SAFE_EXT = '.sim'

function buildMinioUploadFormData(file) {
  const formData = new FormData()
  const name = String(file?.name || 'file')
  if (/\.html?$/i.test(name)) {
    const safeName = name.replace(/\.html?$/i, MINIO_HTML_UPLOAD_SAFE_EXT)
    const safeFile = new File([file], safeName, { type: file.type || 'text/html' })
    formData.append('file', safeFile)
    formData.append('originalFilename', name)
  } else {
    formData.append('file', file)
  }
  return formData
}

export function uploadMinioFile(file, onUploadProgress) {
  return request.post('/minio/files/upload', buildMinioUploadFormData(file), {
    onUploadProgress,
    timeout: UPLOAD_TIMEOUT_MS
  })
}

export function deleteMinioFileByUrl(url) {
  return request.delete('/minio/files/delete', { params: { url } })
}
