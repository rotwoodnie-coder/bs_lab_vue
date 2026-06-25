<template>
  <MobilePageShell class="theme-teacher safe-top" data-layout="teacher-home">
    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton fallback="/home" />
        <h1 class="pad-workbench__title">📊 班级提交看板</h1>
      </header>

      <div class="pad-teacher__mobile-only pb-28">
        <div class="topbar safe-top">
          <PageBackButton fallback="/home" />
          <h1 class="topbar-title flex-1">📊 班级提交看板</h1>
        </div>

        <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
        <div v-else-if="error" class="px-4 py-12 text-center stack-3">
          <p class="muted-2">{{ error }}</p>
          <router-link to="/assign" class="btn btn-primary btn-sm">去发布任务</router-link>
        </div>

        <template v-else>
          <div class="px-4 py-4 stack-3">
            <select v-model="selectedTaskId" class="input w-full" @change="onTaskChange">
              <option v-if="!taskOptions.length" value="">暂无任务</option>
              <option v-for="t in taskOptions" :key="t.taskId" :value="t.taskId">
                {{ t.title }}{{ t.className ? ` · ${t.className}` : '' }}
              </option>
            </select>
          </div>

          <div class="px-4 col items-center py-4 surface-2">
            <div class="ring-chart" :style="ringStyle">
              <div class="ring-chart-inner">
                <div class="text-2xl font-bold text-brand">{{ board.submitRate }}%</div>
                <div class="text-xs muted">完成率</div>
              </div>
            </div>
            <div class="grid grid-3 gap-3 w-full mt-5">
              <div class="card card-pad text-center">
                <div class="text-xl font-bold text-brand">{{ totalStudents }}</div>
                <div class="text-xs muted">总人数</div>
              </div>
              <div class="card card-pad text-center">
                <div class="text-xl font-bold text-success">{{ board.submitted }}</div>
                <div class="text-xs muted">已提交</div>
              </div>
              <div class="card card-pad text-center">
                <div class="text-xl font-bold text-danger">{{ board.unsubmitted }}</div>
                <div class="text-xs muted">未提交</div>
              </div>
            </div>
          </div>

          <div class="px-4 mt-4">
            <div class="row items-center justify-between mb-3">
              <h2 class="text-sm font-bold text-danger">⏳ 未提交（{{ unsubmittedList.length }}人）</h2>
              <button
                v-if="unsubmittedList.length"
                type="button"
                class="btn btn-sm btn-outline"
                :disabled="reminding"
                @click="remindAll"
              >
                <i data-lucide="megaphone" class="icon icon-sm"></i>
                {{ reminding ? '发送中…' : '全部提醒' }}
              </button>
            </div>
            <div v-if="!unsubmittedList.length" class="text-xs muted text-center py-4">全部已提交 🎉</div>
            <div v-else class="stack">
              <div
                v-for="s in visibleUnsubmitted"
                :key="s.userId || s.name"
                class="card card-pad row items-center justify-between"
              >
                <div class="row items-center gap-2">
                  <UserAvatar size="sm" :name="s.name" :src="s.avatarUrl" role="student" />
                  <span class="text-sm">{{ s.name }}</span>
                </div>
                <button type="button" class="btn btn-sm btn-outline" @click="remindAll">
                  <i data-lucide="megaphone" class="icon icon-sm"></i> 提醒
                </button>
              </div>
              <details v-if="unsubmittedList.length > visibleUnsubmitted.length" class="text-xs muted">
                <summary class="cursor-pointer px-3 py-2 text-center">
                  展开其余 {{ unsubmittedList.length - visibleUnsubmitted.length }} 人
                </summary>
                <div class="stack mt-2">
                  <div
                    v-for="s in unsubmittedList.slice(visibleUnsubmitted.length)"
                    :key="'more-' + (s.userId || s.name)"
                    class="card card-pad row items-center justify-between"
                  >
                    <div class="row items-center gap-2">
                      <UserAvatar size="sm" :name="s.name" :src="s.avatarUrl" role="student" />
                      <span class="text-sm">{{ s.name }}</span>
                    </div>
                    <button type="button" class="btn btn-sm btn-outline" @click="remindAll">
                      <i data-lucide="megaphone" class="icon icon-sm"></i> 提醒
                    </button>
                  </div>
                </div>
              </details>
            </div>
          </div>

          <div class="px-4 mt-4 mb-6">
            <div class="row items-center justify-between mb-3">
              <h2 class="text-sm font-bold text-success">✅ 已提交（{{ submittedList.length }}人）</h2>
            </div>
            <div v-if="!submittedList.length" class="text-xs muted text-center py-4">暂无提交</div>
            <div v-else class="stack">
              <div
                v-for="s in visibleSubmitted"
                :key="s.userId || s.name"
                class="card card-pad row items-center justify-between"
              >
                <div class="row items-center gap-2">
                  <UserAvatar
                    size="sm"
                    :name="s.name"
                    :src="s.avatarUrl"
                    grad-class="avatar-grad-violet-indigo"
                    role="student"
                  />
                  <span class="text-sm">{{ s.name }}</span>
                </div>
                <router-link :to="reviewLink(s)" class="btn btn-sm btn-outline text-brand">
                  <i data-lucide="clipboard-list" class="icon icon-sm"></i> 评价
                </router-link>
              </div>
              <details v-if="submittedList.length > visibleSubmitted.length" class="text-xs muted">
                <summary class="cursor-pointer px-3 py-2 text-center">
                  展开其余 {{ submittedList.length - visibleSubmitted.length }} 人
                </summary>
                <div class="stack mt-2">
                  <div
                    v-for="s in submittedList.slice(visibleSubmitted.length)"
                    :key="'sub-' + (s.userId || s.name)"
                    class="card card-pad row items-center justify-between"
                  >
                    <div class="row items-center gap-2">
                      <UserAvatar
                    size="sm"
                    :name="s.name"
                    :src="s.avatarUrl"
                    grad-class="avatar-grad-violet-indigo"
                    role="student"
                  />
                      <span class="text-sm">{{ s.name }}</span>
                    </div>
                    <router-link :to="reviewLink(s)" class="btn btn-sm btn-outline text-brand">
                      <i data-lucide="clipboard-list" class="icon icon-sm"></i> 评价
                    </router-link>
                  </div>
                </div>
              </details>
            </div>
          </div>
        </template>
      </div>
    </div>

  </MobilePageShell>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { fetchTeacherTaskBoard, fetchTeacherTasks, remindTeacherTask } from '@/api/teacher'
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { useLucideIcons } from '@/composables/useLucideIcons'

const route = useRoute()
const appStore = useAppStore()
appStore.setActiveTab('tasks')

const loading = ref(true)
const error = ref('')
const reminding = ref(false)
const taskOptions = ref([])
const selectedTaskId = ref('')
const board = ref({
  taskTitle: '',
  className: '',
  submitted: 0,
  unsubmitted: 0,
  submitRate: 0,
  students: []
})

const PREVIEW_COUNT = 4

const submittedList = computed(() => (board.value.students || []).filter((s) => s.done))
const unsubmittedList = computed(() => (board.value.students || []).filter((s) => !s.done))
const visibleSubmitted = computed(() => submittedList.value.slice(0, PREVIEW_COUNT))
const visibleUnsubmitted = computed(() => unsubmittedList.value.slice(0, PREVIEW_COUNT))
const totalStudents = computed(() => board.value.submitted + board.value.unsubmitted)

const ringStyle = computed(() => {
  const rate = board.value.submitRate || 0
  return {
    background: `conic-gradient(var(--brand) 0% ${rate}%, var(--color-border) ${rate}% 100%)`
  }
})

function reviewLink(student) {
  if (student.workId) {
    return { path: '/review', query: { workId: student.workId } }
  }
  return '/review'
}

async function loadTaskOptions() {
  try {
    const res = await fetchTeacherTasks()
    if (res?.code === 200 && Array.isArray(res.data)) {
      taskOptions.value = res.data
      if (!selectedTaskId.value && taskOptions.value.length) {
        selectedTaskId.value = taskOptions.value[0].taskId
      }
    }
  } catch {
    taskOptions.value = []
  }
}

async function onTaskChange() {
  loading.value = true
  await loadBoard()
  loading.value = false
  initIcons()
}

async function loadBoard() {
  const taskId = selectedTaskId.value || route.query.taskId
  if (!taskId) {
    error.value = '请先发布任务'
    loading.value = false
    return
  }
  selectedTaskId.value = String(taskId)
  error.value = ''
  try {
    const res = await fetchTeacherTaskBoard(taskId)
    if (res?.code === 200 && res.data) {
      board.value = res.data
    } else {
      error.value = res?.message || '加载看板失败'
    }
  } catch {
    error.value = '加载看板失败，请稍后重试'
  }
}

async function remindAll() {
  if (!selectedTaskId.value || reminding.value) return
  reminding.value = true
  try {
    const res = await remindTeacherTask(selectedTaskId.value)
    if (res?.code === 200) {
      const msg = res.data?.message || `已向 ${res.data?.notifiedCount ?? board.value.unsubmitted} 名未提交学生发送提醒`
      alert(msg)
    } else {
      alert(res?.message || '提醒发送失败')
    }
  } catch {
    alert('提醒发送失败，请稍后重试')
  } finally {
    reminding.value = false
  }
}

const { initIcons } = useLucideIcons()

onMounted(async () => {
  if (route.query.taskId) {
    selectedTaskId.value = String(route.query.taskId)
  }
  await loadTaskOptions()
  if (!selectedTaskId.value && taskOptions.value.length) {
    selectedTaskId.value = taskOptions.value[0].taskId
  }
  if (selectedTaskId.value) {
    await loadBoard()
  } else if (!taskOptions.value.length) {
    error.value = '请从教师工作台进入，或先发布任务'
  }
  loading.value = false
  initIcons()
})
</script>
