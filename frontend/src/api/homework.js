import request from './request'

// homework
export function fetchExpHomeworks(params) {
    return request.get('/homework', { params })
  }
  
  export function fetchExpHomework(id) {
    return request.get(`/homework/${id}`)
  }
  
  export function createExpHomework(data) {
    return request.post('/homework', data)
  }
  
  export function updateExpHomework(id, data) {
    return request.put(`/homework/${id}`, data)
  }
  
  export function deleteExpHomework(id) {
    return request.delete(`/homework/${id}`)
  }
  
  // homework student
  export function fetchExpHomeworkStudents(params) {
    return request.get('/homework-students', { params })
  }
  
  export function fetchExpHomeworkStudent(id) {
    return request.get(`/homework-students/${id}`)
  }
  
  export function createExpHomeworkStudent(data) {
    return request.post('/homework-students', data)
  }
  
  export function updateExpHomeworkStudent(id, data) {
    return request.put(`/homework-students/${id}`, data)
  }
  
  export function deleteExpHomeworkStudent(id) {
    return request.delete(`/homework-students/${id}`)
  }