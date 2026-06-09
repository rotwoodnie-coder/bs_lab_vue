import request from './request'

/**
 * 发起拍同款：同一实验若已有进行中任务则返回 409 及现有任务
 * @param {{ expId?: string, workId?: string }} payload
 */
export function startRemix(payload) {
  const body = typeof payload === 'string' ? { expId: payload } : payload
  return request.post('/mobile/remix/start', body)
}
