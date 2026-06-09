import request from './request'
import { rawAuthClient } from '@/bootstrap/auth'
import { getApiBaseURL } from './config'
import { getDeviceId } from '@/utils/deviceId'

/** 移动端登录（支持 login_name / user_phone；审核中账号给出明确提示） */
export function login(credentials) {
  return request.post('/mobile/auth/login', {
    username: credentials.loginName,
    password: credentials.loginPwd,
    deviceId: getDeviceId()
  })
}

/** 登录接口不走 Bearer，使用 raw 客户端 */
export function loginRaw(credentials) {
  return rawAuthClient.post(`${getApiBaseURL()}/mobile/auth/login`, {
    username: credentials.loginName,
    password: credentials.loginPwd,
    deviceId: getDeviceId()
  }).then((res) => res.data)
}

export function fetchSession() {
  return request.get('/mobile/auth/session')
}

export function logoutApi() {
  return request.post('/mobile/auth/logout', {
    refreshToken: localStorage.getItem('refreshToken') || '',
    deviceId: getDeviceId()
  })
}

export function checkLoginNameAvailable(loginName) {
  return request.get('/mobile/auth/login-name/available', { params: { loginName } })
}

export function registerParent(payload) {
  return request.post('/mobile/auth/parent/register', payload)
}
