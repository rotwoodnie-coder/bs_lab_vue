import request from './request'

export function startCreativeTask() {
  return request.post('/mobile/creative/start')
}
