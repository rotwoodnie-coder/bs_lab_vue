<template>

  <div

    class="prototype-container pad-shell"

    :class="{ 'theme-teacher': isTeacherView }"

    :data-layout="isTeacherView ? 'teacher-home' : 'list-workbench'"

    data-tasks-list

  >

    <BottomNav />



    <div class="pad-main pad-workbench">

      <header class="pad-workbench__topbar">

        <PageBackButton />

        <h1 class="pad-workbench__title">{{ pageTitle }}</h1>

        <div class="pad-workbench__topbar-actions">

          <router-link v-if="isTeacherView" to="/assign" class="icon-btn" aria-label="布置任务">

            <i data-lucide="pen-square" class="icon"></i>

          </router-link>

          <router-link v-else to="/search" class="icon-btn" aria-label="搜索">

            <i data-lucide="search" class="icon"></i>

          </router-link>

        </div>

      </header>



      <div class="pad-workbench__filters">

        <div class="tabs" data-tasks-status-tabs>

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



      <!-- 手机竖屏：teacher-home 下 filters 隐藏，单独展示 Tab -->
      <div v-if="isTeacherView" class="tasks-teacher-mobile-tabs px-4 pt-2">
        <div class="tabs" data-tasks-status-tabs>
          <button
            v-for="tab in statusTabs"
            :key="'mt-' + tab.key"
            type="button"
            class="tab"
            :class="{ active: activeStatus === tab.key }"
            @click="switchStatus(tab.key)"
          >{{ tab.label }}</button>
        </div>
      </div>



      <div

        class="pad-workbench__body"

        :class="{ 'pad-workbench__body--teacher': isTeacherView && showTeacherHub }"

      >

        <TeacherTasksHub

          v-if="isTeacherView && showTeacherHub"

          ref="teacherHubRef"

        />



        <div v-else class="pad-workbench__main">

          <div class="pad-workbench__mobile-head px-4 safe-top stack-4">

            <header class="topbar page-topbar">

              <PageBackButton />

              <h1 class="topbar-title text-xl flex-1 min-w-0">{{ pageTitle }}</h1>

              <router-link v-if="isTeacherView" to="/assign" class="icon-btn" aria-label="布置任务">

                <i data-lucide="pen-square" class="icon"></i>

              </router-link>

              <router-link v-else to="/search" class="icon-btn" aria-label="搜索">

                <i data-lucide="search" class="icon"></i>

              </router-link>

            </header>

            <div class="tabs" data-tasks-status-tabs>

              <button

                v-for="tab in statusTabs"

                :key="'m-' + tab.key"

                type="button"

                class="tab"

                :class="{ active: activeStatus === tab.key }"

                @click="switchStatus(tab.key)"

              >{{ tab.label }}</button>

            </div>

          </div>



          <div v-if="isParentView" class="px-4 pt-2">

            <ChildPicker />

          </div>



          <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>

          <div v-else class="pad-list__grid stack-3 px-4 pb-28">

            <p v-if="tasks.length === 0" class="tasks-empty text-center py-12 muted-2">

              {{ emptyLabel }}

            </p>



            <template v-for="task in tasks" :key="task.id + '-' + task.kind">

              <button

                v-if="task.kind === 'creative-start'"

                type="button"

                class="card card-pad card-link text-left w-full"

                :class="'task-card--' + cardVariant(task)"

                :disabled="startingCreative"

                @click="handleCreativeStart"

              >

                <TaskCardInner :task="task" />

              </button>



              <div

                v-else-if="isParentView && showParentAssist(task)"

                class="card card-pad"

                :class="'task-card--' + cardVariant(task)"

              >

                <router-link :to="taskLink(task)" class="block card-link">

                  <TaskCardInner :task="task" />

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

                :to="taskLink(task)"

                class="card card-link card-pad"

                :class="'task-card--' + cardVariant(task)"

              >

                <TaskCardInner :task="task" />

              </router-link>

            </template>

          </div>

        </div>

      </div>

    </div>

  </div>

</template>



<script setup>

import { ref, computed, onMounted, watch, provide } from 'vue'

import { useRoute, useRouter } from 'vue-router'

import { useAppStore } from '@/stores/app'

import BottomNav from '@/components/BottomNav.vue'

import PageBackButton from '@/components/PageBackButton.vue'

import ChildPicker from '@/views/home/ChildPicker.vue'

import TaskCardInner from '@/components/tasks/TaskCardInner.vue'

import TeacherTasksHub from '@/views/tasks/TeacherTasksHub.vue'

import { fetchTaskInbox, fetchTasks } from '@/api/task'

import { startCreativeTask } from '@/api/creative'

import { useParentContext } from '@/composables/useParentContext'

import { useLucideIcons } from '@/composables/useLucideIcons'
import { isParentRole, normalizeRole } from '@/utils/role'

import { useUserStore } from '@/stores/user'



const route = useRoute()

const router = useRouter()

const appStore = useAppStore()

const userStore = useUserStore()

const { childQueryParam, selectedChild, loadChildren, selectChild, children } = useParentContext()



const isParentView = computed(() => isParentRole(userStore.userInfo.userRoleId))

const isTeacherView = computed(() => normalizeRole(userStore.userInfo.userRoleId) === 'teacher')

const showTeacherHub = computed(() => activeStatus.value === 'pending')

const useStudentTaskApi = computed(() => {

  const role = normalizeRole(userStore.userInfo.userRoleId)

  return role === 'student' || role === 'parent'

})



provide('parentSelectedId', selectedChild)

provide('parentSelectChild', selectChild)

provide('parentChildren', computed(() => children.value))



appStore.setActiveTab('tasks')



const loading = ref(false)

const startingCreative = ref(false)

const allTasks = ref([])

const activeStatus = ref('pending')

const teacherHubRef = ref(null)



const categoryLabels = {

  experiment: '我的实验',

  remix: '拍同款',

  creative: '创意实验',

  quiz: '每日答题'

}



const activeCategory = computed(() => {

  const cat = route.query.category

  return typeof cat === 'string' && categoryLabels[cat] ? cat : ''

})



const pageTitle = computed(() => {

  if (isTeacherView.value) return '班级任务'

  if (activeCategory.value) {

    return categoryLabels[activeCategory.value]

  }

  return '我的任务'

})



const tasks = computed(() => {

  if (!activeCategory.value) return allTasks.value

  return allTasks.value.filter((t) => t.category === activeCategory.value)

})



const statusTabs = computed(() => {

  if (isTeacherView.value) {

    return [

      { key: 'pending', label: '进行中' },

      { key: 'done', label: '已完成' },

      { key: 'cancelled', label: '已取消' }

    ]

  }

  return [

    { key: 'pending', label: '待办任务' },

    { key: 'done', label: '已完成' }

  ]

})



const emptyLabel = computed(() => {

  if (activeStatus.value === 'cancelled') return '暂无已取消任务'

  if (activeStatus.value === 'pending') {

    return isTeacherView.value ? '暂无进行中的任务' : '暂无待办任务'

  }

  return '暂无已完成任务'

})



const { initIcons } = useLucideIcons()



function cardVariant(task) {

  if (task.category === 'quiz') return 'quiz'

  if (task.category === 'remix') return 'remix'

  if (task.category === 'creative') return 'creative'

  if (task.category === 'class') return 'homework'

  if (task.category === 'audit') return 'simulator'

  if (task.subType === 'simulator') return 'simulator'

  return 'homework'

}



function taskLink(task) {

  if (task.link) return task.link

  if (task.kind === 'teacher-class' || task.kind === 'teacher-class-cancelled' || task.category === 'class') {

    return `/tasks/${task.id}/summary`

  }

  if (task.kind === 'exp-audit' || task.category === 'audit') {

    return `/content-audits/${task.id}`

  }

  return `/tasks/${task.id}`

}



function showParentAssist(task) {

  if (!task || task.state === 'done') return false

  if (task.category === 'quiz') return false

  if (task.kind === 'creative-start') return false

  if (task.category === 'class' || task.category === 'audit') return false

  return true

}



async function loadStudentParentTasks() {

  const childUserId = childQueryParam()

  const status = activeStatus.value

  const categories = isParentView.value

    ? ['experiment', 'remix', 'creative']

    : ['experiment', 'remix', 'creative', 'quiz']

  const results = await Promise.all(

    categories.map((category) =>

      fetchTasks({ childUserId, category, status, page: 1, size: 50 })

    )

  )

  const merged = []

  const seen = new Set()

  for (const res of results) {

    if (res?.code !== 200) continue

    for (const item of res?.data?.records || []) {

      const key = `${item.id}-${item.kind || item.category || ''}`

      if (seen.has(key)) continue

      seen.add(key)

      merged.push(item)

    }

  }

  merged.sort((a, b) => (b.sortTime || 0) - (a.sortTime || 0))

  allTasks.value = merged

}



async function loadTasks() {

  if (isTeacherView.value && showTeacherHub.value) {

    teacherHubRef.value?.reload?.()

    initIcons()

    return

  }

  loading.value = true

  try {

    if (useStudentTaskApi.value) {

      await loadStudentParentTasks()

      return

    }

    const res = await fetchTaskInbox({

      childUserId: childQueryParam(),

      status: activeStatus.value,

      page: 1,

      size: 50

    })

    allTasks.value = res?.code === 200 ? (res?.data?.records || []) : []

  } catch (e) {

    console.warn('加载任务失败', e)

    allTasks.value = []

  } finally {

    loading.value = false

    initIcons()

  }

}



function syncRouteQuery() {

  const query = { status: activeStatus.value }

  if (activeCategory.value) {

    query.category = activeCategory.value

  }

  const sameStatus = route.query.status === query.status

  const sameCategory = (route.query.category || '') === (query.category || '')

  if (sameStatus && sameCategory) return

  router.replace({ path: '/tasks', query })

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



watch(selectedChild, () => {

  if (isParentView.value) loadTasks()

})



watch(() => route.query.status, (val) => {

  const allowed = isTeacherView.value

    ? ['pending', 'done', 'cancelled']

    : ['pending', 'done']

  if (allowed.includes(val)) {

    if (activeStatus.value !== val) {

      activeStatus.value = val

    }

    loadTasks()

  }

})



watch(() => route.query.category, () => {

  if (!isTeacherView.value || !showTeacherHub.value) {

    loadTasks()

  }

})



onMounted(async () => {

  initIcons()

  if (isParentView.value) {

    await loadChildren()

  }

  const qStatus = route.query.status

  const allowed = isTeacherView.value

    ? ['pending', 'done', 'cancelled']

    : ['pending', 'done']

  if (allowed.includes(qStatus)) {

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

.tasks-teacher-mobile-tabs {
  display: none;
}

@media (max-width: 767px) {
  .tasks-teacher-mobile-tabs {
    display: block;
  }
}

</style>


