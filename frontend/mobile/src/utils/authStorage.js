const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'
const TOKEN_EXPIRES_KEY = 'tokenExpiresAt'
const USER_INFO_KEY = 'userInfo'
export { USER_INFO_KEY }
const LEGACY_TOKEN_KEY = 'token'

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY) || localStorage.getItem(LEGACY_TOKEN_KEY) || ''
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || ''
}

export function getTokenExpiresAt() {
  const raw = localStorage.getItem(TOKEN_EXPIRES_KEY)
  return raw ? Number(raw) : 0
}

export function getUserInfo() {
  try {
    return JSON.parse(localStorage.getItem(USER_INFO_KEY) || '{}')
  } catch {
    return {}
  }
}

export function isLoggedIn() {
  const token = getAccessToken()
  if (!token || token.startsWith('mock-token-')) {
    return !!getRefreshToken()
  }
  return true
}

export function isAccessTokenExpired() {
  const expiresAt = getTokenExpiresAt()
  if (!expiresAt) return false
  return Date.now() >= expiresAt - 60_000
}

export function persistAuthSession(payload, userInfo) {
  const accessToken = payload?.token || payload?.accessToken || ''
  const refreshToken = payload?.refreshToken || ''
  const expiresIn = Number(payload?.expiresIn || 7200)

  if (accessToken) {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(LEGACY_TOKEN_KEY, accessToken)
  }
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  }
  localStorage.setItem(TOKEN_EXPIRES_KEY, String(Date.now() + expiresIn * 1000))
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo || {}))
}

export function clearAuthSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(TOKEN_EXPIRES_KEY)
  localStorage.removeItem(LEGACY_TOKEN_KEY)
  localStorage.removeItem(USER_INFO_KEY)
}

export function buildUserInfoFromLogin(data) {
  if (!data) return {}
  const {
    token,
    refreshToken,
    expiresIn,
    tokenType,
    ...userInfo
  } = data
  return userInfo
}
