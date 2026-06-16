import request from './request'

export function fetchAuditSummary() {
  return request.get('/mobile/audits/summary')
}
