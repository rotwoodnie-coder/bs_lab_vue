import request from './request'

export function fetchPointsLedger(params = {}) {
  return request.get('/mobile/points/ledger', {
    params: {
      page: params.page || 1,
      size: params.size || 20
    }
  })
}
