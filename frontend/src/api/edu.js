import request from './request'

//coursebooks
export function fetchCoursebooks(params) {
  return request.get('/edu/coursebooks', { params })
}

export function createCoursebook(data) {
  return request.post('/edu/coursebooks', data)
}

export function updateCoursebook(id, data) {
  return request.put(`/edu/coursebooks/${id}`, data)
}

export function deleteCoursebook(id) {
  return request.delete(`/edu/coursebooks/${id}`)
}

//coursebook content
export function fetchCoursebookContents(params) {
  return request.get('/edu/coursebook-content', { params })
}

export function createCoursebookContent(data) {
  return request.post('/edu/coursebook-content', data)
}

export function updateCoursebookContent(id, data) {
  return request.put(`/edu/coursebook-content/${id}`, data)
}

export function deleteCoursebookContent(id) {
  return request.delete(`/edu/coursebook-content/${id}`)
}

export function reorderCoursebookContents(coursebookId, tree) {
  return request.put(`/edu/coursebook-content/${coursebookId}/reorder`, tree)
}

//teacher subjects
export function fetchTeacherSubjects(params) {
  return request.get('/edu/teacher-subjects', { params })
}

export function fetchTeacherSubject(teacherId) {
  return request.get(`/edu/teacher-subjects/${teacherId}`)
}

export function saveTeacherSubject(teacherId, data) {
  return request.put(`/edu/teacher-subjects/${teacherId}`, data)
}

export function fetchTeacherClasses(params) {
  return request.get('/edu/teacher-classes', { params })
}

export function saveTeacherClasses(teacherId, data) {
  return request.put(`/edu/teacher-classes/${teacherId}`, data)
}


//subject group
export function fetchSubjectGroups(params) {
  return request.get('/edu/subject-groups', { params })
}

export function fetchTeacherAssignOptions(params) {
  return request.get('/edu/subject-groups/teacher-assign-options', { params })
}

export function fetchSubjectGroup(groupId) {
  return request.get(`/edu/subject-groups/${groupId}`)
}

export function createSubjectGroup(data) {
  return request.post('/edu/subject-groups', data)
}

export function updateSubjectGroup(groupId, data) {
  return request.put(`/edu/subject-groups/${groupId}`, data)
}

export function deleteSubjectGroup(groupId) {
  return request.delete(`/edu/subject-groups/${groupId}`)
}

export function fetchSubjectGroupResearchers(groupId) {
  return request.get(`/edu/subject-groups/${groupId}/researchers`)
}

export function saveSubjectGroupResearchers(groupId, data) {
  return request.put(`/edu/subject-groups/${groupId}/researchers`, data)
}

export function fetchSubjectGroupTeachers(groupId) {
  return request.get(`/edu/subject-groups/${groupId}/teachers`)
}

export function saveSubjectGroupTeachers(groupId, data) {
  return request.put(`/edu/subject-groups/${groupId}/teachers`, data)
}

//school notices
export function fetchSchoolNotices(params) {
  return request.get('/edu/school-notices', { params })
}

export function fetchSchoolNotice(id) {
  return request.get(`/edu/school-notices/${id}`)
}

export function createSchoolNotice(data) {
  return request.post('/edu/school-notices', data)
}

export function updateSchoolNotice(id, data) {
  return request.put(`/edu/school-notices/${id}`, data)
}
export function publishSchoolNotice(id, data) {
  return request.put(`/edu/school-notices/${id}/publish`, data)
}

export function deleteSchoolNotice(id) {
  return request.delete(`/edu/school-notices/${id}`)
}
