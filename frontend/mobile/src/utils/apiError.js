/** 从 axios / 业务响应中提取可读错误文案 */
export function apiMessage(err, fallback = '操作失败') {
  if (!err) return fallback
  if (typeof err === 'string') return err
  if (err.message && err.code !== undefined && !err.response) {
    return err.message
  }
  return err?.response?.data?.message || err?.message || fallback
}

/** 后端字段校验错误列表 → 前端 fieldErrors 对象 */
export function mapFieldErrors(errors, fieldMap = {}) {
  if (!Array.isArray(errors) || !errors.length) return {}
  const out = {}
  for (const item of errors) {
    const key = fieldMap[item.field] || item.field
    if (key && item.message) out[key] = item.message
  }
  return out
}

export function apiFieldErrors(err) {
  const list = err?.response?.data?.data
  if (Array.isArray(list)) return list
  return []
}
