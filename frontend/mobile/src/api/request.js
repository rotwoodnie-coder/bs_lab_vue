import axios from 'axios'
import { getApiBaseURL } from './config'
import { getAccessToken, getUserInfo } from '@/utils/authStorage'
import { refreshAccessToken, clearAuthAndRedirectLogin } from '@/bootstrap/auth'

const request = axios.create({
  timeout: 50000
})

request.interceptors.request.use((config) => {
  config.baseURL = getApiBaseURL()

  const accessToken = getAccessToken()
  if (accessToken && !accessToken.startsWith('mock-token-')) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }

  const userInfo = getUserInfo()
  if (userInfo.userId) {
    config.headers['X-User-Id'] = userInfo.userId
    config.headers['X-User-Role-Id'] = userInfo.userRoleId || ''
    config.headers['X-User-Root-Org-Id'] = userInfo.rootOrgId || ''
  }
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const original = error.config
    const status = error.response?.status

    if (status === 401 && original && !original._retry && !original.url?.includes('/mobile/auth/refresh')) {
      original._retry = true
      try {
        await refreshAccessToken()
        original.headers = original.headers || {}
        original.headers.Authorization = `Bearer ${getAccessToken()}`
        return request(original)
      } catch {
        clearAuthAndRedirectLogin()
        return Promise.reject(error)
      }
    }

    if (status === 401) {
      clearAuthAndRedirectLogin()
    }
    return Promise.reject(error)
  }
)

export default request
