<template>
  <MobilePageShell class="safe-top safe-bottom" data-layout="detail" data-quiz-flow>
    <div class="topbar page-topbar safe-top">
      <PageBackButton fallback="/tasks?status=pending&category=quiz" />
      <h1 class="topbar-title">🧠 每日答题</h1>
      <span v-if="result" class="badge badge-success shrink-0">已完成</span>
    </div>

    <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
    <div v-else-if="error" class="px-4 py-12 text-center stack-3">
      <p class="muted-2">{{ error }}</p>
      <router-link to="/quiz" class="btn btn-primary btn-sm">去答题</router-link>
    </div>

    <template v-else-if="result">
      <div class="px-4 mt-4">
        <div class="card card-pad quiz-completed-lock text-center">
          <div class="text-5xl mb-3">{{ result.perfect ? '✅' : '💪' }}</div>
          <h2 class="text-lg font-bold">今日答题已完成</h2>
          <p class="text-sm muted mt-2">
            得分 <strong :class="result.perfect ? 'text-success' : 'text-warning'">{{ scoreLabel }}</strong>
            · 获得 <strong class="text-warning">+{{ result.points ?? 0 }}</strong> 积分
          </p>
          <p class="text-xs muted mt-3">下一轮刷新时间：明日 00:00</p>
        </div>
      </div>

      <div class="px-4 mt-4 stack-3 pb-28">
        <div class="card card-pad">
          <h3 class="text-sm font-bold mb-2">今日记录</h3>
          <div class="row justify-between text-xs muted py-2 border-b">
            <span>提交时间</span><span>{{ result.submitTime || '—' }}</span>
          </div>
          <div class="row justify-between text-xs muted py-2 border-b">
            <span>连对天数</span><span class="text-success font-bold">{{ result.streakDays ?? 0 }} 天</span>
          </div>
          <div class="row justify-between text-xs muted py-2">
            <span>错题数</span><span>{{ result.wrongCount ?? 0 }} 题</span>
          </div>
        </div>

        <div class="card card-pad stack-2">
          <router-link
            v-if="result.resultType"
            :to="`/quiz/result/${result.resultType}`"
            class="btn btn-outline btn-block"
          >查看今日正式结果</router-link>
          <router-link to="/quiz?mode=practice" class="btn btn-primary btn-block">重做练习（不计分）</router-link>
          <router-link to="/quiz/history" class="btn btn-gradient btn-block">查看答题记录</router-link>
          <router-link to="/badges" class="btn btn-soft btn-block">查看勋章进度</router-link>
          <p class="text-xs muted text-center mt-2">今日正式答题已计分 · 练习不限次数</p>
        </div>
      </div>
    </template>
  </MobilePageShell>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchTodayQuiz } from '@/api/quiz'
import { useLucideIcons } from '@/composables/useLucideIcons'

const router = useRouter()
const loading = ref(true)
const error = ref('')
const result = ref(null)

const scoreLabel = computed(() => {
  if (!result.value) return '—'
  return `${result.value.score} / ${result.value.total}`
})

const { initIcons } = useLucideIcons()

onMounted(async () => {
  try {
    const res = await fetchTodayQuiz()
    if (res?.code === 200 && res.data?.submittedToday && res.data.todayResult) {
      result.value = res.data.todayResult
      return
    }
    if (res?.code === 200 && !res.data?.submittedToday) {
      router.replace('/quiz')
      return
    }
    error.value = '今日尚未完成正式答题'
  } catch {
    error.value = '加载失败，请稍后重试'
  } finally {
    loading.value = false
    initIcons()
  }
})
</script>
