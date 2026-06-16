/** 钉钉 OAuth 回调根地址，须与开放平台登记一致 */
export function getDingTalkRedirectBase() {
  if (typeof window === 'undefined') return ''
  const { origin, pathname, port } = window.location
  if (port === '8010' && pathname.startsWith('/m')) {
    const basePath = pathname.replace(/\/$/, '') || '/m'
    return `${origin}${basePath}`
  }
  return origin
}
