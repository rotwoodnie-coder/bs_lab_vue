import request from './request'

export function fetchTeacherDashboard() {
  return request.get('/mobile/teacher/dashboard')
}

export function fetchTeacherReviewQueue(params = {}) {
  return request.get('/mobile/teacher/review-queue', {
    params: { page: params.page || 1, size: params.size || 50 }
  })
}

export function submitTeacherReview(workId, payload) {
  return request.post(`/mobile/teacher/works/${workId}/review`, payload)
}

export function fetchTeacherTaskBoard(taskId) {
  return request.get('/mobile/teacher/task-board', { params: { taskId } })
}

export function fetchTeacherAssignOptions() {
  return request.get('/mobile/teacher/assign-options')
}

export function fetchTeacherTasks() {
  return request.get('/mobile/teacher/tasks')
}

export function remindTeacherTask(taskId) {
  return request.post('/mobile/teacher/remind', null, { params: { taskId } })
}

export function fetchTeacherParentBinds(params = {}) {
  return request.get('/mobile/teacher/parent-binds', {
    params: {
      status: params.status || 'pending',
      page: params.page || 1,
      size: params.size || 20
    }
  })
}

export function fetchTeacherParentBindPendingCount() {
  return request.get('/mobile/teacher/parent-binds/pending-count')
}

export function auditTeacherParentBind(bindId, payload) {
  return request.patch(`/mobile/teacher/parent-binds/${bindId}`, payload)
}
