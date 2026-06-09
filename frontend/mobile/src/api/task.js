import request from './request'

export function fetchTasks(params = {}) {
  return request.get('/mobile/tasks', {
    params: {
      childUserId: params.childUserId || undefined,
      category: params.category || undefined,
      type: params.type || undefined,
      status: params.status || undefined,
      page: params.page || 1,
      size: params.size || 20
    }
  })
}

export function fetchTaskDetail(taskId) {
  return request.get(`/mobile/tasks/${taskId}`)
}

export function createTask(payload) {
  return request.post('/mobile/tasks', payload)
}
