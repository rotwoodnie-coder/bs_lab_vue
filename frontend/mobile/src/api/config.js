/**
 * API 地址解析
 * - 后端托管 /m/ 时：同源 /api（8010 单端口，PC端+移动端共用）
 * - Vite 开发模式时：走 devServer 代理（移动端 Vite 5174/5173）
 */

function lanHostname() {
  if (typeof window === 'undefined') return null
  const hostname = window.location.hostname
  if (!hostname || hostname === 'localhost' || hostname === '127.0.0.1') return null
  return hostname
}

function isBackendHosted() {
  if (typeof window === 'undefined') return false
  const { port, pathname } = window.location
  // 后端 Spring Boot 托管 /m/ 路径（单端口方案：8010）
  return port === '8010' && pathname.startsWith('/m')
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

  if (isBackendHosted()) {
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
