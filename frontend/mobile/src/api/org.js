import request from './request'

export function fetchSchools() {
  return request.get('/mobile/org/schools')
}

export function fetchGrades(schoolOrgId) {
  return request.get('/mobile/org/grades', { params: { schoolOrgId } })
}

export function fetchClasses(gradeOrgId) {
  return request.get('/mobile/org/classes', { params: { gradeOrgId } })
}

export function searchStudents(params) {
  return request.get('/mobile/org/students/search', { params })
}
