import request from './request'

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
  return request.put(`/sys/users/${id}`, data)
}

export function updateUserStatus(id, status) {
  return request.patch(`/sys/users/${id}/status`, null, { params: { status } })
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
  return request.put(`/sys/orgs/${id}`, data)
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
  return request.put(`/sys/menus/${id}`, data)
}

export function deleteMenu(id) {
  return request.delete(`/sys/menus/${id}`)
}

export function updateMenuStatus(id, status) {
  return request.patch(`/sys/menus/${id}/status`, null, { params: { status } })
}

export function fetchRoles(params) {
  return request.get('/sys/roles', { params })
}

export function createRole(data) {
  return request.post('/sys/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/sys/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/sys/roles/${id}`)
}

export function updateRoleStatus(id, status) {
  return request.patch(`/sys/roles/${id}/status`, null, { params: { status } })
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
  return request.put(`/system/messages/${msgId}/read`)
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
  return request.put(`/sys/data-dictionaries/${type}/${id}`, data)
}

export function deleteDictionaryItem(type, id) {
  return request.delete(`/sys/data-dictionaries/${type}/${id}`)
}

//upload files
export function uploadFile(file, onUploadProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress
  })
}

export function deleteFileByUrl(url) {
  return request.delete('/files/delete', { params: { url } })
}

//minio files
export function uploadMinioFile(file, onUploadProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/minio/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress
  })
}

export function deleteMinioFileByUrl(url) {
  return request.delete('/minio/files/delete', { params: { url } })
}
