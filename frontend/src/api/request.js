import axios from 'axios'

/** 普通 API 请求超时（毫秒） */
const DEFAULT_TIMEOUT_MS = 50_000

/** 大文件上传超时：与 Spring multipart 1GB、Nginx client_max_body_size 配套 */
export const UPLOAD_TIMEOUT_MS = 2 * 60 * 60 * 1000

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8010/api',
  timeout: DEFAULT_TIMEOUT_MS
})

request.interceptors.request.use((config) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  config.headers['X-User-Id'] = userInfo.userId || ''
  config.headers['X-User-Role-Id'] = userInfo.userRoleId || ''
  config.headers['X-User-Root-Org-Id'] = userInfo.rootOrgId || ''
  // FormData 必须由浏览器自动带 boundary，勿手写 multipart/form-data
  if (config.data instanceof FormData && config.headers) {
    delete config.headers['Content-Type']
  }
  return config
})

export default request
