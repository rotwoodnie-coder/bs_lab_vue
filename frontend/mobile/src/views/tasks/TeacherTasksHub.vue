<template>
  <div class="teacher-tasks-hub">
    <!-- ── Pad 横屏：概览 + 侧栏操作 + 任务列表 ── -->
    <div v-if="!loading" class="pad-teacher__overview">
      <router-link
        v-for="stat in overviewStats"
        :key="stat.key"
        :to="stat.to"
        class="card rounded-xl card-pad text-center teacher-stat-card card-link"
      >
        <div class="mb-1">
          <i :data-lucide="stat.icon" class="icon icon-lg" :style="{ color: stat.color }"></i>
        </div>
        <div class="stat-num" :class="stat.numClass">{{ stat.value }}</div>
        <div class="text-xs muted mt-1">{{ stat.label }}</div>
      </router-link>
    </div>

    <div v-if="!loading" class="pad-teacher__body">
      <nav class="pad-teacher__actions stack-2">
        <router-link
          v-for="action in quickActions"
          :key="action.key"
          :to="action.to"
          class="card card-link card-pad row items-center gap-3 teacher-action-card"
        >
          <span class="teacher-action-card__icon" :style="{ background: action.bg }">
            <i :data-lucide="action.icon" class="icon"></i>
          </span>
          <span class="text-sm font-bold flex-1">{{ action.label }}</span>
          <span v-if="action.badge > 0" class="badge badge-warning">{{ action.badge }}</span>
        </router-link>
        <router-link to="/assign" class="btn btn-primary btn-block">
          <i data-lucide="pen-square" class="icon"></i>
          发布新任务
        </router-link>
      </nav>

      <section class="pad-teacher__feed">
        <div class="row items-center justify-between mb-3 px-1">
          <h2 class="text-sm font-bold">班级实验任务</h2>
          <span class="text-xs muted">共 {{ tasks.length }} 项</span>
        </div>
        <div v-if="!tasks.length" class="teacher-empty card card-pad text-center">
          <div class="text-2xl mb-2">📋</div>
          <p class="text-sm font-bold">还没有发布任务</p>
          <p class="text-xs muted mt-2">点击下方按钮创建第一个班级实验任务</p>
          <router-link to="/assign" class="btn btn-primary btn-sm mt-4">去发布</router-link>
        </div>
        <div v-else class="pad-home__grid">
          <router-link
            v-for="task in tasks"
            :key="task.taskId"
            :to="`/tasks/${task.taskId}/summary`"
            class="card card-link card-pad teacher-task-card"
          >
            <TeacherTaskCardBody :task="task" />
          </router-link>
        </div>
      </section>
    </div>

    <!-- ── 手机竖屏 ── -->
    <div class="pad-teacher__mobile-only pb-28">
      <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>

      <template v-else>
        <div class="teacher-hub-banner mx-4 mt-3">
          <div class="row items-center justify-between gap-3">
            <div class="min-w-0">
              <p class="text-xs teacher-hub-banner__eyebrow">教师工作台</p>
              <h2 class="text-lg font-bold truncate">{{ dashboard?.teacherName || '老师' }}，你好</h2>
              <p v-if="dashboard?.classLabel" class="text-xs teacher-hub-banner__sub mt-1">{{ dashboard.classLabel }}</p>
            </div>
            <router-link to="/assign" class="btn btn-sm teacher-hub-banner__cta">
              <i data-lucide="pen-square" class="icon icon-sm"></i>
              发布
            </router-link>
          </div>
        </div>

        <div class="card card-pad mx-4 mt-4 teacher-hub-progress">
          <div class="row items-center justify-between mb-3">
            <span class="text-sm font-bold">班级整体进度</span>
            <span class="text-xs muted">{{ dashboard?.students || 0 }} 名学生</span>
          </div>
          <div class="row items-center gap-4">
            <div class="teacher-ring" :style="ringStyle">
              <div class="teacher-ring__inner">
                <span class="text-lg font-bold text-brand">{{ dashboard?.submitRate || 0 }}%</span>
              </div>
            </div>
            <div class="grid-2 gap-2 flex-1">
              <div class="stat surface-2 rounded-lg p-3">
                <div class="stat-num text-brand">{{ dashboard?.pendingReview || 0 }}</div>
                <div class="stat-label">待评价</div>
              </div>
              <div class="stat surface-2 rounded-lg p-3">
                <div class="stat-num text-warning">{{ dashboard?.pendingParentBinds || 0 }}</div>
                <div class="stat-label">绑定待审</div>
              </div>
            </div>
          </div>
          <div class="progress mt-3">
            <div class="progress-fill progress-fill-success" :style="{ width: (dashboard?.submitRate || 0) + '%' }"></div>
          </div>
          <div v-if="weeklyTrendBars.length" class="teacher-weekly-trend mt-4">
            <div class="row items-center justify-between mb-2">
              <span class="text-xs font-bold">本周提交</span>
              <span v-if="dashboard?.trendDeltaPercent != null" class="text-xs muted">
                较上周 {{ dashboard.trendDeltaPercent >= 0 ? '+' : '' }}{{ dashboard.trendDeltaPercent }}%
              </span>
            </div>
            <div class="teacher-weekly-trend__bars row items-end gap-1">
              <div
                v-for="(bar, idx) in weeklyTrendBars"
                :key="'w-' + idx"
                class="teacher-weekly-trend__bar"
                :style="{ height: bar.height + 'px' }"
                :title="`${bar.label}：${bar.value} 份`"
              />
            </div>
            <div class="teacher-weekly-trend__labels row justify-between mt-1">
              <span v-for="(bar, idx) in weeklyTrendBars" :key="'l-' + idx" class="text-xs muted">{{ bar.label }}</span>
            </div>
          </div>
        </div>

        <div class="px-4 mt-4">
          <div class="grid-2 gap-3 teacher-hub-actions">
            <router-link
              v-for="action in quickActions"
              :key="'m-' + action.key"
              :to="action.to"
              class="card card-link card-pad text-center teacher-quick-action relative"
            >
              <div class="mb-1">
                <i :data-lucide="action.icon" class="icon icon-lg" :style="{ color: action.color }"></i>
              </div>
              <span class="text-xs font-bold">{{ action.label }}</span>
              <span v-if="action.badge > 0" class="teacher-quick-action__badge">{{ action.badge }}</span>
            </router-link>
          </div>
        </div>

        <div class="px-4 mt-5">
          <div class="row items-center justify-between mb-3">
            <h2 class="text-sm font-bold">班级实验任务</h2>
            <span class="text-xs muted">{{ tasks.length }} 项</span>
          </div>

          <div v-if="!tasks.length" class="teacher-empty card card-pad text-center py-8">
            <div class="text-2xl mb-2">📋</div>
            <p class="text-sm muted-2">暂无发布的任务</p>
            <router-link to="/assign" class="btn btn-primary btn-sm mt-4">发布新任务</router-link>
          </div>

          <div v-else class="stack-3">
            <router-link
              v-for="task in tasks"
              :key="'m-task-' + task.taskId"
              :to="`/tasks/${task.taskId}/summary`"
              class="card card-link card-pad teacher-task-card"
            >
              <TeacherTaskCardBody :task="task" />
            </router-link>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, defineComponent, h } from 'vue'
import { fetchTeacherDashboard, fetchTeacherTasks } from '@/api/teacher'
import { fetchAuditSummary } from '@/api/audits'
import { useLucideIcons } from '@/composables/useLucideIcons'

const TeacherTaskCardBody = defineComponent({
  name: 'TeacherTaskCardBody',
  props: {
    task: { type: Object, required: true }
  },
  setup(props) {
    const rate = computed(() => Math.min(100, Math.max(0, props.task.submitRate || 0)))
    const statusBadge = computed(() => {
      const task = props.task
      const pending = task.pendingReview || 0
      const submitted = task.submitted || 0
      const total = task.totalStudents || 0
      if (task.cancelled) {
        return { label: '已取消', className: 'badge-slate' }
      }
      if (pending > 0) {
        return { label: `待审 ${pending}`, className: 'badge-warning' }
      }
      if (submitted < total) {
        return { label: '待跟进', className: 'badge-info' }
      }
      return { label: '已完成', className: 'badge-success' }
    })
    return () => h('div', { class: 'teacher-task-card__body stack-3' }, [
      h('div', { class: 'row items-start justify-between gap-2' }, [
        h('div', { class: 'min-w-0 flex-1' }, [
          h('span', { class: 'task-type-badge task-type-badge--homework' }, '📋 班级实验任务'),
          h('div', { class: 'text-base font-bold mt-2 truncate' }, props.task.title || '未命名任务'),
          props.task.className
            ? h('p', { class: 'text-xs muted mt-1 truncate' }, props.task.className)
            : null
        ]),
        h('div', { class: 'teacher-task-card__rate shrink-0 text-center' }, [
          h('div', { class: 'text-lg font-bold text-brand' }, `${rate.value}%`),
          h('div', { class: 'text-xs muted' }, '完成率')
        ])
      ]),
      h('div', { class: 'progress progress-sm' }, [
        h('div', {
          class: 'progress-fill progress-fill-success',
          style: { width: `${rate.value}%` }
        })
      ]),
      h('div', { class: 'row items-center justify-between gap-2 flex-wrap' }, [
        h('span', { class: 'text-xs muted' }, `已提交 ${props.task.submitted || 0}/${props.task.totalStudents || 0}`),
        h('span', { class: `badge text-xs ${statusBadge.value.className}` }, statusBadge.value.label)
      ])
    ])
  }
})

const loading = ref(true)
const dashboard = ref(null)
const tasks = ref([])
const auditSummary = ref(null)

const auditTotal = computed(() => auditSummary.value?.total || 0)

const ringStyle = computed(() => {
  const rate = dashboard.value?.submitRate || 0
  return {
    background: `conic-gradient(var(--brand) 0% ${rate}%, var(--color-border) ${rate}% 100%)`
  }
})

const latestTaskLink = computed(() => {
  const id = dashboard.value?.latestTaskId
  return id ? `/tasks/${id}/summary` : '/tasks'
})

const reviewTaskLink = computed(() => {
  const pendingTask = tasks.value.find((t) => (t.pendingReview || 0) > 0)
  if (pendingTask?.taskId) {
    return `/tasks/${pendingTask.taskId}/summary?tab=pending`
  }
  if ((dashboard.value?.pendingReview || 0) > 0 && dashboard.value?.latestTaskId) {
    return `/tasks/${dashboard.value.latestTaskId}/summary?tab=pending`
  }
  return '/tasks?status=pending'
})

const WEEKDAY_LABELS = ['一', '二', '三', '四', '五']

const weeklyTrendBars = computed(() => {
  const trend = dashboard.value?.weeklyTrend
  if (!Array.isArray(trend) || !trend.length) return []
  const max = Math.max(...trend, 1)
  return trend.map((value, idx) => ({
    label: WEEKDAY_LABELS[idx] || '',
    value: value || 0,
    height: Math.max(4, Math.round(((value || 0) / max) * 48))
  }))
})

const overviewStats = computed(() => [
  {
    key: 'review',
    icon: 'file-check',
    color: 'var(--c-blue-600)',
    numClass: 'text-brand',
    value: dashboard.value?.pendingReview || 0,
    label: '待评价',
    to: reviewTaskLink.value
  },
  {
    key: 'bind',
    icon: 'users',
    color: 'var(--c-amber-600)',
    numClass: 'text-warning',
    value: dashboard.value?.pendingParentBinds || 0,
    label: '绑定待审',
    to: '/parent-binds'
  },
  {
    key: 'assigned',
    icon: 'clipboard-list',
    color: 'var(--c-violet-600)',
    numClass: 'text-accent',
    value: dashboard.value?.assigned || tasks.value.length || 0,
    label: '已发布',
    to: '/tasks'
  },
  {
    key: 'rate',
    icon: 'trending-up',
    color: 'var(--c-emerald-600)',
    numClass: 'text-success',
    value: `${dashboard.value?.submitRate || 0}%`,
    label: '班级完成率',
    to: latestTaskLink.value
  }
])

const quickActions = computed(() => [
  {
    key: 'audits',
    icon: 'inbox',
    label: '待办中心',
    color: 'var(--c-blue-600)',
    bg: 'color-mix(in srgb, var(--brand) 12%, var(--color-surface))',
    to: '/audits',
    badge: auditTotal.value
  },
  {
    key: 'assign',
    icon: 'pen-square',
    label: '发布实验任务',
    color: 'var(--c-emerald-600)',
    bg: 'color-mix(in srgb, var(--brand) 12%, var(--color-surface))',
    to: '/assign',
    badge: 0
  },
  {
    key: 'binds',
    icon: 'user-check',
    label: '绑定审核',
    color: 'var(--c-amber-600)',
    bg: 'color-mix(in srgb, var(--color-warning) 12%, var(--color-surface))',
    to: '/parent-binds',
    badge: dashboard.value?.pendingParentBinds || 0
  },
  {
    key: 'board',
    icon: 'bar-chart-3',
    label: '任务看板',
    color: 'var(--c-violet-600)',
    bg: 'color-mix(in srgb, var(--color-accent) 12%, var(--color-surface))',
    to: latestTaskLink.value,
    badge: 0
  }
])

async function load() {
  loading.value = true
  try {
    const [dashRes, taskRes, auditRes] = await Promise.all([
      fetchTeacherDashboard(),
      fetchTeacherTasks(),
      fetchAuditSummary()
    ])
    dashboard.value = dashRes?.code === 200 ? dashRes.data : null
    tasks.value = taskRes?.code === 200 && Array.isArray(taskRes.data) ? taskRes.data : []
    auditSummary.value = auditRes?.code === 200 ? auditRes.data : null
  } catch (e) {
    console.warn('加载教师任务看板失败', e)
    dashboard.value = null
    tasks.value = []
  } finally {
    loading.value = false
    initIcons()
  }
}

const { initIcons } = useLucideIcons()

onMounted(load)
defineExpose({ reload: load })
</script>

<style scoped>
.teacher-hub-banner {
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--brand-gradient);
  color: #fff;
  box-shadow: 0 8px 24px color-mix(in srgb, var(--brand) 28%, transparent);
}

.teacher-hub-banner__eyebrow {
  opacity: 0.85;
  letter-spacing: 0.04em;
}

.teacher-hub-banner__sub {
  opacity: 0.9;
}

.teacher-hub-banner__cta {
  background: rgba(255, 255, 255, 0.95);
  color: var(--brand-strong);
  border: none;
  flex-shrink: 0;
}

.teacher-ring {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.teacher-ring__inner {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: var(--color-surface);
  display: flex;
  align-items: center;
  justify-content: center;
}

.teacher-quick-action {
  min-height: 88px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.teacher-quick-action__badge {
  position: absolute;
  top: 8px;
  right: 8px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--color-warning);
  color: #fff;
  font-size: var(--text-2xs);
  font-weight: 700;
  line-height: 18px;
  text-align: center;
}

.teacher-action-card__icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.teacher-task-card {
  border-left: 3px solid var(--brand);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.teacher-task-card:active {
  transform: scale(0.99);
}

.progress-sm {
  height: 6px;
}

.teacher-stat-card {
  transition: transform 0.15s ease;
}

.teacher-stat-card:hover {
  transform: translateY(-2px);
}

.teacher-empty {
  border: 1px dashed var(--color-border-soft);
  background: var(--color-surface-2);
}

.teacher-weekly-trend__bars {
  height: 52px;
  align-items: flex-end;
}

.teacher-weekly-trend__bar {
  flex: 1;
  min-width: 0;
  border-radius: 4px 4px 0 0;
  background: color-mix(in srgb, var(--brand) 65%, var(--color-surface));
}

.teacher-weekly-trend__labels span {
  flex: 1;
  text-align: center;
}

@media (min-width: 768px) {
  .teacher-tasks-hub {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
    overflow: hidden;
  }
}
</style>
