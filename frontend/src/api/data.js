import request from './request'

//data dict
export function fetchDataDictItems(type, config = {}) {
  return request.get(`/data/dict/${type}`, config)
}

export function createDataDictItem(type, data) {
  return request.post(`/data/dict/${type}`, data)
}

export function updateDataDictItem(type, id, data) {
  return request.post(`/data/dict/${type}/${id}`, data)
}

export function deleteDataDictItem(type, id) {
  return request.delete(`/data/dict/${type}/${id}`)
}

//question types
export function fetchQuestionTypes() {
  return fetchDataDictItems('data_question_type')
}

export function fetchDifficultyTypes() {
  return fetchDataDictItems('data_difficulty_type')
}

//question capacities
export function fetchQuestionCapacities() {
  return fetchDataDictItems('data_question_capacity')
}

//data file types
export function fetchDataFileTypes() {
  return request.get('/sys/data-file-types')
}

export function createDataFileType(data) {
  return request.post('/sys/data-file-types', data)
}

export function updateDataFileType(id, data) {
  return request.post(`/sys/data-file-types/${id}`, data)
}

export function deleteDataFileType(id) {
  return request.delete(`/sys/data-file-types/${id}`)
}

//data file
export function fetchDataFiles(params) {
  return request.get('/data/files', { params })
}

export function fetchMyDataFiles(params) {
  return request.get('/data/files/my', { params })
}

export function fetchPublicDataFiles(params) {
  return request.get('/data/files/public', { params })
}

export function fetchDataFilesForPublic(params) {
  return request.get('/data/files/forPublic', { params })
}

export function fetchDataFileLogs(params) {
  return request.get('/data/file-logs', { params })
}

export function fetchVideoMaterials(params) {
  return request.get('/data/files/videos', { params: { ...params, fileTypeId: 'Video' } })
}

export function fetchImageMaterials(params) {
  return request.get('/data/files/videos', { params: { ...params, fileTypeId: 'Image' } })
}

export function createDataFile(data) {
  return request.post('/data/files', data)
}

export function updateDataFile(id, data) {
  return request.post(`/data/files/${id}`, data)
}

export function updateDataFilePublic(id, data) {
  return request.post(`/data/files/${id}/public`, data)
}

export function deleteDataFile(id) {
  return request.delete(`/data/files/${id}`)
}

//material msg
export function fetchMaterialMsgs(params) {
  return request.get('/data/material-msgs', { params })
}

export function fetchMaterialMsgsMy(params) {
  return request.get('/data/material-msgs/my', { params })
}

export function fetchMaterialMsgsPublic(params) {
  return request.get('/data/material-msgs/public', { params })
}

export function fetchMaterialMsgsForPublic(params) {
  return request.get('/data/material-msgs/forPublic', { params })
}

export function fetchPublicMaterialMsgs(params) {
  return request.get('/data/material-msgs', { params: { ...params, publicMode: true } })
}

export function fetchMaterialMsg(id) {
  return request.get(`/data/material-msgs/${id}`)
}

export function fetchMaterialLogs(params) {
  return request.get('/data/material-logs', { params })
}

export function createMaterialMsg(data) {
  return request.post('/data/material-msgs', data)
}

export function updateMaterialMsg(id, data) {
  return request.post(`/data/material-msgs/${id}`, data)
}

export function updateMaterialMsgPublic(id, data) {
  return request.post(`/data/material-msgs/${id}/public`, data)
}

export function deleteMaterialMsg(id) {
  return request.delete(`/data/material-msgs/${id}`)
}