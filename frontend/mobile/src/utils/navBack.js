/**
 * 统一返回：优先 history.back，无历史则跳转 fallback
 */
export function goBack(router, fallback = '/home') {
  if (!router) return
  if (typeof window !== 'undefined' && window.history.length > 1) {
    router.back()
    return
  }
  router.push(fallback)
}

/** 底部 Tab 根路由 */
export const BOTTOM_TAB_ROUTES = [
  '/home',
  '/experiments',
  '/tasks',
  '/assign',
  '/chat',
  '/profile'
]
