import request from './request'

/**
 * 分页查询模拟实验
 * @param {object} params - { keyword, status, pageNum, pageSize }
 */
export function fetchSimulators(params = {}) {
  return request.get('/mobile/simulators', {
    params: {
      status: params.status || 'y',
      keyword: params.keyword || undefined,
      gradeKey: params.gradeKey || 'all',
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 50
    }
  })
}

/**
 * 获取模拟实验详情
 * @param {string} simulatorId
 */
export function fetchSimulatorDetail(simulatorId) {
  return request.get(`/mobile/simulators/${simulatorId}`)
}
