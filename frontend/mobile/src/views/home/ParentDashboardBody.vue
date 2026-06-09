<template>

  <div class="stack-4">

    <div class="card card-pad">

      <div class="row items-center justify-between mb-3">

        <span class="text-sm font-bold">{{ activeChild.name }} · 今日进度</span>

        <button
          type="button"
          class="text-xs text-brand font-medium btn-link"
          @click="onViewAllTasks"
        >查看全部</button>

      </div>

      <div class="grid-2 gap-3">

        <div class="stat surface-2 rounded-lg p-3">

          <div class="stat-num">{{ activeProgress.pending }}</div>

          <div class="stat-label">待完成</div>

        </div>

        <div class="stat surface-2 rounded-lg p-3">

          <div class="stat-num text-success">{{ activeProgress.completed }}</div>

          <div class="stat-label">已完成</div>

        </div>

      </div>

      <div class="progress mt-3">

        <div class="progress-fill progress-fill-success" :style="{ width: activeProgress.completionRate + '%' }"></div>

      </div>

      <div class="text-xs muted-2 mt-2 text-right">完成率 {{ activeProgress.completionRate }}%</div>

      <div v-if="activeQuizToday" class="row items-center gap-2 mt-3 pt-3 surface-2 rounded-lg px-3 py-2">
        <span class="text-base shrink-0">🧠</span>
        <span class="text-xs flex-1" :class="quizTodayClass">{{ activeQuizToday.label }}</span>
      </div>

    </div>



    <div class="card card-pad">

      <div class="text-sm font-bold mb-3">最近动态</div>

      <div class="stack-2">
        <div v-if="!activeActivities.length" class="text-xs muted-2 py-2">暂无最近动态</div>
        <div v-for="(act, idx) in activeActivities" :key="idx" class="row gap-3 items-start">

          <span class="notif-dot mt-1" :class="notifDotClass(act.type)"></span>

          <div class="flex-1">

            <div class="text-sm">{{ act.content }}</div>

            <div class="text-xs muted-2 mt-1">{{ act.time }}</div>

          </div>

        </div>

      </div>

    </div>

  </div>

</template>



<script setup>

import { computed, inject } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const selectedChildId = inject('parentSelectedId')
const dashboardRef = inject('parentDashboard', null)
const scrollToParentTaskList = inject('scrollToParentTaskList', null)



const fb = computed(() => dashboardRef?.value || null)



const activeChild = computed(() => {
  if (!fb.value) return { id: null, name: '', pending: 0, completed: 0 }
  return fb.value.children.find((c) => c.id === selectedChildId.value) || fb.value.children[0] || { id: null, name: '', pending: 0, completed: 0 }
})



const activeProgress = computed(() => {

  const c = activeChild.value

  const total = (c.pending || 0) + (c.completed || 0)

  return {

    pending: c.pending || 0,

    completed: c.completed || 0,

    completionRate: total ? Math.round((c.completed / total) * 100) : 0

  }

})

const activeActivities = computed(() => {
  if (!fb.value) return []
  const acts = fb.value.activities || {}
  return acts[activeChild.value.id] || []
})

const activeQuizToday = computed(() => {
  if (!fb.value) return null
  const map = fb.value.quizToday || {}
  return map[activeChild.value.id] || null
})

const quizTodayClass = computed(() => {
  const state = activeQuizToday.value?.state
  if (state === 'done') return 'text-success'
  if (state === 'pending') return 'text-warning'
  return 'muted-2'
})

function notifDotClass(type) {

  return { completed: 'notif-dot-brand', submitted: 'notif-dot-success', newTask: 'notif-dot-warning' }[type] || 'notif-dot-brand'

}

function onViewAllTasks() {
  if (typeof scrollToParentTaskList === 'function') {
    scrollToParentTaskList()
    return
  }
  router.push('/tasks')
}

</script>

<style scoped>
.btn-link {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
}
</style>

