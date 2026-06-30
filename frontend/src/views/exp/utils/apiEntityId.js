/** 从 ApiResponse 中解析单条记录 ID（如 stepId / resultId） */
export function resolveApiEntityId(res, fieldNames = ['stepId', 'id', 'value', 'result']) {
  const body = res?.data
  if (!body || body.code !== 200) return ''
  const data = body.data
  if (typeof data === 'string' || typeof data === 'number') {
    return String(data).trim()
  }
  if (data && typeof data === 'object') {
    for (const key of fieldNames) {
      const value = data[key]
      if (value != null && String(value).trim()) {
        return String(value).trim()
      }
    }
  }
  return ''
}
