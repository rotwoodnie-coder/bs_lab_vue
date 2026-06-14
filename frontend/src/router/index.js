import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/DashboardView.vue')
      },
      {
        path: 'system/user',
        name: 'user-management',
        component: () => import('../views/system/UserManagementView.vue')
      },
      {
        path: 'system/role',
        name: 'role-management',
        component: () => import('../views/system/RoleManagementView.vue')
      },
      {
        path: 'system/menu',
        name: 'menu-management',
        component: () => import('../views/system/MenuManagementView.vue')
      },
      {
        path: 'system/org',
        name: 'org-management',
        component: () => import('../views/system/OrgManagementView.vue')
      },
      {
        path: 'system/dictionary',
        name: 'dictionary-management',
        component: () => import('../views/system/SystemDictionaryView.vue')
      },
      {
        path: 'system/data-file-type',
        name: 'data-file-type-management',
        component: () => import('../views/system/DataFileTypeView.vue')
      },
      {
        path: 'system/log',
        name: 'log-management',
        component: () => import('../views/system/SysLogView.vue')
      },
      {
        path: 'data/data-dict',
        name: 'data-dict-management',
        component: () => import('../views/data/DataDictView.vue')
      },
      {
        path: 'data/textbook-edition',
        name: 'textbook-edition-management',
        component: () => import('../views/data/TextbookEditionView.vue')
      },
      {
        path: 'data/data-file',
        name: 'data-file-management',
        component: () => import('../views/data/DataFileView.vue')
      },
      {
        path: 'data/public-data-file',
        name: 'public-data-file-management',
        component: () => import('../views/data/PublicDataFileView.vue')
      },
      {
        path: 'data/public-data-file-view',
        name: 'public-data-file-view',
        component: () => import('../views/data/PublicDataFileViewOnly.vue')
      },
      {
        path: 'data/material-msg',
        name: 'material-msg-management',
        component: () => import('../views/data/MaterialMsgView.vue')
      },
      {
        path: 'data/public-material-msg',
        name: 'public-material-msg-management',
        component: () => import('../views/data/PublicMaterialMsgView.vue')
      },
      {
        path: 'data/public-material-msg-view',
        name: 'public-material-msg-view',
        component: () => import('../views/data/PublicMaterialMsgViewOnly.vue')
      },
      {
        path: 'edu/coursebook',
        name: 'coursebook-management',
        component: () => import('../views/edu/CoursebookView.vue')
      },
      {
        path: 'edu/teacher-subject',
        name: 'teacher-subject-management',
        component: () => import('../views/edu/TeacherSubjectView.vue')
      },
      {
        path: 'edu/subject-group',
        name: 'subject-group-management',
        component: () => import('../views/edu/SubjectGroupView.vue')
      },
      {
        path: 'edu/school-notice',
        name: 'school-notice-management',
        component: () => import('../views/edu/SchoolNoticeView.vue')
      },
      {
        path: 'system/messages',
        name: 'message-management',
        component: () => import('../views/system/SysMsgView.vue')
      },
      {
        path: 'exp/exp-standard',
        name: 'exp-standard-management',
        component: () => import('../views/exp/ExpStandardView.vue')
      },
      {
        path: 'exp/exp-standard/create',
        name: 'exp-standard-create',
        component: () => import('../views/exp/ExpStandardCreateView.vue')
      },
      {
        path: 'exp/exp-standard/view',
        name: 'exp-standard-view',
        component: () => import('../views/exp/ExpStandardDetailView.vue')
      },
      {
        path: 'exp/exp-standard-audit',
        name: 'exp-standard-audit-management',
        component: () => import('../views/exp/AuditExpStandard.vue')
      },
      {
        path: 'exp/exp-standard-public',
        name: 'exp-standard-query-management',
        component: () => import('../views/exp/QueryExpStandard.vue')
      },
      {
        path: 'exp/exp-teach',
        name: 'exp-teach-management',
        component: () => import('../views/exp/TeachExpView.vue')
      },
      {
        path: 'exp/exp-teach/create',
        name: 'exp-teach-create',
        component: () => import('../views/exp/TeachExpCreateView.vue')
      },
      {
        path: 'exp/exp-teach/view',
        name: 'exp-teach-view',
        component: () => import('../views/exp/TeachExpDetailView.vue')
      },
      {
        path: 'exp/exp-teach/course',
        name: 'exp-teach-view-course',
        component: () => import('../views/exp/TeachExpViewCourse.vue')
      },
      {
        path: 'exp/exp-teach-audit',
        name: 'exp-teach-audit-management',
        component: () => import('../views/exp/AuditExpTeach.vue')
      },
      {
        path: 'exp/exp-teach-public',
        name: 'exp-teach-query-management',
        component: () => import('../views/exp/QueryExpTeach.vue')
      },
      {
        path: 'exp/exp-student-audit',
        name: 'exp-student-audit-management',
        component: () => import('../views/exp/AuditExpStudent.vue')
      },
      {
        path: 'exp/exp-student-public',
        name: 'exp-student-query-management',
        component: () => import('../views/exp/QueryExpStudent.vue')
      },
      {
        path: 'exp/exp-question',
        name: 'exp-question-management',
        component: () => import('../views/exp/ExpQuestionView.vue')
      },
      {
        path: 'exp/exp-simulator',
        name: 'exp-simulator-management',
        component: () => import('../views/exp/ExpSimulatorView.vue')
      },
      {
        path: 'homework/exp-homework',
        name: 'exp-homework-management',
        component: () => import('../views/homework/ExpHomeworkView.vue')
      },
      {
        path: 'homework/exp-homework-student',
        name: 'exp-homework-student-management',
        component: () => import('../views/homework/ExpHomeworkStudentView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/admin/dashboard'
  }
  return true
})

export default router
