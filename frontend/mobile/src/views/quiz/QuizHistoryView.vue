<template>
  <div class="prototype-container pad-shell safe-top" data-layout="detail" data-quiz-flow>
    <div class="topbar page-topbar safe-top">
      <PageBackButton fallback="/quiz" />
      <h1 class="topbar-title">答题记录</h1>
    </div>

    <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
    <div v-else class="px-4 pb-28 stack-3">
      <p v-if="history.length === 0" class="text-center py-12 muted-2">暂无答题记录</p>
      <router-link
        v-for="(item, i) in history"
        :key="i"
        :to="historyLink(item)"
        class="card card-pad card-link row items-center justify-between"
      >
        <div>
          <div class="text-sm font-bold">{{ item.date }}</div>
          <div class="text-xs muted mt-1">得分 {{ item.score }}</div>
        </div>
        <div class="text-right">
          <span class="badge" :class="item.perfect ? 'badge-success' : 'badge-info'">{{ item.points }}</span>
          <div v-if="item.perfect" class="text-xs muted mt-1">全对 🎉</div>
        </div>
      </router-link>
    </div>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchTodayQuiz } from '@/api/quiz'
import { useLucideIcons } from '@/composables/useLucideIcons'

const loading = ref(true)
const history = ref([])

function historyLink(item) {
  const type = item.perfect ? 'perfect' : 'low'
  return `/quiz/result/${type}?date=${item.date}`
}

const { initIcons } = useLucideIcons()

onMounted(async () => {
  try {
    const res = await fetchTodayQuiz()
    if (res?.code === 200 && Array.isArray(res.data?.history)) {
      history.value = res.data.history
    }
  } catch {
    history.value = []
  } finally {
    loading.value = false
    initIcons()
  }
})
</script>
