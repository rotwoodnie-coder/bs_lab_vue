<template>
  <MobilePageShell class="safe-top" data-layout="list-workbench">
    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton :fallback="hubFallback" />
        <h1 class="pad-workbench__title flex-1">{{ hubTitle }}</h1>
        <span v-if="summary.total > 0" class="badge badge-warning">{{ summary.total }}</span>
      </header>

      <div class="pad-workbench__body">
        <div class="pad-workbench__main">
          <div class="pad-workbench__mobile-head px-4 safe-top stack-4">
            <header class="topbar page-topbar">
              <PageBackButton :fallback="hubFallback" />
              <h1 class="topbar-title text-xl flex-1 min-w-0">{{ hubTitle }}</h1>
              <span v-if="summary.total > 0" class="badge badge-warning shrink-0">{{ summary.total }}</span>
            </header>
          </div>

          <div class="px-4 pb-28 stack-4">
            <div v-if="loading" class="py-12 text-center muted-2">加载中…</div>
            <template v-else>
              <!-- 教师：作品评价 -->
              <router-link
                v-if="showTeacherWorkReviews"
                :to="workReviewLink"
                class="card card-link card-pad row items-center justify-between"
              >
                <div>
                  <div class="text-sm font-bold">作品待评价</div>
                  <div class="text-xs muted mt-1">在班级任务中审核学生提交</div>
                </div>
                <span v-if="summary.pendingWorkReviews > 0" class="badge badge-warning">{{ summary.pendingWorkReviews }}</span>
              </router-link>

              <!-- 教师：家长绑定 -->
              <router-link
                v-if="showParentBinds"
                to="/parent-binds"
                class="card card-link card-pad row items-center justify-between"
              >
                <div>
                  <div class="text-sm font-bold">家长绑定审核</div>
                  <div class="text-xs muted mt-1">审核本班家长绑定申请</div>
                </div>
                <span v-if="summary.pendingParentBinds > 0" class="badge badge-warning">{{ summary.pendingParentBinds }}</span>
              </router-link>

              <!-- 教研员/管理员：实验审核 -->
              <router-link
                v-if="showExpAudits"
                to="/content-audits"
                class="card card-link card-pad row items-center justify-between"
              >
                <div>
                  <div class="text-sm font-bold">实验待审核</div>
                  <div class="text-xs muted mt-1">{{ expAuditHint }}</div>
                </div>
                <span v-if="summary.pendingExpAudits > 0" class="badge badge-warning">{{ summary.pendingExpAudits }}</span>
              </router-link>

              <!-- 管理员：学生作品审核 -->
              <router-link
                v-if="showAdminWorkReviews"
                to="/admin/work-reviews"
                class="card card-link card-pad row items-center justify-between"
              >
                <div>
                  <div class="text-sm font-bold">学生作品待审核</div>
                  <div class="text-xs muted mt-1">{{ adminWorkHint }}</div>
                </div>
                <span v-if="summary.pendingWorkReviews > 0" class="badge badge-warning">{{ summary.pendingWorkReviews }}</span>
              </router-link>

              <!-- 管理员：家长注册审核 -->
              <router-link
                v-if="showAdminParentRegs"
                to="/admin/parent-registrations"
                class="card card-link card-pad row items-center justify-between"
              >
                <div>
                  <div class="text-sm font-bold">家长注册审核</div>
                  <div class="text-xs muted mt-1">审核家长账号注册申请</div>
                </div>
                <span v-if="summary.pendingParentRegistrations > 0" class="badge badge-warning">{{ summary.pendingParentRegistrations }}</span>
              </router-link>

              <p v-if="summary.total === 0" class="text-center py-12 muted-2 text-sm">暂无待办事项 🎉</p>
            </template>
          </div>
        </div>
      </div>
    </div>
  </MobilePageShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchAuditSummary } from '@/api/audits'
import { fetchTeacherDashboard, fetchTeacherTasks } from '@/api/teacher'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { isTeacherRole, canAuditExperiments, isAdminRole, isSysAdminRole } from '@/utils/role'

const userStore = useUserStore()
const appStore = useAppStore()
appStore.setActiveTab(isAdminRole(userStore.userInfo?.userRoleId) ? 'admin' : 'profile')

const loading = ref(true)
const summary = ref({
  pendingWorkReviews: 0,
  pendingParentBinds: 0,
  pendingExpAudits: 0,
  pendingParentRegistrations: 0,
  total: 0
})
const teacherDash = ref(null)
const teacherTasks = ref([])

const role = computed(() => userStore.userInfo?.userRoleId)
const isAdmin = computed(() => isAdminRole(role.value))
const isSysAdmin = computed(() => isSysAdminRole(role.value))

const hubTitle = computed(() => (isAdmin.value ? '审核中心' : '待办中心'))
const hubFallback = computed(() => (isAdmin.value ? '/admin' : '/profile'))

const showTeacherWorkReviews = computed(() => isTeacherRole(role.value))
const showParentBinds = computed(() => isTeacherRole(role.value))
const showExpAudits = computed(() => canAuditExperiments(role.value))
const showAdminWorkReviews = computed(() => isAdmin.value)
const showAdminParentRegs = computed(() => isAdmin.value)

const expAuditHint = computed(() => {
  if (isAdmin.value && !isSysAdmin.value) return '审核本校老师提交的教学/标准实验'
  if (isAdmin.value) return '审核老师提交的教学/标准实验（全局）'
  return '审核老师提交的教学/标准实验'
})

const adminWorkHint = computed(() => {
  if (isSysAdmin.value) return '审核学生提交的创意/拍同款作品（全局）'
  return '审核本校学生提交的创意/拍同款作品'
})

const workReviewLink = computed(() => {
  if (!showTeacherWorkReviews.value) return '/tasks'
  if ((summary.value.pendingWorkReviews || 0) <= 0) return '/tasks?status=pending'
  const pendingTask = teacherTasks.value.find((t) => (t.pendingReview || 0) > 0)
  if (pendingTask?.taskId) {
    return `/tasks/${pendingTask.taskId}/summary?tab=pending`
  }
  const dash = teacherDash.value
  if (dash?.latestTaskId) {
    return `/tasks/${dash.latestTaskId}/summary?tab=pending`
  }
  return '/tasks?status=pending'
})

async function load() {
  loading.value = true
  try {
    const requests = [fetchAuditSummary()]
    if (showTeacherWorkReviews.value) {
      requests.push(fetchTeacherDashboard(), fetchTeacherTasks())
    }
    const results = await Promise.all(requests)
    const auditRes = results[0]
    if (auditRes?.code === 200 && auditRes.data) {
      const d = auditRes.data
      summary.value = {
        pendingWorkReviews: d.pendingWorkReviews || 0,
        pendingParentBinds: d.pendingParentBinds || 0,
        pendingExpAudits: d.pendingExpAudits || 0,
        pendingParentRegistrations: d.pendingParentRegistrations || 0,
        total: d.total ?? (
          (d.pendingWorkReviews || 0) +
          (d.pendingParentBinds || 0) +
          (d.pendingExpAudits || 0) +
          (d.pendingParentRegistrations || 0)
        )
      }
    }
    if (showTeacherWorkReviews.value) {
      const dashRes = results[1]
      const taskRes = results[2]
      if (dashRes?.code === 200) teacherDash.value = dashRes.data
      teacherTasks.value = taskRes?.code === 200 && Array.isArray(taskRes.data) ? taskRes.data : []
    }
  } catch (e) {
    console.warn('加载待办汇总失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
