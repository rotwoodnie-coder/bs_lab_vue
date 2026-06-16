import request from './request'

/** 统一待办/已完成任务列表（全角色） */
export function fetchTaskInbox(params = {}) {
  return request.get('/mobile/tasks/inbox', {
    params: {
      childUserId: params.childUserId || undefined,
      status: params.status || 'pending',
      page: params.page || 1,
      size: params.size || 50
    }
  })
}

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
