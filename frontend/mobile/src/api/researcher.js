import request from './request'

export function fetchResearcherExpAudits(params = {}) {
  return request.get('/mobile/researcher/exp-audits', {
    params: {
      expType: params.expType || undefined,
      page: params.page || 1,
      size: params.size || 20
    }
  })
}

export function fetchResearcherExpAuditPendingCount() {
  return request.get('/mobile/researcher/exp-audits/pending-count')
}

export function fetchResearcherExpAuditDetail(expId) {
  return request.get(`/mobile/researcher/exp-audits/${expId}`)
}

export function auditResearcherExp(expId, payload) {
  return request.patch(`/mobile/researcher/exp-audits/${expId}`, payload)
}
