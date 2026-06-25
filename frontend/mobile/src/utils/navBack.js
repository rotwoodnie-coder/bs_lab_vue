import { BOTTOM_TAB_ROUTES } from '@/utils/pageNav'

export { BOTTOM_TAB_ROUTES }

/**
 * 统一返回：优先 history.back，无历史则跳转 fallback
 * 底部 Tab 根路由（一级页面）直接跳转 fallback，避免 router.back 回退到非预期页
 */
export function goBack(router, fallback = '/home') {
  if (!router) return

  const currentPath = router.currentRoute?.value?.path || ''
  if (BOTTOM_TAB_ROUTES.includes(currentPath)) {
    router.push(fallback)
    return
  }

  if (typeof window !== 'undefined' && window.history.length > 1) {
    router.back()
    return
  }
  router.push(fallback)
}
