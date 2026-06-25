import { isAdminRole } from '@/utils/role'
import { NAV_TAB } from '@/utils/pageNav'

/** 根据 Tab 根路由同步底部导航高亮 */
export function syncActiveTabFromRoute(path, userRoleId, setActiveTab) {
  if (path === '/home') {
    setActiveTab('home')
    return
  }
  if (path === '/experiments') {
    setActiveTab('experiments')
    return
  }
  if (path === '/tasks') {
    setActiveTab(isAdminRole(userRoleId) ? 'admin' : 'tasks')
    return
  }
  if (path === '/content-audits') {
    setActiveTab(isAdminRole(userRoleId) ? 'admin' : 'tasks')
    return
  }
  if (path === '/admin') {
    setActiveTab('admin')
    return
  }
  if (path === '/profile') {
    setActiveTab('profile')
    return
  }
}

export function isTabRootRoute(route) {
  return route.meta?.nav === NAV_TAB
}
