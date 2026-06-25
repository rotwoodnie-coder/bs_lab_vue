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



const routes = [

  { path: '/', redirect: '/home' },

  { path: '/login', name: 'mobile-login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },

  { path: '/legal/terms', name: 'legal-terms', component: () => import('@/views/legal/LegalDocumentView.vue'), meta: { public: true } },

  { path: '/legal/privacy', name: 'legal-privacy', component: () => import('@/views/legal/LegalDocumentView.vue'), meta: { public: true } },

  { path: '/register/parent', name: 'parent-register', component: () => import('@/views/auth/ParentRegisterView.vue'), meta: { public: true } },

  { path: '/bind-child', name: 'bind-child', component: () => import('@/views/bind/BindChildView.vue') },

  { path: '/bind-success', name: 'bind-success', component: () => import('@/views/bind/BindSuccessView.vue'), meta: { public: true } },

  { path: '/home', name: 'home', component: () => import('@/views/home/HomeView.vue') },

  { path: '/search', name: 'search', component: () => import('@/views/search/SearchView.vue') },

  { path: '/search/voice', name: 'voice-search', component: () => import('@/views/search/VoiceSearchView.vue') },

  { path: '/video/:id', name: 'video-detail', component: () => import('@/views/content/ContentDetailView.vue') },

  { path: '/exp/:id', name: 'exp-detail', component: () => import('@/views/content/ContentDetailView.vue') },

  { path: '/notifications', name: 'notifications', component: () => import('@/views/notification/NotificationListView.vue') },

  { path: '/notifications/notice/:id', name: 'notice-detail', component: () => import('@/views/notification/NotificationDetailView.vue') },

  { path: '/notifications/:id', name: 'notification-detail', component: () => import('@/views/notification/NotificationDetailView.vue') },

  { path: '/chat', name: 'assistant-chat', component: () => import('@/views/assistant/AssistantChatView.vue') },

  { path: '/parenting', redirect: '/chat' },

  { path: '/teaching', redirect: '/settings' },

  { path: '/teacher-ai', redirect: '/chat' },

  { path: '/experiments', name: 'experiments', component: () => import('@/views/experiment/VirtualExpView.vue') },

  { path: '/experiments/search', name: 'experiments-search', component: () => import('@/views/experiment/VirtualExpSearchView.vue') },

  { path: '/sim/:id', name: 'sim-detail', component: () => import('@/views/experiment/VirtualExpDetailView.vue') },

  { path: '/tasks', name: 'tasks', component: () => import('@/views/tasks/TasksView.vue'), beforeEnter: tasksEntryGuard, meta: { roles: ['student', 'parent', 'teacher', 'researcher'] } },

  { path: '/tasks/:taskId/summary', name: 'task-summary', component: () => import('@/views/teacher/TaskSummaryView.vue'), meta: { roles: ['teacher'] } },

  { path: '/tasks/:taskId/submissions/:workId', name: 'task-submission', component: () => import('@/views/teacher/TaskSubmissionView.vue'), meta: { roles: ['teacher'] } },

  { path: '/tasks/:id', name: 'task-detail', component: () => import('@/views/tasks/TaskDetailView.vue'), meta: { roles: ['student', 'parent'] } },

  { path: '/works', name: 'works', component: () => import('@/views/works/WorksView.vue'), meta: { roles: ['student'] } },

  { path: '/works/:id', name: 'work-detail', component: () => import('@/views/works/WorkDetailView.vue') },

  { path: '/upload', name: 'upload', component: () => import('@/views/upload/UploadView.vue'), meta: { roles: ['student', 'parent'] } },

  { path: '/badges', name: 'badges', component: () => import('@/views/badges/BadgeWallView.vue'), meta: { roles: ['student'] } },

  { path: '/points', name: 'points-ledger', component: () => import('@/views/points/PointsLedgerView.vue'), meta: { roles: ['student'] } },

  { path: '/growth', name: 'growth', component: () => import('@/views/growth/GrowthArchiveView.vue'), meta: { roles: ['student', 'parent'] } },

  { path: '/quiz', name: 'quiz', component: () => import('@/views/quiz/QuizView.vue'), meta: { roles: ['student'] } },

  { path: '/quiz/history', name: 'quiz-history', component: () => import('@/views/quiz/QuizHistoryView.vue'), meta: { roles: ['student'] } },

  { path: '/quiz/submit', name: 'quiz-submit', component: () => import('@/views/quiz/QuizSubmitView.vue'), meta: { roles: ['student'] } },

  { path: '/quiz/completed', name: 'quiz-completed', component: () => import('@/views/quiz/QuizCompletedView.vue'), meta: { roles: ['student'] } },

  { path: '/quiz/review', name: 'quiz-review', component: () => import('@/views/quiz/QuizReviewView.vue'), meta: { roles: ['student'] } },

  { path: '/quiz/result/:type', name: 'quiz-result', component: () => import('@/views/quiz/QuizResultView.vue'), meta: { roles: ['student'] } },

  { path: '/assign', name: 'assign', component: () => import('@/views/teacher/AssignView.vue'), meta: { roles: ['teacher'] } },

  { path: '/review', redirect: '/audits' },

  { path: '/board', redirect: (to) => {
    if (to.query.taskId) return { path: `/tasks/${to.query.taskId}/summary`, replace: true }
    return { path: '/tasks', replace: true }
  }},

  { path: '/audits', name: 'audits', component: () => import('@/views/audits/AuditsHubView.vue'), meta: { roles: ['teacher', 'researcher', 'admin'] } },

  { path: '/content-audits', name: 'content-audits', component: () => import('@/views/researcher/ContentAuditsView.vue'), meta: { roles: ['researcher', 'admin'] } },

  { path: '/content-audits/:expId', name: 'content-audit-detail', component: () => import('@/views/researcher/ContentAuditDetailView.vue'), meta: { roles: ['researcher', 'admin'] } },

  { path: '/parent-binds', name: 'parent-binds', component: () => import('@/views/teacher/ParentBindAuditView.vue'), meta: { roles: ['teacher'] } },

  { path: '/admin', name: 'admin-hub', component: () => import('@/views/admin/AdminHubView.vue'), meta: { roles: ['admin'] } },

  { path: '/admin/badges', name: 'admin-badges', component: () => import('@/views/admin/AdminBadgeRulesView.vue'), meta: { roles: ['admin'] } },

  { path: '/admin/quiz-config', name: 'admin-quiz-config', component: () => import('@/views/admin/AdminQuizConfigView.vue'), meta: { roles: ['admin'] } },

  { path: '/admin/parent-registrations', name: 'admin-parent-registrations', component: () => import('@/views/admin/AdminParentRegistrationsView.vue'), meta: { roles: ['admin'] } },

  { path: '/admin/work-reviews', name: 'admin-work-reviews', component: () => import('@/views/admin/AdminWorkReviewsView.vue'), meta: { roles: ['admin'] } },

  { path: '/profile', name: 'profile', component: () => import('@/views/profile/ProfileView.vue') },

  { path: '/settings', name: 'settings', component: () => import('@/views/settings/SettingsView.vue') },

  { path: '/settings/dingtalk/callback', name: 'dingtalk-callback', component: () => import('@/views/settings/DingTalkCallbackView.vue') }

]



const router = createRouter({

  history: createWebHashHistory(import.meta.env.BASE_URL),

  routes,

  linkExactActiveClass: 'active'

})



function homeForRole() {

  // 首页对所有角色都是内容信息流（按角色范围个性化），统一落到 /home。
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

