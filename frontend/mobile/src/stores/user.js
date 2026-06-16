import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getAccessToken,
  getRefreshToken,
  getUserInfo,
  persistAuthSession,
  clearAuthSession,
  buildUserInfoFromLogin,
  isLoggedIn as storageIsLoggedIn
} from '@/utils/authStorage'
import { clearAuthAndRedirectLogin } from '@/bootstrap/auth'
import { getDeviceId } from '@/utils/deviceId'
import { rawAuthClient } from '@/bootstrap/auth'
import { getApiBaseURL } from '@/api/config'
import { useProfileStore } from '@/stores/profile'

export const useUserStore = defineStore('user', () => {
  const token = ref(getAccessToken())
  const refreshToken = ref(getRefreshToken())
  const userInfo = ref(getUserInfo())

  const isLoggedIn = computed(() => storageIsLoggedIn())

  function applyTokenPayload(payload) {
    const info = buildUserInfoFromLogin(payload)
    persistAuthSession(payload, info)
    token.value = getAccessToken()
    refreshToken.value = getRefreshToken()
    userInfo.value = info
  }

  function setLogin(loginPayload, info) {
    if (loginPayload?.token || loginPayload?.refreshToken) {
      applyTokenPayload(loginPayload)
      if (info && Object.keys(info).length) {
        userInfo.value = { ...userInfo.value, ...info }
        persistAuthSession(loginPayload, userInfo.value)
      }
      return
    }
    token.value = loginPayload || ''
    userInfo.value = info || {}
    persistAuthSession({ token: token.value }, userInfo.value)
  }

  async function logout() {
    const rt = getRefreshToken()
    try {
      if (rt) {
        await rawAuthClient.post(`${getApiBaseURL()}/mobile/auth/logout`, {
          refreshToken: rt,
          deviceId: getDeviceId()
        })
      }
    } catch {
      // ignore network errors on logout
    }
    token.value = ''
    refreshToken.value = ''
    userInfo.value = {}
    clearAuthSession()
    try {
      useProfileStore().reset()
    } catch {
      // pinia may not be ready
    }
  }

  function logoutAndRedirect() {
    logout().finally(() => clearAuthAndRedirectLogin())
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    setLogin,
    applyTokenPayload,
    logout,
    logoutAndRedirect
  }
})
