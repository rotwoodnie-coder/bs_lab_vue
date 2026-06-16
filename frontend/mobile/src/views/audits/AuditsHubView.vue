<template>
  <div class="prototype-container pad-shell safe-top" data-layout="list-workbench">
    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton fallback="/profile" />
        <h1 class="pad-workbench__title flex-1">待办中心</h1>
        <span v-if="summary.total > 0" class="badge badge-warning">{{ summary.total }}</span>
      </header>

      <div class="pad-workbench__body">
        <div class="pad-workbench__main">
          <div class="pad-workbench__mobile-head px-4 safe-top stack-4">
            <header class="topbar page-topbar">
              <PageBackButton fallback="/profile" />
              <h1 class="topbar-title text-xl flex-1 min-w-0">待办中心</h1>
              <span v-if="summary.total > 0" class="badge badge-warning shrink-0">{{ summary.total }}</span>
            </header>
          </div>

          <div class="pb-28 stack-4">
            <div v-if="loading" class="py-12 text-center muted-2">加载中…</div>
            <template v-else>
              <router-link
                v-if="showWorkReviews"
                :to="workReviewLink"
                class="card card-link card-pad row items-center justify-between"
              >
                <div>
                  <div class="text-sm font-bold">作品待批阅</div>
                  <div class="text-xs muted mt-1">在班级任务中审核学生提交</div>
                </div>
                <span v-if="summary.pendingWorkReviews > 0" class="badge badge-warning">{{ summary.pendingWorkReviews }}</span>
              </router-link>

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

              <router-link
                v-if="showExpAudits"
                to="/content-audits"
                class="card card-link card-pad row items-center justify-between"
              >
                <div>
                  <div class="text-sm font-bold">实验待审核</div>
                  <div class="text-xs muted mt-1">审核老师提交的教学/标准实验</div>
                </div>
                <span v-if="summary.pendingExpAudits > 0" class="badge badge-warning">{{ summary.pendingExpAudits }}</span>
              </router-link>

              <p v-if="summary.total === 0" class="text-center py-12 muted-2 text-sm">暂无待办事项 🎉</p>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchAuditSummary } from '@/api/audits'
import { fetchTeacherDashboard, fetchTeacherTasks } from '@/api/teacher'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { isTeacherRole, canAuditExperiments } from '@/utils/role'

const userStore = useUserStore()
const appStore = useAppStore()
appStore.setActiveTab(canAuditExperiments(userStore.userInfo?.userRoleId) && !isTeacherRole(userStore.userInfo?.userRoleId) ? 'tasks' : 'profile')
const loading = ref(true)
const summary = ref({
  pendingWorkReviews: 0,
  pendingParentBinds: 0,
  pendingExpAudits: 0,
  total: 0
})
const teacherDash = ref(null)
const teacherTasks = ref([])

const role = computed(() => userStore.userInfo?.userRoleId)
const showWorkReviews = computed(() => isTeacherRole(role.value))
const showParentBinds = computed(() => isTeacherRole(role.value))
const showExpAudits = computed(() => canAuditExperiments(role.value))

const workReviewLink = computed(() => {
  if (!showWorkReviews.value) return '/tasks'
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
    if (showWorkReviews.value) {
      requests.push(fetchTeacherDashboard(), fetchTeacherTasks())
    }
    const results = await Promise.all(requests)
    const auditRes = results[0]
    if (auditRes?.code === 200 && auditRes.data) {
      summary.value = {
        ...auditRes.data,
        total: auditRes.data.total ?? (
          (auditRes.data.pendingWorkReviews || 0) +
          (auditRes.data.pendingParentBinds || 0) +
          (auditRes.data.pendingExpAudits || 0)
        )
      }
    }
    if (showWorkReviews.value) {
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
