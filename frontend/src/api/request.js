import axios from 'axios'

const request = axios.create({
  baseURL: 'http://127.0.0.1:8010/api',
  //baseURL: 'http://10.0.181.203:8011/api',
  timeout: 50000
})

request.interceptors.request.use((config) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  config.headers['X-User-Id'] = userInfo.userId || ''
  config.headers['X-User-Role-Id'] = userInfo.userRoleId || ''
  config.headers['X-User-Root-Org-Id'] = userInfo.rootOrgId || ''
  return config
})

export default request
