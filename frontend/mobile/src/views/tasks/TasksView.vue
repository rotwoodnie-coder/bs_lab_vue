<template>
  <div class="prototype-container pad-shell" data-layout="list-workbench" data-tasks-list>
    <BottomNav />

    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton />
        <h1 class="pad-workbench__title">{{ pageTitle }}</h1>
        <div class="pad-workbench__topbar-actions">
          <router-link to="/search" class="icon-btn" aria-label="搜索">
            <i data-lucide="search" class="icon"></i>
          </router-link>
        </div>
      </header>

      <div v-if="!isTeacherView" class="pad-workbench__filters">
        <div class="tabs" data-tasks-tabs>
          <button
            v-for="tab in visibleCategoryTabs"
            :key="tab.key"
            type="button"
            class="tab"
            :class="{ active: activeCategory === tab.key }"
            @click="switchCategory(tab.key)"
          >{{ tab.label }}</button>
        </div>
        <div class="tabs mt-2" data-tasks-status-tabs>
          <button
            v-for="tab in statusTabs"
            :key="tab.key"
            type="button"
            class="tab"
            :class="{ active: activeStatus === tab.key }"
            @click="switchStatus(tab.key)"
          >{{ tab.label }}</button>
        </div>
      </div>

      <div class="pad-workbench__body">
        <div v-if="isParentView" class="parent-tasks-hub-wrap px-4 pt-3">
          <ParentTasksHub ref="parentHubRef" />
        </div>
        <div class="pad-workbench__main">
          <div class="pad-workbench__mobile-head px-4 safe-top stack-4" data-parent-task-list>
            <header class="topbar page-topbar">
              <PageBackButton />
              <h1 class="topbar-title text-xl flex-1 min-w-0">{{ pageTitle }}</h1>
              <router-link to="/search" class="icon-btn" aria-label="搜索">
                <i data-lucide="search" class="icon"></i>
              </router-link>
            </header>
            <div v-if="!isTeacherView" class="tabs" data-tasks-tabs>
              <button
                v-for="tab in visibleCategoryTabs"
                :key="'m-cat-' + tab.key"
                type="button"
                class="tab"
                :class="{ active: activeCategory === tab.key }"
                @click="switchCategory(tab.key)"
              >{{ tab.label }}</button>
            </div>
            <div v-if="!isTeacherView" class="tabs" data-tasks-status-tabs>
              <button
                v-for="tab in statusTabs"
                :key="'m-status-' + tab.key"
                type="button"
                class="tab"
                :class="{ active: activeStatus === tab.key }"
                @click="switchStatus(tab.key)"
              >{{ tab.label }}</button>
            </div>
          </div>

          <div v-if="loading" class="px-4 py-8 text-center muted-2">加载中…</div>
          <div v-else-if="isTeacherView" class="pad-list__grid stack-3 px-4 pb-28">
            <p v-if="teacherTasks.length === 0" class="tasks-empty text-center py-8 muted-2">暂无布置的任务</p>
            <router-link
              v-for="task in teacherTasks"
              :key="task.taskId"
              :to="{ path: '/board', query: { taskId: task.taskId } }"
              class="card card-link card-pad task-card--homework"
            >
              <div class="row justify-between items-start gap-3">
                <div class="min-w-0">
                  <span class="task-type-badge task-type-badge--homework">📋 班级任务</span>
                  <div class="text-base font-bold mt-2">{{ task.title || '未命名任务' }}</div>
                  <p v-if="task.className" class="text-xs muted mt-2">{{ task.className }}</p>
                </div>
                <span class="badge badge-info shrink-0">看板</span>
              </div>
            </router-link>
            <router-link to="/assign" class="btn btn-primary btn-block">
              <i data-lucide="pen-square" class="icon"></i>
              布置新任务
            </router-link>
          </div>
          <div v-else class="pad-list__grid stack-3 px-4 pb-28">
            <p v-if="displayTasks.length === 0" class="tasks-empty text-center py-8 muted-2">暂无符合条件的任务</p>

            <!-- 答题 Tab：今日答题固定入口 -->
            <div v-if="activeCategory === 'quiz' && todayQuizTask" class="task-quiz-block">
              <router-link
                :to="todayQuizTask.link || '/quiz'"
                class="card card-link card-pad task-quiz-compact"
                :class="todayQuizTask.state === 'pending' ? 'task-quiz-compact--pending' : ''"
              >
                <div class="row items-center gap-3">
                  <div class="task-quiz-compact__icon" aria-hidden="true">🧠</div>
                  <div class="flex-1 min-w-0">
                    <div class="row items-center justify-between gap-2">
                      <span class="text-sm font-bold">{{ todayQuizTask.title || '今日答题' }}</span>
                      <span
                        v-if="todayQuizTask.state === 'done' && todayQuizTask.quizScore"
                        class="task-quiz-compact__score text-success"
                      >{{ todayQuizTask.quizScore }}</span>
                      <span
                        v-else-if="todayQuizTask.state === 'pending'"
                        class="badge badge-warning shrink-0"
                      >{{ todayQuizTask.stateLabel }}</span>
                    </div>
                    <p v-if="todayQuizTask.desc" class="text-xs muted mt-1">{{ todayQuizTask.desc }}</p>
                  </div>
                  <span
                    v-if="todayQuizTask.state === 'pending'"
                    class="task-quiz-compact__cta text-xs text-brand font-bold shrink-0"
                  >去答题</span>
                  <span
                    v-else-if="todayQuizTask.state === 'done'"
                    class="badge badge-success shrink-0"
                  >{{ todayQuizTask.stateLabel }}</span>
                </div>
              </router-link>
              <div class="task-quiz-block__foot">
                <router-link to="/quiz/history" class="text-xs text-brand font-medium">答题记录</router-link>
              </div>
            </div>

            <template v-for="task in listTasks" :key="task.id">
              <!-- 历史答题记录 -->
              <router-link
                v-if="task.category === 'quiz'"
                :to="task.link || '/quiz/completed'"
                class="card card-link card-pad task-quiz-compact"
              >
                <div class="row items-center gap-3">
                  <div class="task-quiz-compact__icon">🧠</div>
                  <div class="flex-1 min-w-0">
                    <div class="row justify-between items-start gap-2">
                      <div class="text-sm font-bold">{{ task.title || '每日答题' }}</div>
                      <span class="badge shrink-0" :class="task.badgeClass">{{ task.stateLabel }}</span>
                    </div>
                    <p v-if="task.desc" class="text-xs muted mt-1">{{ task.desc }}</p>
                    <div v-if="task.quizScore" class="task-quiz-compact__score mt-1 text-accent">{{ task.quizScore }}</div>
                  </div>
                </div>
              </router-link>

              <!-- 创意开始 -->
              <button
                v-else-if="task.kind === 'creative-start'"
                type="button"
                class="card card-pad card-link text-left w-full"
                :class="'task-card--' + cardVariant(task)"
                :disabled="startingCreative"
                @click="handleCreativeStart"
              >
                <div class="row justify-between items-start gap-3">
                  <div class="min-w-0">
                    <span class="task-type-badge" :class="'task-type-badge--' + cardVariant(task)">
                      {{ taskTypeLabel(task) }}
                    </span>
                    <div class="text-base font-bold mt-2">{{ task.title }}</div>
                  </div>
                  <span class="badge shrink-0" :class="task.badgeClass">{{ task.stateLabel }}</span>
                </div>
                <p v-if="task.desc" class="text-xs muted mt-2">{{ task.desc }}</p>
              </button>

              <!-- 普通任务 -->
              <div
                v-else-if="isParentView && showParentAssist(task)"
                class="card card-pad"
                :class="'task-card--' + cardVariant(task)"
              >
                <router-link :to="task.link || `/tasks/${task.id}`" class="block card-link">
                  <div class="row justify-between items-start gap-3">
                    <div class="min-w-0">
                      <span class="task-type-badge" :class="'task-type-badge--' + cardVariant(task)">
                        {{ taskTypeLabel(task) }}
                      </span>
                      <div class="text-base font-bold mt-2">{{ task.title }}</div>
                    </div>
                    <span class="badge shrink-0" :class="task.badgeClass">{{ task.stateLabel }}</span>
                  </div>
                  <p v-if="task.desc" class="text-xs muted mt-2">{{ task.desc }}</p>
                </router-link>
                <p v-if="task.materialsLine" class="parent-task-materials text-xs muted mt-3">
                  🧪 材料：{{ task.materialsLine }}
                </p>
                <router-link
                  :to="task.uploadLink || `/upload?taskId=${task.id}`"
                  class="parent-task-upload row items-center justify-center gap-2 mt-3 pt-3 text-sm text-brand font-medium"
                >
                  <i data-lucide="camera" class="icon"></i>
                  上传
                </router-link>
              </div>
              <router-link
                v-else
                :to="task.link || `/tasks/${task.id}`"
                class="card card-link card-pad"
                :class="'task-card--' + cardVariant(task)"
              >
                <div class="row justify-between items-start gap-3">
                  <div class="min-w-0">
                    <span class="task-type-badge" :class="'task-type-badge--' + cardVariant(task)">
                      {{ taskTypeLabel(task) }}
                    </span>
                    <div class="text-base font-bold mt-2">{{ task.title }}</div>
                  </div>
                  <span class="badge shrink-0" :class="task.badgeClass">{{ task.stateLabel }}</span>
                </div>
                <p v-if="task.desc" class="text-xs muted mt-2">{{ task.desc }}</p>
              </router-link>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchTasks } from '@/api/task'
import { fetchTeacherTasks } from '@/api/teacher'
import { startCreativeTask } from '@/api/creative'
import { useParentContext } from '@/composables/useParentContext'
import { isParentRole, isTeacherRole } from '@/utils/role'
import { useUserStore } from '@/stores/user'
import ParentTasksHub from './ParentTasksHub.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const { childQueryParam, selectedChild, loadChildren, children: parentChildren } = useParentContext()
const parentHubRef = ref(null)
const isParentView = computed(() => isParentRole(userStore.userInfo.userRoleId))
const isTeacherView = computed(() => isTeacherRole(userStore.userInfo.userRoleId))
const pageTitle = computed(() => {
  if (isParentView.value) return '孩子任务'
  return '我的任务'
})
appStore.setActiveTab('tasks')

const loading = ref(false)
const startingCreative = ref(false)
const tasks = ref([])
const teacherTasks = ref([])
const activeCategory = ref('experiment')
const activeStatus = ref('pending')

const categoryTabs = [
  { key: 'experiment', label: '实验' },
  { key: 'remix', label: '拍同款' },
  { key: 'creative', label: '创意' },
  { key: 'quiz', label: '答题' }
]

const visibleCategoryTabs = computed(() =>
  isParentView.value ? categoryTabs.filter((t) => t.key !== 'quiz') : categoryTabs
)

const statusTabs = [
  { key: 'pending', label: '待完成' },
  { key: 'done', label: '已完成' }
]

const VALID_CATEGORIES = categoryTabs.map((t) => t.key)

const todayQuizTask = computed(() => {
  if (activeCategory.value !== 'quiz') return null
  return tasks.value.find((t) => t.kind === 'quiz-today') || null
})

const quizHistoryTasks = computed(() => {
  if (activeCategory.value !== 'quiz') return []
  return tasks.value.filter((t) => t.kind === 'quiz-history')
})

const listTasks = computed(() => {
  if (activeCategory.value !== 'quiz') return tasks.value
  return tasks.value.filter((t) => t.kind !== 'quiz-today')
})

const displayTasks = computed(() => {
  if (activeCategory.value === 'quiz' && todayQuizTask.value) {
    return [todayQuizTask.value, ...quizHistoryTasks.value]
  }
  return tasks.value
})

function scrollToParentTaskList() {
  const el = document.querySelector('[data-parent-task-list]')
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

provide('scrollToParentTaskList', scrollToParentTaskList)

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => {
      createIcons({ icons })
    }).catch(() => {})
  })
}

function cardVariant(task) {
  if (task.category === 'quiz') return 'quiz'
  if (task.category === 'remix') return 'remix'
  if (task.category === 'creative') return 'creative'
  if (task.subType === 'simulator') return 'simulator'
  return 'homework'
}

function taskTypeLabel(task) {
  if (task.category === 'quiz') return '🧠 答题'
  if (task.category === 'remix') return '📷 拍同款'
  if (task.category === 'creative') return '💡 创意'
  if (task.subType === 'simulator') return '🖥️ 模拟实验'
  return '🔬 实验'
}

function showParentAssist(task) {
  if (!task || task.state === 'done') return false
  if (task.category === 'quiz') return false
  if (task.kind === 'creative-start') return false
  return true
}

async function loadTasks() {
  loading.value = true
  try {
    if (isTeacherView.value) {
      const res = await fetchTeacherTasks()
      teacherTasks.value = res?.code === 200 && Array.isArray(res.data) ? res.data : []
    } else {
      const res = await fetchTasks({
        childUserId: childQueryParam(),
        category: activeCategory.value,
        status: activeStatus.value,
        page: 1,
        size: 50
      })
      tasks.value = (res?.data?.records) || []
    }
  } catch (e) {
    console.warn('加载任务失败', e)
    if (isTeacherView.value) teacherTasks.value = []
    else tasks.value = []
  } finally {
    loading.value = false
    initIcons()
  }
}

function syncRouteQuery() {
  if (isTeacherView.value) return
  const query = {
    category: activeCategory.value,
    status: activeStatus.value
  }
  if (route.query.category === query.category && route.query.status === query.status) return
  router.replace({ path: '/tasks', query })
}

function switchCategory(key) {
  activeCategory.value = key
  syncRouteQuery()
  loadTasks()
}

function switchStatus(key) {
  activeStatus.value = key
  syncRouteQuery()
  loadTasks()
}

async function handleCreativeStart() {
  if (startingCreative.value) return
  startingCreative.value = true
  try {
    const res = await startCreativeTask()
    const taskId = res?.data?.id
    if (taskId) {
      router.push(`/tasks/${taskId}`)
      return
    }
    await loadTasks()
  } catch (e) {
    const taskId = e?.response?.data?.data?.id
    if (taskId) {
      router.push(`/tasks/${taskId}`)
    } else {
      console.warn('开始创意任务失败', e)
    }
  } finally {
    startingCreative.value = false
  }
}

function resolveQueryCategory() {
  const qCat = route.query.category || route.query.type
  if (qCat === 'homework' || qCat === 'all') return 'experiment'
  if (qCat && VALID_CATEGORIES.includes(qCat)) return qCat
  return null
}

watch(() => [route.query.category, route.query.type], () => {
  const cat = resolveQueryCategory()
  if (cat) {
    activeCategory.value = cat
    loadTasks()
  }
})

watch(selectedChild, () => {
  if (isParentView.value) loadTasks()
})

watch(() => route.query.status, (val) => {
  if (val === 'pending' || val === 'done') {
    activeStatus.value = val
    loadTasks()
  }
})

onMounted(async () => {
  if (isParentView.value) {
    await loadChildren()
    if (activeCategory.value === 'quiz') activeCategory.value = 'experiment'
  }
  const cat = resolveQueryCategory()
  if (cat) activeCategory.value = cat
  const qStatus = route.query.status
  if (qStatus === 'pending' || qStatus === 'done') {
    activeStatus.value = qStatus
  }
  syncRouteQuery()
  loadTasks()
})
</script>

<style scoped>
.tasks-empty {
  column-span: all;
  width: 100%;
}

.parent-task-materials {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.45;
}

.parent-task-upload {
  border-top: 1px solid var(--surface-border, rgba(0, 0, 0, 0.06));
  text-decoration: none;
}
</style>
