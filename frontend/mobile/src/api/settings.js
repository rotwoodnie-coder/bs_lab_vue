import request from './request'

export function fetchPreferences() {
  return request.get('/mobile/settings/preferences')
}

export function savePreferences(payload) {
  return request.put('/mobile/settings/preferences', payload)
}

export function fetchAccountSecurity() {
  return request.get('/mobile/settings/account')
}

export function fetchDingTalkAuthorizeUrl(redirectBase) {
  return request.get('/mobile/settings/dingtalk/authorize-url', {
    params: redirectBase ? { redirectBase } : undefined
  })
}

export function fetchDingTalkStatus() {
  return request.get('/mobile/settings/dingtalk/status')
}

export function bindDingTalk(payload) {
  return request.post('/mobile/settings/dingtalk/bind', payload)
}

export function unbindDingTalk() {
  return request.delete('/mobile/settings/dingtalk/bind')
}
