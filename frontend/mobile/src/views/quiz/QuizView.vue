<template>
  <div class="prototype-container pad-shell safe-top safe-bottom" data-layout="detail" data-quiz-flow>
    <div class="topbar page-topbar safe-top">
      <PageBackButton />
      <h1 class="topbar-title">🧠 每日答题</h1>
      <router-link to="/quiz/history" class="icon-btn" aria-label="答题记录">
        <i data-lucide="scroll-text" class="icon"></i>
      </router-link>
    </div>

    <div v-if="isPractice" class="px-4 mt-2">
      <p class="quiz-mode-practice-banner">🔄 <strong>练习模式</strong> · 不计积分 · 不影响连对天数</p>
    </div>

    <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>

    <div v-else-if="!quiz.ready" class="px-4 py-12 text-center stack-3">
      <p class="muted-2">{{ quiz.message || '题库暂无可用题目' }}</p>
      <router-link to="/tasks?status=pending&category=quiz" class="btn btn-ghost btn-sm">返回我的任务</router-link>
    </div>

    <template v-else>
      <div class="px-4 mt-3">
        <div class="card card-pad quiz-nudge">
          <div class="row items-center justify-between gap-2 mb-2">
            <span class="text-sm font-bold">今日 {{ totalQuestions }} 题</span>
            <span class="badge badge-info">{{ answeredCount }} / {{ totalQuestions }} 题已选</span>
          </div>
          <div class="progress">
            <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <p class="text-xs muted mt-2">
            🔥 已连续答对 <strong>{{ quiz.streakDays }}</strong> 天
            <span v-if="quiz.bonusPoints"> · 连对额外 +{{ quiz.bonusPoints }} 积分</span>
            <span v-if="quiz.basePoints"> · 全对 +{{ quiz.basePoints }} 基础分</span>
          </p>
        </div>
      </div>

      <div class="px-4 mt-2">
        <p class="quiz-tip-banner">系统已根据日期自动分配今日题目，提交前不显示对错。</p>
      </div>
      <div class="px-4 mt-3 pb-28">
        <div class="card">
          <div class="card-header row items-center gap-2 flex-wrap">
            <span class="badge badge-info">选择题</span>
            <span class="text-xs muted">第 {{ currentIndex + 1 }} / {{ totalQuestions }} 题</span>
            <span v-if="currentQuestion.meta" class="badge badge-slate ml-auto">{{ currentQuestion.meta }}</span>
          </div>
          <div class="card-body">
            <p class="text-base font-bold leading-tight">{{ currentQuestion.title }}</p>
            <div class="stack mt-4">
              <button
                v-for="(opt, i) in currentQuestion.options"
                :key="i"
                type="button"
                class="card card-pad text-left w-full"
                :class="{ 'ring-brand': answers[currentIndex] === i }"
                @click="selectOption(i)"
              >{{ String.fromCharCode(65 + i) }}. {{ opt }}</button>
            </div>
            <div class="row gap-2 mt-4">
              <button
                v-if="currentIndex > 0"
                type="button"
                class="btn btn-outline flex-1"
                @click="prevQuestion"
              >上一题</button>
              <button
                type="button"
                class="btn btn-primary flex-1"
                :disabled="answers[currentIndex] === null"
                @click="next"
              >
                {{ nextButtonLabel }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchTodayQuiz, submitQuiz } from '@/api/quiz'
import { useLucideIcons } from '@/composables/useLucideIcons'

const QUIZ_DRAFT_KEY = 'mb_quiz_draft'
const QUIZ_RESULT_KEY = 'mb_quiz_result'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const submitting = ref(false)
const currentIndex = ref(0)
const quiz = ref({
  ready: false,
  message: '',
  questions: [],
  questionsPerDay: 3,
  streakDays: 0,
  bonusPoints: 0,
  basePoints: 10,
  submittedToday: false
})
const questions = computed(() => quiz.value.questions || [])
const totalQuestions = computed(() => questions.value.length || quiz.value.questionsPerDay || 3)
const isPractice = computed(() => route.query.mode === 'practice')

const answers = ref([])

watch(questions, (qs) => {
  answers.value = qs.length ? qs.map(() => null) : []
  currentIndex.value = 0
}, { immediate: true })

const currentQuestion = computed(() => questions.value[currentIndex.value] || { options: [] })
const answeredCount = computed(() => answers.value.filter((a) => a !== null && a !== undefined).length)
const progressPercent = computed(() => {
  if (!totalQuestions.value) return 0
  return Math.round((answeredCount.value / totalQuestions.value) * 100)
})
const isLastQuestion = computed(() => currentIndex.value >= totalQuestions.value - 1)
const nextButtonLabel = computed(() => {
  if (isLastQuestion.value) {
    return isPractice.value ? '提交练习' : '确认提交'
  }
  return '下一题'
})

function selectOption(i) {
  answers.value[currentIndex.value] = i
}

function prevQuestion() {
  if (currentIndex.value > 0) currentIndex.value -= 1
}

function saveDraft() {
  sessionStorage.setItem(QUIZ_DRAFT_KEY, JSON.stringify({
    answers: answers.value,
    practice: isPractice.value,
    currentIndex: currentIndex.value
  }))
}

async function next() {
  if (answers.value[currentIndex.value] === null) return
  if (!isLastQuestion.value) {
    currentIndex.value += 1
    initIcons()
    return
  }
  saveDraft()
  if (isPractice.value) {
    await submitDraft(true)
    return
  }
  router.push('/quiz/submit')
}

async function submitDraft(practice) {
  if (submitting.value) return
  submitting.value = true
  try {
    const res = await submitQuiz({ answers: answers.value, practice })
    if (res?.code === 200 && res.data) {
      sessionStorage.setItem(QUIZ_RESULT_KEY, JSON.stringify(res.data))
      sessionStorage.removeItem(QUIZ_DRAFT_KEY)
      router.replace(`/quiz/result/${res.data.resultType || (practice ? 'practice' : 'low')}`)
      return
    }
    if (res?.code === 409 && !practice) {
      const todayRes = await fetchTodayQuiz().catch(() => null)
      const tr = todayRes?.data?.todayResult
      if (tr) {
        const data = {
          score: tr.score,
          total: tr.total,
          points: tr.points,
          perfect: tr.perfect,
          resultType: tr.resultType || (tr.perfect ? 'perfect' : 'low'),
          streakDays: tr.streakDays
        }
        sessionStorage.setItem(QUIZ_RESULT_KEY, JSON.stringify(data))
        sessionStorage.removeItem(QUIZ_DRAFT_KEY)
        router.replace(`/quiz/result/${data.resultType}`)
        return
      }
      router.replace('/quiz/completed')
      return
    }
    alert(res?.message || '提交失败，请稍后重试')
  } catch (e) {
    if (e?.response?.status === 409 && !practice) {
      router.replace('/quiz/completed')
      return
    }
    alert(e?.response?.data?.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const { initIcons } = useLucideIcons()

onMounted(async () => {
  try {
    const res = await fetchTodayQuiz()
    if (res?.code === 200 && res.data) {
      quiz.value = res.data
      if (res.data.submittedToday && !isPractice.value) {
        router.replace('/quiz/completed')
        return
      }
      try {
        const raw = sessionStorage.getItem(QUIZ_DRAFT_KEY)
        if (raw) {
          const draft = JSON.parse(raw)
          if (Array.isArray(draft.answers) && draft.answers.length === (res.data.questions?.length || 0)) {
            answers.value = draft.answers
            if (typeof draft.currentIndex === 'number') {
              currentIndex.value = Math.min(draft.currentIndex, (res.data.questions?.length || 1) - 1)
            }
          }
        }
      } catch { /* ignore */ }
    }
  } catch {
    quiz.value = { ...quiz.value, ready: false, message: '加载失败，请稍后重试' }
  } finally {
    loading.value = false
    initIcons()
  }
})
</script>

<style scoped>
.ring-brand { outline: 2px solid var(--brand); }
</style>
