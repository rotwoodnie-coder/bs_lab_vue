/**
 * API 地址解析
 * - 生产 /m/（Nginx 或 8010 托管）：同源 /api
 * - Vite 开发模式：走 devServer 代理
 * - LAN 直连：http://IP:8010/api（仅内网调试）
 */

function lanHostname() {
  if (typeof window === 'undefined') return null
  const hostname = window.location.hostname
  if (!hostname || hostname === 'localhost' || hostname === '127.0.0.1') return null
  return hostname
}

/** 访问路径在 /m/ 下（Nginx 静态或后端 8010 托管）→ 同源 /api */
function isMobileDeployed() {
  if (typeof window === 'undefined') return false
  return window.location.pathname.startsWith('/m')
}

function isViteDevServer() {
  if (typeof window === 'undefined') return false
  const port = window.location.port
  return port === '5174' || port === '5173' || port === '8080'
}

/** Java 后端 API 根路径 */
export function getApiBaseURL() {
  const fromEnv = import.meta.env.VITE_API_BASE
  if (fromEnv) return fromEnv.replace(/\/$/, '')

  if (isMobileDeployed()) {
    return '/api'
  }

  // Vite 开发模式走 devServer 代理 → 本机 8010
  if (isViteDevServer()) {
    return '/api'
  }

  const host = lanHostname()
  if (host) {
    const port = import.meta.env.VITE_API_PORT || '8010'
    return `http://${host}:${port}/api`
  }
  return '/api'
}

/** AI 对话服务根路径 */
export function getAgentBaseURL() {
  const fromEnv = import.meta.env.VITE_AGENT_BASE
  if (fromEnv) return fromEnv.replace(/\/$/, '')

  const host = lanHostname()
  if (host) {
    const port = import.meta.env.VITE_AGENT_PORT || '5001'
    return `http://${host}:${port}`
  }
  return ''
}

export function agentUrl(path) {
  const base = getAgentBaseURL()
  return base ? `${base}${path}` : path
}
