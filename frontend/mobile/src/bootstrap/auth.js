import axios from 'axios'
import { getApiBaseURL } from '@/api/config'
import { getDeviceId } from '@/utils/deviceId'
import {
  getAccessToken,
  getRefreshToken,
  persistAuthSession,
  buildUserInfoFromLogin,
  clearAuthSession
} from '@/utils/authStorage'

/** 无拦截器的原始客户端，供 refresh 使用避免循环 */
export const rawAuthClient = axios.create({ timeout: 15000 })

let refreshPromise = null

export async function refreshAccessToken() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error('NO_REFRESH_TOKEN')
  }
  if (refreshPromise) {
    return refreshPromise
  }
  refreshPromise = rawAuthClient.post(`${getApiBaseURL()}/mobile/auth/refresh`, {
    refreshToken,
    deviceId: getDeviceId()
  }).then((response) => {
    const res = response.data
    if (res?.code !== 200 || !res.data) {
      throw new Error(res?.message || 'REFRESH_FAILED')
    }
    const userInfo = buildUserInfoFromLogin(res.data)
    persistAuthSession(res.data, userInfo)
    return res.data
  }).finally(() => {
    refreshPromise = null
  })
  return refreshPromise
}

export function clearAuthAndRedirectLogin() {
  clearAuthSession()
  if (typeof window !== 'undefined') {
    window.location.hash = '#/login'
  }
}

export async function bootstrapAuthSession() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    clearAuthSession()
    return false
  }
  try {
    await refreshAccessToken()
    return true
  } catch {
    clearAuthSession()
    return false
  }
}
