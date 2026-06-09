import request from './request'

/**
 * 获取数据字典列表
 * @param {string} type - 如 data_school_subject
 */
export function fetchDictItems(type) {
  return request.get(`/mobile/dict/${type}`)
}
