/** 页面导航层级（底栏与返回互斥，immersive-tab 为 Tab 入口的全屏例外） */
export const NAV_TAB = 'tab'
export const NAV_IMMERSIVE_TAB = 'immersive-tab'
export const NAV_STACK = 'stack'
export const NAV_DETAIL = 'detail'
export const NAV_AUTH = 'auth'

/** 底部 Tab 根路由：有底栏、无返回 */
export const BOTTOM_TAB_ROUTES = [
  '/home',
  '/experiments',
  '/tasks',
  '/admin',
  '/chat',
  '/profile',
  '/content-audits'
]

export function showBottomNavForMeta(nav) {
  return nav === NAV_TAB
}

export function showBackForMeta(nav) {
  return nav === NAV_STACK || nav === NAV_DETAIL || nav === NAV_IMMERSIVE_TAB
}
