import request from './request'

export function fetchMessages(params = {}) {
  return request.get('/mobile/notifications', {
    params: {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 50,
      msgTypeId: params.msgTypeId || undefined,
      readTag: params.readTag || undefined
    }
  })
}

export function fetchUnreadCount() {
  return request.get('/mobile/notifications/unread-count')
}

export function markMessageRead(msgId) {
  return request.put(`/mobile/notifications/${msgId}/read`)
}

export function markAllMessagesRead() {
  return request.put('/mobile/notifications/read-all')
}
