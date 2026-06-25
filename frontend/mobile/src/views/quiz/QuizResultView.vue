<template>
  <div class="prototype-container pad-shell safe-top" data-layout="detail" data-quiz-flow>
    <div class="topbar page-topbar safe-top">
      <PageBackButton fallback="/quiz" />
      <h1 class="topbar-title">{{ pageTitle }}</h1>
    </div>

    <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
    <div v-else-if="error" class="px-4 py-12 text-center stack-3">
      <p class="muted-2">{{ error }}</p>
      <router-link to="/quiz" class="btn btn-ghost btn-sm">返回答题</router-link>
    </div>

    <div v-else class="px-4 py-8 text-center stack-4">
      <div class="text-5xl">{{ emoji }}</div>
      <h2 class="text-xl font-bold">{{ headline }}</h2>
      <p class="text-sm muted">{{ subtitle }}</p>

      <div class="card card-pad row justify-around mt-4">
        <div class="stat">
          <div class="stat-num text-brand">{{ scoreLabel }}</div>
          <div class="stat-label">正确率</div>
        </div>
        <div class="stat">
          <div class="stat-num text-success">+{{ points }}</div>
          <div class="stat-label">获得积分</div>
        </div>
        <div class="stat">
          <div class="stat-num text-warning">{{ streak }}</div>
          <div class="stat-label">连对天数</div>
        </div>
      </div>

      <div class="stack-2 mt-4">
        <router-link
          v-if="showReview"
          :to="reviewLink"
          class="btn btn-outline btn-block"
        >查看错题解析</router-link>
        <router-link to="/quiz" class="btn btn-gradient btn-block">继续答题</router-link>
        <router-link to="/profile" class="btn btn-ghost btn-block">返回个人中心</router-link>
      </div>
    </div>

    <BottomNav />
  </div>
</template>

<script setup>
import { computed, onMounted, nextTick, ref } from 'vue'
import { useRoute } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchQuizRecord } from '@/api/quiz'
import { useLucideIcons } from '@/composables/useLucideIcons'

const QUIZ_RESULT_KEY = 'mb_quiz_result'

const route = useRoute()
const type = computed(() => route.params.type || 'perfect')
const recordDate = computed(() => (typeof route.query.date === 'string' ? route.query.date : ''))

const loading = ref(true)
const error = ref('')
const result = ref(null)

const pageTitle = computed(() => (type.value === 'practice' ? '练习结果' : '答题结果'))
const isPractice = computed(() => type.value === 'practice')

const streak = computed(() => result.value?.streakDays ?? 0)
const points = computed(() => result.value?.points ?? 0)

const emoji = computed(() => {
  if (isPractice.value) return '🔄'
  return result.value?.perfect ? '🎉' : '💪'
})

const headline = computed(() => {
  if (isPractice.value) return '练习完成'
  if (!result.value) return '—'
  return result.value.perfect ? '答对了！' : '还需加强'
})

const subtitle = computed(() => {
  if (isPractice.value) return '练习模式不计积分'
  if (!result.value) return ''
  return `得分 ${result.value.score}/${result.value.total} · 获得 +${result.value.points} 积分`
})

const scoreLabel = computed(() => {
  if (isPractice.value || !result.value?.total) return '—'
  return Math.round((result.value.score / result.value.total) * 100) + '%'
})

const showReview = computed(() => !isPractice.value && result.value && !result.value.perfect)

const reviewLink = computed(() => {
  const date = recordDate.value || ''
  return date ? `/quiz/review?date=${date}` : '/quiz/review'
})

const { initIcons } = useLucideIcons()

async function loadResult() {
  loading.value = true
  error.value = ''
  try {
    if (isPractice.value) {
      const raw = sessionStorage.getItem(QUIZ_RESULT_KEY)
      if (raw) {
        result.value = JSON.parse(raw)
        return
      }
      error.value = '练习结果已过期，请重新练习'
      return
    }

    const raw = sessionStorage.getItem(QUIZ_RESULT_KEY)
    if (raw && !recordDate.value) {
      result.value = JSON.parse(raw)
      return
    }

    const res = await fetchQuizRecord(recordDate.value || undefined)
    if (res?.code === 200 && res.data) {
      result.value = res.data
      return
    }
    error.value = res?.message || '答题记录不存在'
  } catch {
    error.value = '加载失败，请稍后重试'
  } finally {
    loading.value = false
    initIcons()
  }
}

onMounted(loadResult)
</script>
