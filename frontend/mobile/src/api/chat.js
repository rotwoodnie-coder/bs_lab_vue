import request from './request'
import { agentUrl } from './config'

/**
 * 发送聊天消息（非流式）
 * @param {object} params - { message, thread_id?, role?, user_name?, user_id?, grade_level? }
 * @returns {Promise<{reply: string, thread_id: string}>}
 */
export function sendChatMessage(params) {
  return request.post('/mobile/chat/send', params)
}

/**
 * 发送聊天消息（流式 SSE）
 * @param {object} params - { message, thread_id?, role?, user_name?, user_id?, grade_level? }
 * @param {function} onChunk - 收到每个文本块的回调
 * @param {function} onDone - 流结束的回调
 * @param {function} onError - 出错回调
 */
export function sendChatMessageStream(params, onChunk, onDone, onError) {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const role = params.role || 'student'
  const queryParams = new URLSearchParams({
    message: params.message,
    ...(params.thread_id && { thread_id: params.thread_id }),
    ...(params.user_name || userInfo.username ? { user_name: params.user_name || userInfo.username } : {}),
    ...(params.user_id || userInfo.userId ? { user_id: params.user_id || userInfo.userId } : {}),
    ...(params.grade_level && { grade_level: params.grade_level })
  })

  const url = agentUrl(`/v1/${role}/chat/stream?${queryParams.toString()}`)
  const eventSource = new EventSource(url)

  let fullReply = ''

  eventSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.content) {
        fullReply += data.content
        onChunk(fullReply, data.content)
      }
      if (data.done) {
        eventSource.close()
        if (onDone) onDone(fullReply)
      }
    } catch (e) {
      // 忽略非 JSON 数据块
    }
  }

  eventSource.onerror = () => {
    eventSource.close()
    if (onError) onError(new Error('SSE 连接错误'))
  }

  return eventSource
}

/**
 * 获取聊天历史
 * @param {string} threadId
 * @returns {Promise<Array>}
 */
export function fetchChatHistory(threadId) {
  return request.get(`/mobile/chat/history/${threadId}`)
}

/**
 * 清除聊天会话
 * @param {string} threadId
 */
export function clearChatSession(threadId) {
  return request.delete(`/mobile/chat/clear/${threadId}`)
}
