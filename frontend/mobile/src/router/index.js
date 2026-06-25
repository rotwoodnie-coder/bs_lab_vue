import { createRouter, createWebHashHistory } from 'vue-router'

import { isLoggedIn, getUserInfo } from '@/utils/authStorage'

import { normalizeRole } from '@/utils/role'

import {
  isParentRestricted,
  isParentRoleUser,
  shouldShowParentApprovalCelebration,
  syncParentAccessState,
  PARENT_AUDIT_PATH
} from '@/utils/parentAccess'

import { useUserStore } from '@/stores/user'
import { useProfileStore } from '@/stores/profile'
import {
  NAV_TAB,
  NAV_IMMERSIVE_TAB,
  NAV_STACK,
  NAV_DETAIL,
  NAV_AUTH
} from '@/utils/pageNav'

const routes = [
  { path: '/', redirect: '/home' },

  { path: '/login', name: 'mobile-login', component: () => import('@/views/LoginView.vue'), meta: { public: true, nav: NAV_AUTH } },

  { path: '/legal/terms', name: 'legal-terms', component: () => import('@/views/legal/LegalDocumentView.vue'), meta: { public: true, nav: NAV_STACK, backFallback: '/login' } },

  { path: '/legal/privacy', name: 'legal-privacy', component: () => import('@/views/legal/LegalDocumentView.vue'), meta: { public: true, nav: NAV_STACK, backFallback: '/login' } },

  { path: '/register/parent', name: 'parent-register', component: () => import('@/views/auth/ParentRegisterView.vue'), meta: { public: true, nav: NAV_AUTH } },

  { path: '/bind-child', name: 'bind-child', component: () => import('@/views/bind/BindChildView.vue'), meta: { nav: NAV_AUTH } },

  { path: '/bind-success', name: 'bind-success', component: () => import('@/views/bind/BindSuccessView.vue'), meta: { public: true, nav: NAV_AUTH } },

  { path: '/home', name: 'home', component: () => import('@/views/home/HomeView.vue'), meta: { nav: NAV_TAB } },

  { path: '/search', name: 'search', component: () => import('@/views/search/SearchView.vue'), meta: { nav: NAV_STACK, backFallback: '/home' } },

  { path: '/search/voice', name: 'voice-search', component: () => import('@/views/search/VoiceSearchView.vue'), meta: { nav: NAV_STACK, backFallback: '/search' } },

  { path: '/video/:id', name: 'video-detail', component: () => import('@/views/content/ContentDetailView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/home' } },

  { path: '/exp/:id', name: 'exp-detail', component: () => import('@/views/content/ContentDetailView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/home' } },

  { path: '/notifications', name: 'notifications', component: () => import('@/views/notification/NotificationListView.vue'), meta: { nav: NAV_STACK, backFallback: '/home' } },

  { path: '/notifications/notice/:id', name: 'notice-detail', component: () => import('@/views/notification/NotificationDetailView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/notifications' } },

  { path: '/notifications/:id', name: 'notification-detail', component: () => import('@/views/notification/NotificationDetailView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/notifications' } },

  { path: '/chat', name: 'assistant-chat', component: () => import('@/views/assistant/AssistantChatView.vue'), meta: { nav: NAV_IMMERSIVE_TAB, backFallback: '/home' } },

  { path: '/parenting', redirect: '/chat' },

  { path: '/teaching', redirect: '/settings' },

  { path: '/teacher-ai', redirect: '/chat' },

  { path: '/experiments', name: 'experiments', component: () => import('@/views/experiment/VirtualExpView.vue'), meta: { nav: NAV_TAB } },

  { path: '/experiments/search', name: 'experiments-search', component: () => import('@/views/experiment/VirtualExpSearchView.vue'), meta: { nav: NAV_STACK, backFallback: '/experiments' } },

  { path: '/sim/:id', name: 'sim-detail', component: () => import('@/views/experiment/VirtualExpDetailView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/experiments' } },

  { path: '/tasks', name: 'tasks', component: () => import('@/views/tasks/TasksView.vue'), beforeEnter: tasksEntryGuard, meta: { nav: NAV_TAB, roles: ['student', 'parent', 'teacher', 'researcher'] } },

  { path: '/tasks/:taskId/summary', name: 'task-summary', component: () => import('@/views/teacher/TaskSummaryView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/tasks', roles: ['teacher'] } },

  { path: '/tasks/:taskId/submissions/:workId', name: 'task-submission', component: () => import('@/views/teacher/TaskSubmissionView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/tasks', roles: ['teacher'] } },

  { path: '/tasks/:id', name: 'task-detail', component: () => import('@/views/tasks/TaskDetailView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/tasks', roles: ['student', 'parent'] } },

  { path: '/works', name: 'works', component: () => import('@/views/works/WorksView.vue'), meta: { nav: NAV_STACK, backFallback: '/profile', roles: ['student'] } },

  { path: '/works/:id', name: 'work-detail', component: () => import('@/views/works/WorkDetailView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/works' } },

  { path: '/upload', name: 'upload', component: () => import('@/views/upload/UploadView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/profile', roles: ['student', 'parent'] } },

  { path: '/badges', name: 'badges', component: () => import('@/views/badges/BadgeWallView.vue'), meta: { nav: NAV_STACK, backFallback: '/profile', roles: ['student'] } },

  { path: '/points', name: 'points-ledger', component: () => import('@/views/points/PointsLedgerView.vue'), meta: { nav: NAV_STACK, backFallback: '/profile', roles: ['student'] } },

  { path: '/growth', name: 'growth', component: () => import('@/views/growth/GrowthArchiveView.vue'), meta: { nav: NAV_STACK, backFallback: '/profile', roles: ['student', 'parent'] } },

  { path: '/quiz', name: 'quiz', component: () => import('@/views/quiz/QuizView.vue'), meta: { nav: NAV_STACK, backFallback: '/tasks', roles: ['student'] } },

  { path: '/quiz/history', name: 'quiz-history', component: () => import('@/views/quiz/QuizHistoryView.vue'), meta: { nav: NAV_STACK, backFallback: '/quiz', roles: ['student'] } },

  { path: '/quiz/submit', name: 'quiz-submit', component: () => import('@/views/quiz/QuizSubmitView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/quiz', roles: ['student'] } },

  { path: '/quiz/completed', name: 'quiz-completed', component: () => import('@/views/quiz/QuizCompletedView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/tasks', roles: ['student'] } },

  { path: '/quiz/review', name: 'quiz-review', component: () => import('@/views/quiz/QuizReviewView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/quiz', roles: ['student'] } },

  { path: '/quiz/result/:type', name: 'quiz-result', component: () => import('@/views/quiz/QuizResultView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/quiz', roles: ['student'] } },

  { path: '/assign', name: 'assign', component: () => import('@/views/teacher/AssignView.vue'), meta: { nav: NAV_STACK, backFallback: '/tasks', roles: ['teacher'] } },

  { path: '/review', redirect: '/audits' },

  { path: '/board', redirect: (to) => {
    if (to.query.taskId) return { path: `/tasks/${to.query.taskId}/summary`, replace: true }
    return { path: '/tasks', replace: true }
  }},

  { path: '/audits', name: 'audits', component: () => import('@/views/audits/AuditsHubView.vue'), meta: { nav: NAV_STACK, backFallback: '/profile', roles: ['teacher', 'researcher', 'admin'] } },

  { path: '/content-audits', name: 'content-audits', component: () => import('@/views/researcher/ContentAuditsView.vue'), meta: { nav: NAV_TAB, roles: ['researcher', 'admin'] } },

  { path: '/content-audits/:expId', name: 'content-audit-detail', component: () => import('@/views/researcher/ContentAuditDetailView.vue'), meta: { nav: NAV_DETAIL, backFallback: '/content-audits', roles: ['researcher', 'admin'] } },

  { path: '/parent-binds', name: 'parent-binds', component: () => import('@/views/teacher/ParentBindAuditView.vue'), meta: { nav: NAV_STACK, backFallback: '/profile', roles: ['teacher'] } },

  { path: '/admin', name: 'admin-hub', component: () => import('@/views/admin/AdminHubView.vue'), meta: { nav: NAV_TAB, roles: ['admin'] } },

  { path: '/admin/badges', name: 'admin-badges', component: () => import('@/views/admin/AdminBadgeRulesView.vue'), meta: { nav: NAV_STACK, backFallback: '/admin', roles: ['admin'] } },

  { path: '/admin/quiz-config', name: 'admin-quiz-config', component: () => import('@/views/admin/AdminQuizConfigView.vue'), meta: { nav: NAV_STACK, backFallback: '/admin', roles: ['admin'] } },

  { path: '/admin/parent-registrations', name: 'admin-parent-registrations', component: () => import('@/views/admin/AdminParentRegistrationsView.vue'), meta: { nav: NAV_STACK, backFallback: '/admin', roles: ['admin'] } },

  { path: '/admin/work-reviews', name: 'admin-work-reviews', component: () => import('@/views/admin/AdminWorkReviewsView.vue'), meta: { nav: NAV_STACK, backFallback: '/admin', roles: ['admin'] } },

  { path: '/profile', name: 'profile', component: () => import('@/views/profile/ProfileView.vue'), meta: { nav: NAV_TAB } },

  { path: '/settings', name: 'settings', component: () => import('@/views/settings/SettingsView.vue'), meta: { nav: NAV_STACK, backFallback: '/profile' } },

  { path: '/settings/dingtalk/callback', name: 'dingtalk-callback', component: () => import('@/views/settings/DingTalkCallbackView.vue'), meta: { nav: NAV_AUTH } }
]

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes,
  linkExactActiveClass: 'active'
})

function homeForRole() {
  return '/home'
}

function tasksEntryGuard() {
  const userInfo = getUserInfo()
  const role = normalizeRole(userInfo?.userRoleId)

  if (role === 'admin') {
    return { path: '/admin', replace: true }
  }

  if (role === 'researcher') {
    return { path: '/content-audits', replace: true }
  }

  return true
}

function parentAuditRedirect() {
  return { path: PARENT_AUDIT_PATH, query: { from: 'bind' }, replace: true }
}

const PARENT_PATHS_WHEN_RESTRICTED = ['/bind-success', '/bind-child']

function applyUserInfoToStore() {
  try {
    const store = useUserStore()
    store.userInfo = getUserInfo()
  } catch {
    // pinia not ready
  }
}

router.beforeEach(async (to) => {
  if (to.path === '/login' || to.path === '/register/parent') {
    return true
  }

  if (!isLoggedIn()) {
    if (to.meta?.public) return true
    return '/login'
  }

  if (to.meta?.public) return true

  try {
    useProfileStore().loadProfile()
  } catch {
    // pinia not ready
  }

  if (isParentRoleUser()) {
    await syncParentAccessState()
    applyUserInfoToStore()
  }

  const userInfo = getUserInfo()

  if (isParentRoleUser(userInfo)) {
    if (isParentRestricted(userInfo)) {
      if (!PARENT_PATHS_WHEN_RESTRICTED.includes(to.path)) {
        return parentAuditRedirect()
      }
      return true
    }

    if (shouldShowParentApprovalCelebration(userInfo)) {
      if (to.path !== PARENT_AUDIT_PATH) {
        return { path: PARENT_AUDIT_PATH, query: { from: 'approved' }, replace: true }
      }
      return true
    }

    if (to.path === PARENT_AUDIT_PATH) {
      return { path: homeForRole('parent'), replace: true }
    }
  }

  const roles = to.meta?.roles
  if (roles?.length) {
    const role = normalizeRole(userInfo?.userRoleId)
    if (!roles.includes(role)) {
      return homeForRole(role)
    }
  }

  return true
})

export default router
