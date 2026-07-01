import request from './request'
import { getApiBaseURL } from './config'
import { getAccessToken, getUserInfo } from '@/utils/authStorage'

/**
 * 发送聊天消息（非流式，同步模式）
 * @param {object} params - { message, thread_id?, role?, user_name?, user_id?, grade_level?, experiment_title?, grade_level_name? }
 * @returns {Promise<{reply: string, thread_id: string}>}
 */
export function sendChatMessage(params) {
  return request.post('/mobile/chat/send', params)
}

/**
 * 发送聊天消息（流式 SSE）
 * 使用 POST + ReadableStream 方式，通过 Java 后端 /api/mobile/chat/stream
 *
 * @param {object} params - { message, thread_id?, role?, user_name?, user_id?, grade_level? }
 * @param {function} onChunk - 收到每个文本块的回调 (fullText, deltaText)
 * @param {function} onDone - 流结束的回调 (fullText)
 * @param {function} onError - 出错回调
 */
export async function sendChatMessageStream(params, onChunk, onDone, onError) {
  try {
    const baseURL = getApiBaseURL()

    const userInfo = getUserInfo()
    const response = await fetch(`${baseURL}/mobile/chat/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getAccessToken()}`,
        'X-User-Id': userInfo.userId || '',
        'X-User-Role-Id': userInfo.userRoleId || '',
        'X-User-Root-Org-Id': userInfo.rootOrgId || ''
      },
      body: JSON.stringify(params)
    })

    if (!response.ok) {
      throw new Error(`SSE request failed: ${response.status}`)
    }
    if (!response.body) {
      throw new Error('ReadableStream not supported in this browser')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let fullReply = ''
    let diagnosisReport = null

    const finish = () => {
      onDone?.(fullReply, { diagnosisReport })
    }

    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        if (fullReply || diagnosisReport) finish()
        else onDone?.()
        break
      }

      buffer += decoder.decode(value, { stream: true })

      // Parse SSE frames: "data: {json}\n\n"
      const lines = buffer.split('\n')
      buffer = lines.pop() || '' // keep incomplete line

      const DATA_PREFIX = 'data:'
      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed.startsWith(DATA_PREFIX)) continue
        const data = trimmed.slice(DATA_PREFIX.length).trim()
        if (data === '[DONE]') {
          finish()
          return
        }
        try {
          const parsed = JSON.parse(data)
          if (parsed.type === 'diagnosis_report' && parsed.report) {
            diagnosisReport = parsed.report
          }
          if (parsed.content) {
            fullReply += parsed.content
            onChunk?.(fullReply, parsed.content)
          }
          if (parsed.done) {
            finish()
            return
          }
        } catch {
          // Skip non-JSON data lines
        }
      }
    }
  } catch (e) {
    console.error('[chat stream] error:', e)
    onError?.(e)
  }
}

/**
 * 生成实验方案（同步，120s 超时）
 * @param {object} params - { experiment_title, grade_level, user_name?, user_id?, thread_id? }
 * @returns {Promise<{ plans: Array, thread_id: string, reply: string }>}
 */
export function generatePlans(params) {
  const title = (params.experiment_title || '').trim()
  const grade = (params.grade_level || '').trim()
  return request.post('/mobile/chat/sync', {
    message: title
      ? `请为「${title}」设计 3 个适合${grade || '小学'}学生的实验方案`
      : '请设计 3 个科学实验方案',
    role: 'plan_design',
    experiment_title: title,
    grade_level: grade,
    user_name: params.user_name,
    user_id: params.user_id,
    thread_id: params.thread_id,
  }, {
    timeout: 120000
  })
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
